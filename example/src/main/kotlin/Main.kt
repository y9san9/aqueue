import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import me.y9san9.aqueue.AQueue
import me.y9san9.aqueue.fixedThreadPool
import me.y9san9.aqueue.flow.mapInAQueue
import kotlin.time.measureTime

@OptIn(DelicateCoroutinesApi::class)
suspend fun main() {
    val natural = flow {
        var number = 0
        while (true) emit(number++)
    }.take(count = 10)

    // This will be executed in roughly 0.1 seconds,
    // because every key is unique.
    // Every action will run in parallel.
    val duration1 = measureTime {
        natural.mapInAQueue(
            key = { it },
            transform = { delay(100); it }
        ).collect()
    }
    println("First: $duration1")

    // This will be executed in roughly 1 second,
    // because all keys are the same.
    // Every action will run consecutively.
    val duration2 = measureTime {
        natural.mapInAQueue(
            key = { Unit },
            transform = { delay(100); it }
        ).collect()
    }
    println("Second: $duration2")

    // This will be executed in roughly 5 seconds,
    // because the key is either 0 or 1.
    // There would be 2 consecutive queues:
    // - For even numbers
    // - For odd numbers
    // Two queues cut time from 1 second to 0.5 seconds
    val duration3 = measureTime {
        natural.mapInAQueue(
            key = { it % 2 },
            transform = { delay(100); it }
        ).collect()
    }
    println("Third: $duration3")

    // This will be executed in roughly 1 second because of single-threaded pool
    val singleThreadedQueue = AQueue.fixedThreadPool(numberOfThreads = 1, name = "Test")

    val duration4 = measureTime {
        natural.mapInAQueue(
            queue = singleThreadedQueue,
            transform = {
                Thread.sleep(100)
                it
            }
        ).collect()
    }
    println("Third: $duration4")
}
