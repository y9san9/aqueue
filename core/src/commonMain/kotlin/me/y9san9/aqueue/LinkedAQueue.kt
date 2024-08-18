package me.y9san9.aqueue

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

/**
 * Creates a default implementation of [AQueue]
 */
public fun <TRequest, TResponse> AQueue(): AQueue<TRequest, TResponse> {
    return LinkedAQueue()
}

/**
 * An implementation of [AQueue] that join jobs discriminated by key.
 * It is kind of like LinkedList works, because every job saves reference
 * to the previous job.
 */
public class LinkedAQueue<TRequest, TResponse> : AQueue<TRequest, TResponse> {
    private val pendingMap = PendingMap()

    /**
     * Executes [request] with fine-grained control over concurrency
     *
     * @param request The request to execute
     * @param key It is guaranteed that requests with the same [key] will be executed consecutively
     * @param context The context that is used to launch new coroutines. You may limit parallelism using context
     * @param action The action to perform with [request]
     */
    override suspend fun execute(
        request: TRequest,
        key: Any?,
        context: CoroutineContext,
        action: suspend (TRequest) -> TResponse
    ): TResponse {
        return coroutineScope {
            val scope = this

            suspendCancellableCoroutine { continuation ->
                launch(start = CoroutineStart.UNDISPATCHED) {
                    pendingMap.putPending(key) { pendingJob ->
                        launch {
                            pendingJob?.join()
                            val result = runCatching { action(request) }
                            pendingMap.finishPendingJob(key, coroutineContext.job)
                            continuation.resumeWith(result)
                        }
                    }
                }
                continuation.invokeOnCancellation { cancellation ->
                    if (cancellation is CancellationException) scope.cancel(cancellation)
                }
            }
        }
    }
}

private class PendingMap {
    val mutex = Mutex()
    val map = mutableMapOf<Any, Job>()

    suspend inline fun putPending(
        key: Any?, block: (Job?) -> Job
    ) {
        mutex.withLock {
            val previous = map[key]
            val pending = block(previous)
            if (key != null) {
                map[key] = pending
            }
        }
    }

    suspend fun finishPendingJob(key: Any?, job: Job) {
        key ?: return
        mutex.withLock {
            val mapJob = map[key]?.job
            if (mapJob === job) map.remove(key)
        }
    }
}
