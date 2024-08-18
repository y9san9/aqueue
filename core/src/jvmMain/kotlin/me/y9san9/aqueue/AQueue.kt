package me.y9san9.aqueue

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Asynchronous Queue that uses [newFixedThreadPoolContext] to create a queue
 *
 * @param numberOfThreads The number of threads for [newFixedThreadPoolContext]
 * @param name The name for [newFixedThreadPoolContext]
 * @param queue The queue that is used to parallel requests
 */
@DelicateCoroutinesApi
public fun AQueue.Companion.fixedThreadPool(
    numberOfThreads: Int,
    name: String,
    queue: AQueue = AQueue()
): AQueue {
    val fixedContext = newFixedThreadPoolContext(numberOfThreads, name)

    return object : AQueue {
        override suspend fun <T> execute(key: Any?, context: CoroutineContext, action: suspend () -> T): T {
            return queue.execute(
                key = key,
                context = context + fixedContext,
                action = action
            )
        }
    }
}

/**
 * Asynchronous Queue that uses [Dispatchers.IO] to create a queue
 *
 * @param queue The queue that is used to parallel requests
 */
@DelicateCoroutinesApi
public fun AQueue.Companion.io(queue: AQueue = AQueue()): AQueue {
    return object : AQueue {
        override suspend fun <T> execute(key: Any?, context: CoroutineContext, action: suspend () -> T): T {
            return queue.execute(
                key = key,
                context = context + Dispatchers.IO,
                action = action
            )
        }
    }
}
