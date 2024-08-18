package me.y9san9.aqueue

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Asynchronous Queue with fine-grained control over concurrency
 */
public interface AQueue<TRequest, TResponse> {

    /**
     * Executes [request] with fine-grained control over concurrency
     *
     * @param request The request to execute
     * @param key It is guaranteed that requests with the same [key] will be executed consecutively
     * @param context The context that is used to launch new coroutines. You may limit parallelism using context
     * @param action The action to perform with [request]
     */
    public suspend fun execute(
        request: TRequest,
        key: Any? = null,
        context: CoroutineContext = EmptyCoroutineContext,
        action: suspend (TRequest) -> TResponse
    ): TResponse

    /**
     * Asynchronous Queue that has all parameters provided except
     * of the request itself.
     *
     * @param key It is guaranteed that requests with the same [key] will be executed consecutively
     * @param context The context that is used to launch new coroutines. You may limit parallelism using context
     * @param queue The queue that is used to parallel requests
     * @param action The action to perform with request
     */
    public class Bound<TRequest, TResponse>(
        private val key: (TRequest) -> Any? = { null },
        private val context: CoroutineContext = EmptyCoroutineContext,
        private val queue: AQueue<TRequest, TResponse> = AQueue(),
        private val action: suspend (TRequest) -> TResponse,
    ) {
        /**
         * Executes [request] with fine-grained control over concurrency
         */
        public suspend fun execute(
            request: TRequest,
            key: Any? = this.key(request),
            context: CoroutineContext = this.context
        ): TResponse {
            return queue.execute(request, key, context, action)
        }

        public fun copy(
            key: (TRequest) -> Any? = this.key,
            context: CoroutineContext = this.context,
            queue: AQueue<TRequest, TResponse> = this.queue,
            action: suspend (TRequest) -> TResponse = this.action,
        ): Bound<TRequest, TResponse> {
            return Bound(key, context, queue, action)
        }

        public companion object
    }

    public companion object
}
