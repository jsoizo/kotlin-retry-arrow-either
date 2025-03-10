package com.jsoizo.retryeither

import arrow.core.Either
import com.github.michaelbull.retry.policy.constantDelay
import com.github.michaelbull.retry.policy.continueIf
import com.github.michaelbull.retry.policy.stopAtAttempts
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class EitherTest {

    private data class AttemptsError(val attempts: Int)

    @Test
    fun retryToAttemptLimit() = runTest {
        val fiveTimes = stopAtAttempts<AttemptsError>(5)
        var attempts = 0

        val result = retry(fiveTimes) {
            attempts++

            if (attempts < 5) {
                Either.Left(AttemptsError(attempts))
            } else {
                Either.Right(Unit)
            }
        }

        assertEquals(Either.Right(Unit), result)
        assertEquals(5, attempts)
    }

    @Test
    fun retryExhaustingAttemptLimit() = runTest {
        val tenTimes = stopAtAttempts<AttemptsError>(10)
        var attempts = 0

        val result = retry(tenTimes) {
            attempts++

            if (attempts < 15) {
                Either.Left(AttemptsError(attempts))
            } else {
                Either.Right(Unit)
            }
        }

        assertEquals(Either.Left(AttemptsError(10)), result)
        assertEquals(10, attempts)
    }

    @Test
    fun retryThrowsCancellationException() = runTest {
        val tenTimes = stopAtAttempts<Unit>(10)

        assertFailsWith<CancellationException> {
            retry(tenTimes) {
                Either.Right(Unit).also {
                    throw CancellationException()
                }
            }
        }
    }

    @Test
    fun retryStopsAfterCancellation() = runTest {
        val fiveTimes = stopAtAttempts<Unit>(5)
        var attempts = 0

        assertFailsWith<CancellationException> {
            retry(fiveTimes) {
                attempts++

                if (attempts == 2) {
                    throw CancellationException()
                } else {
                    Either.Left(Unit)
                }
            }
        }

        assertEquals(2, attempts)
    }

    @Test
    fun retryWithCustomPolicy() = runTest {
        val uptoFifteenTimes = continueIf<AttemptsError> { (failure) ->
            failure.attempts < 15
        }

        var attempts = 0

        val result = retry(uptoFifteenTimes) {
            attempts++
            Either.Left(AttemptsError(attempts))
        }

        assertEquals(Either.Left(AttemptsError(15)), result)
    }

    @Test
    fun cancelRetryFromJob() = runTest {
        val every100ms = constantDelay<AttemptsError>(100)
        var attempts = 0

        val job = backgroundScope.launch {
            retry(every100ms) {
                attempts++
                Either.Left(AttemptsError(attempts))
            }
        }

        testScheduler.advanceTimeBy(350)
        testScheduler.runCurrent()

        job.cancel()

        testScheduler.advanceUntilIdle()

        assertTrue(job.isCancelled)
        assertEquals(4, attempts)

        testScheduler.advanceTimeBy(2000)
        testScheduler.runCurrent()

        assertTrue(job.isCancelled)
        assertEquals(4, attempts)
    }

    @Test
    fun cancelRetryWithinJob() = runTest {
        val every20ms = constantDelay<AttemptsError>(20)
        var attempts = 0

        val job = launch {
            retry(every20ms) {
                attempts++

                if (attempts == 15) {
                    cancel()
                }

                Either.Left(AttemptsError(attempts))
            }
        }

        testScheduler.advanceUntilIdle()

        assertTrue(job.isCancelled)
        assertEquals(15, attempts)

        testScheduler.advanceTimeBy(2000)
        testScheduler.runCurrent()

        assertTrue(job.isCancelled)
        assertEquals(15, attempts)
    }

    @Test
    fun cancelRetryWithinChildJob() = runTest {
        val every20ms = constantDelay<AttemptsError>(20)
        var attempts = 0

        lateinit var childJobOne: Deferred<Int>
        lateinit var childJobTwo: Deferred<Int>

        val parentJob = launch {
            retry(every20ms) {
                childJobOne = async {
                    delay(100)
                    attempts
                }

                childJobTwo = async {
                    delay(50)

                    if (attempts == 15) {
                        cancel()
                    }

                    1
                }

                attempts = childJobOne.await() + childJobTwo.await()

                Either.Left(AttemptsError(attempts))
            }
        }

        testScheduler.advanceUntilIdle()

        assertTrue(parentJob.isCancelled)
        assertFalse(childJobOne.isCancelled)
        assertTrue(childJobTwo.isCancelled)
        assertEquals(15, attempts)

        testScheduler.advanceTimeBy(2000)
        testScheduler.runCurrent()

        assertTrue(parentJob.isCancelled)
        assertFalse(childJobOne.isCancelled)
        assertTrue(childJobTwo.isCancelled)
        assertEquals(15, attempts)
    }
}
