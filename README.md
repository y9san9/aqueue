# aqueue

> Asynchronous Queue with fine-grained control over concurrency

Useful for cases when you need to combine asynchronous 
and synchronous behaviour in your services.

A good example of such behaviour is processing messages in
telegram bot. Messages in such case should be processed 
in parallel. However, if there are multiple messages received
from the same user, they must be processed consequently.

Note that the project is feature complete. 
I am happy to review pull requests, but I don't plan any further development.

## Install

```kotlin
dependencies {
    implementation("me.y9san9.aqueue:core:1.0.0")
}
```

## Usage

```kotlin
suspend fun main() {
    val natural = flow {
        var number = 0
        while (true) emit(number++)
    }.take(count = 10)
    
    // This will be executed in roughly 1 second,
    // because every key is unique.
    // Every action will run in parallel.
    natural.mapInAQueue(
        key = { it },
        action = { delay(1_000) }
    ).collect()

    // This will be executed in roughly 10 seconds,
    // because all keys are the same.
    // Every action will run consecutively.
    natural.mapInAQueue(
        key = { Unit },
        action = { delay(1_000) }
    ).collect()

    // This will be executed in roughly 5 seconds,
    // because the key is either 0 or 1.
    // There would be 2 consecutive queues:
    // - For even numbers
    // - For odd numbers
    // Two queues cut time from 10 seconds to 5 seconds
    natural.mapInAQueue(
        key = { it % 2 },
        action = { delay(1_000) }
    ).collect()
}
```

## Example

See [this example](example/src/main/kotlin/Main.kt) to play around with AQueue.

## API

```kotlin
/**
 * Asynchronous Queue with fine-grained control over concurrency
 */
interface AQueue<TRequest, TResponse> {

    /**
     * Executes [request] with fine-grained control over concurrency
     *
     * @param request The request to execute
     * @param key It is guaranteed that requests with the same [key] will be executed consecutively
     * @param context The context that is used to launch new coroutines. You may limit parallelism using context
     * @param action The action to perform with [request]
     */
    suspend fun execute(
        request: TRequest,
        key: Any? = null,
        context: CoroutineContext = EmptyCoroutineContext,
        action: suspend (TRequest) -> TResponse
    ): TResponse
```
