package com.jsoizo.retryeither

import arrow.core.Either
import com.github.michaelbull.retry.attempt.Attempt
import com.github.michaelbull.retry.attempt.firstAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.RetryInstruction
import com.github.michaelbull.retry.instruction.StopRetrying
import com.github.michaelbull.retry.policy.RetryPolicy
import kotlinx.coroutines.delay
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Calls the specified function [block] and returns its [Either], handling any [Either.Left] returned from the [block] function
 * execution retrying the invocation according to [instructions][RetryInstruction] from the [policy].
 */
public suspend inline fun <A, B> retry(policy: RetryPolicy<A>, block: () -> Either<A, B>): Either<A, B> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    val attempt: Attempt = firstAttempt()

    while (true) {
        val result = block()
        result.fold({ error ->
            val failedAttempt = attempt.failedWith(error)
            when (val instruction = policy(failedAttempt)) {
                StopRetrying -> {
                    return result
                }

                ContinueRetrying -> {
                    attempt.retryImmediately()
                }

                else -> {
                    val (delayMillis) = instruction
                    delay(delayMillis)
                    attempt.retryAfter(delayMillis)
                }
            }
        }, {
            // When the result is right, return it
            return result
        })
    }
}
