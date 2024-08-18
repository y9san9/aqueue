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
 * @param key It is guaranteed that requests with the same [key] will be executed consecutively
 * @param context The context that is used to launch new coroutines. You may limit parallelism using context
 * @param queue The queue that is used to parallel requests
 * @param action The action to perform with [request]
 */
@DelicateCoroutinesApi
public fun <TRequest, TResponse> AQueue.Bound.Companion.fixedThreadPool(
    numberOfThreads: Int,
    name: String,
    key: (TRequest) -> Any? = { null },
    context: CoroutineContext = EmptyCoroutineContext,
    queue: AQueue = AQueue(),
    action: suspend (TRequest) -> TResponse,
): AQueue.Bound<TRequest, TResponse> {
    val fixedContext = newFixedThreadPoolContext(numberOfThreads, name)

    return AQueue.Bound(
        key = key,
        context = context + fixedContext,
        queue = queue,
        action = action
    )
}

/**
 * Asynchronous Queue that uses [Dispatchers.IO] to create a queue
 *
 * @param key It is guaranteed that requests with the same [key] will be executed consecutively
 * @param context The context that is used to launch new coroutines. You may limit parallelism using context
 * @param queue The queue that is used to parallel requests
 * @param action The action to perform with [request]
 */
@DelicateCoroutinesApi
public fun <TRequest, TResponse> AQueue.Bound.Companion.io(
    key: (TRequest) -> Any? = { null },
    context: CoroutineContext = EmptyCoroutineContext,
    queue: AQueue = AQueue(),
    action: suspend (TRequest) -> TResponse,
): AQueue.Bound<TRequest, TResponse> {
    return AQueue.Bound(
        key = key,
        context = context + Dispatchers.IO,
        queue = queue,
        action = action
    )
}
