package me.y9san9.aqueue.flow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.y9san9.aqueue.AQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Parallels flow using provided [queue]
 *
 * @param key It is guaranteed that requests with the same [key] will be executed consecutively
 * @param context The context that is used to launch new coroutines. You may limit parallelism using context
 * @param queue The queue used to parallel flow
 * @param recover The action to perform in case of exception
 * @param transform The action to perform with request
 */
public fun <T, R> Flow<T>.mapInAQueue(
    queue: AQueue = AQueue(),
    key: suspend (T) -> Any? = { null },
    context: CoroutineContext = EmptyCoroutineContext,
    recover: suspend FlowCollector<R>.(Throwable) -> Unit = { throw it },
    transform: suspend (T) -> R
): Flow<R> {
    return channelFlow {
        collect { element ->
            launch(start = CoroutineStart.UNDISPATCHED) {
                runCatching {
                    queue.execute(key(element), context) { transform(element) }
                }.onFailure { throwable ->
                    ensureActive()
                    val flow = flow { recover(throwable) }
                    flow.collect(::send)
                }.onSuccess { element ->
                    send(element)
                }
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
 * @param recover The action to perform in case of exception
 * @param block The action to perform with request
 */
public fun <T> Flow<T>.launchInAQueue(
    scope: CoroutineScope,
    queue: AQueue = AQueue(),
    key: suspend (T) -> Any? = { null },
    context: CoroutineContext = EmptyCoroutineContext,
    recover: suspend (Throwable) -> Unit = { throw it },
    block: suspend (T) -> Unit
): Job {
    return mapInAQueue(queue, key, context, { throwable -> recover(throwable) }, block).launchIn(scope)
}
