package me.y9san9.aqueue.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.y9san9.aqueue.AQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Parallels flow using provided [queue]
 *
 * @param key It is guaranteed that requests with the same [key] will be executed consecutively
 * @param context The context that is used to launch new coroutines. You may limit parallelism using context
 * @param queue The queue used to parallel flow
 * @param action The action to perform with request
 */
public fun <T, R> Flow<T>.mapInAQueue(
    queue: AQueue = AQueue(),
    key: suspend (T) -> Any? = { null },
    context: CoroutineContext = EmptyCoroutineContext,
    action: suspend (T) -> R,
): Flow<R> {
    return channelFlow {
        collect { element ->
            launch(start = CoroutineStart.UNDISPATCHED) {
                val result = queue.execute(key(element), context) { action(element) }
                send(result)
            }
        }
    }
}

/**
 * Parallels flow using provided [queue] and launches it
 *
 * @param scope The scope used to launch flow
 * @param key It is guaranteed that requests with the same [key] will be executed consecutively
 * @param context The context that is used to launch new coroutines. You may limit parallelism using context
 * @param queue The queue used to parallel flow
 * @param action The action to perform with request
 */
public fun <T> Flow<T>.launchInAQueue(
    scope: CoroutineScope,
    queue: AQueue = AQueue(),
    key: suspend (T) -> Any? = { null },
    context: CoroutineContext = EmptyCoroutineContext,
    action: suspend (T) -> Unit
): Job {
    return mapInAQueue(queue, key, context, action).launchIn(scope)
}
