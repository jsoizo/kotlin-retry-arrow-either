# kotlin-retry-arrow-either

[![Maven Central Version](https://img.shields.io/maven-central/v/com.jsoizo/kotlin-retry-arrow-either)](https://central.sonatype.com/artifact/com.jsoizo/kotlin-retry-arrow-either)/
[![codecov](https://codecov.io/gh/jsoizo/kotlin-retry-arrow-either/graph/badge.svg?token=hlqka9e5Ei)](https://codecov.io/gh/jsoizo/kotlin-retry-arrow-either)

## About

An extension library for [kotlin-retry](https://github.com/michaelbull/kotlin-retry) that provides retry functionality for [Arrow](https://arrow-kt.io/)'s Either type.

## Overview

This library extends kotlin-retry to handle Arrow's Either return types, making it easier to implement retry logic for operations that return `Either<E, A>`. It's particularly useful when you need to retry operations that may fail but use Either for error handling instead of exceptions.

## Installation

Add the following dependency to your build.gradle.kts or build.gradle file:

```kotlin
dependencies {
    implementation("com.michael-bull.kotlin-retry:kotlin-retry:2.0.1")
    implementation("com.jsoizo:kotlin-retry-arrow-either:0.1.0")
}
```

## Usage

The library provides a `retry` function that works similarly to the one provided by kotlin-retry, but it expects a function that returns an `Either<E, A>` instead of a function that throws an exception.  
Retry policies are defined using the functions provided by kotlin-retry, such as `constantDelay`, `binaryExponentialBackoff`, etc.

```kotlin
import arrow.core.Either
import com.github.michaelbull.retry.policy.stopAtAttempts
import com.jsoizo.retryeither.retry

val fiveTimes = stopAtAttempts<AttemptsError>(5)

suspend fun fetchData(): Either<FetchError, Data> = 
    retry(retryPolicy = fiveTimes) {
        // Call an API that returns Either<FetchError, Data>
        callApi()
    }
```

## Version Compatibility

- Kotlin: 2.1+
- Arrow: 2.0+
- kotlin-retry: 2.0+

## License

This library is released under the MIT License. See [LICENSE](LICENSE) for details.