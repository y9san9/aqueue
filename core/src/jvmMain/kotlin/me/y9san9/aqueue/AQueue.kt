package me.y9san9.aqueue

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newFixedThreadPoolContext

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
    return queue.withContext(fixedContext)
}

/**
 * Asynchronous Queue that uses [Dispatchers.IO] to create a queue
 *
 * @param queue The queue that is used to parallel requests
 */
public fun AQueue.Companion.io(queue: AQueue = AQueue()): AQueue {
    return queue.withContext(Dispatchers.IO)
}
