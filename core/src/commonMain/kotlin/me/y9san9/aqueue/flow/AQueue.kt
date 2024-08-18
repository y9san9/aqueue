package me.y9san9.aqueue.flow

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import me.y9san9.aqueue.AQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Parallels flow with fine-grained control using [AQueue.Bound] queue.
 *
 * @param queue The queue used to parallel given flow
 */
public fun <TRequest, TResponse> Flow<TRequest>.mapInAQueue(
    queue: AQueue.Bound<TRequest, TResponse>
): Flow<TResponse> {
    return channelFlow {
        collect { request ->
            launch(start = CoroutineStart.UNDISPATCHED) {
                val result = queue.execute(request)
                send(result)
            }
        }
    }
}

/**
 * Constructs new [AQueue.Bound] and parallels flow using it.
 *
 * @param key It is guaranteed that requests with the same [key] will be executed consecutively
 * @param context The context that is used to launch new coroutines. You may limit parallelism using context
 * @param queue The queue used to parallel flow
 * @param action The action to perform with request
 */
public fun <TRequest, TResponse> Flow<TRequest>.mapInAQueue(
    key: (TRequest) -> Any? = { null },
    context: CoroutineContext = EmptyCoroutineContext,
    queue: AQueue<TRequest, TResponse> = AQueue(),
    action: suspend (TRequest) -> TResponse,
): Flow<TResponse> {
    val bound = AQueue.Bound(key, context, queue, action)
    return mapInAQueue(bound)
}
