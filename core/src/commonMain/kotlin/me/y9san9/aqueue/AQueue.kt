package me.y9san9.aqueue

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Asynchronous Queue with fine-grained control over concurrency
 */
public interface AQueue {

    /**
     * Executes [block] with fine-grained control over concurrency
     *
     * @param key It is guaranteed that requests with the same [key] will be executed consecutively
     * @param context The context that is used to launch new coroutines. You may limit parallelism using context
     * @param block The action to perform
     */
    public suspend fun <T> execute(
        key: Any? = null,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend () -> T
    ): T

    public companion object
}
