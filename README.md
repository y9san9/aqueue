# aqueue

> Asynchronous Queue with fine-grained control over concurrency

Useful for cases when you need to combine asynchronous 
and synchronous behaviour in your services.

A good example of such behaviour is processing messages in
telegram bot. Messages in such case should be processed 
in parallel. However, if there are multiple messages received
from the same user, they must be processed consequently.

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

## Flow Extensions

`AQueue` might be very useful when working with flows, because there is no
quite good API in `kotlinx.coroutines` to parallel Flows based on `key`,
but with this library it is very simple

```kotlin
// Parallel upstream and return results in a flow
val flow: Flow<B> = upstream.mapInAQueue(key = { optional }) { loadSomething(...) }

// Parallel upstream and return Job
val job: Job = upstream.launchInAQueue(key = { optional }) { loadSomething(...) }
```

## Coroutine Builder Extensions

You may often want to `launch` execution of `AQueue` or use it with `async`
which is also supported by the library:

```kotlin
val queue = AQueue()
queue.launch(scope, key = optional) { loadSomething(...) }
queue.async(scope, key = optional) { loadSomething(...) }
```

## Dispatchers Integration

It is possible to use concurrent `Dispatchers` to construct AQueue.
If you are on JVM, use will have the following utility functions:

```kotlin
val queue = AQueue.io() // Constructs AQueue that launches new coroutines on Dispatchers.IO
val queue = AQueue.fixedThreadPool(12) // Constructs AQueue that has a pool of 12 threads
```
