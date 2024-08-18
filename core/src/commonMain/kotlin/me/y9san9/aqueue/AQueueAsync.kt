package me.y9san9.aqueue

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Launches [block] with fine-grained control over concurrency.
 *
 * @param scope The scope used to launch a coroutine
 * @param start The start mode used to launch a coroutine
 * @param key It is guaranteed that requests with the same [key] will be executed consecutively
 * @param context The context that is used to launch new coroutines. You may limit parallelism using context
 * @param block The action to perform
 */
public fun <T> AQueue.async(
    scope: CoroutineScope,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    key: Any? = null,
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend () -> T
): Deferred<T> {
    return scope.async(start = start) {
        execute(key, context, block)
    }
}
