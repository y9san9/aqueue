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

Replace $version with the latest version from `Releases` Tab.

```kotlin
dependencies {
    implementation("me.y9san9.aqueue:core:$version")
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

    // This will be executed in roughly 1 second because of single-threaded pool
    val singleThreadedQueue = AQueue.fixedThreadPool(numberOfThreads = 1, name = "Test")
    
    natural.mapInAQueue(
        queue = singleThreadedQueue,
        action = {
            Thread.sleep(100)
            it
        }
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
interface AQueue {

    /**
     * Executes [block] with fine-grained control over concurrency
     *
     * @param key It is guaranteed that requests with the same [key] will be executed consecutively
     * @param context The context that is used to launch new coroutines. You may limit parallelism using context
     * @param block The action to perform
     */
    suspend fun <T> execute(
        key: Any? = null,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend () -> T
    ): T
```
