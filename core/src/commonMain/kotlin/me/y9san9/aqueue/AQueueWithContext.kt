package me.y9san9.aqueue

import kotlin.coroutines.CoroutineContext

public fun AQueue.withContext(context: CoroutineContext): AQueue {
    return WithContextAQueue(context, upstream = this)
}

private class WithContextAQueue(
    private val context: CoroutineContext,
    private val upstream: AQueue
) : AQueue {
    override suspend fun <T> execute(key: Any?, context: CoroutineContext, block: suspend () -> T): T {
        return upstream.execute(
            key = key,
            context = this.context + context,
            block = block
        )
    }
}
