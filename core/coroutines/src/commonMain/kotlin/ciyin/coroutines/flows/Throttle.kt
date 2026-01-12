package ciyin.coroutines.flows

import ciyin.platform.time.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

/**
 * 对 Flow 进行节流操作，仅允许在指定时间窗口内发射最近的一个值。
 *
 * @param timeWindow 一个无参数函数，返回一个 Duration 值，表示时间窗口的持续时间。
 * @return 返回一个经过节流处理后的 Flow。
 */
fun <T> Flow<T>.throttle(timeWindow: () -> Duration): Flow<T> = flow {
    var lastTime = 0L
    val inWholeMilliseconds = timeWindow().inWholeMilliseconds
    collect {
        if (currentTimeMillis() - lastTime >= inWholeMilliseconds) {
            emit(it)
            // 重新获取时间，考虑 emit 可能的耗时
            lastTime = currentTimeMillis()
        }
    }
}

/**
 * 对流中的数据进行限流操作，只在指定的时间窗口内发射数据，忽略窗口内的多余数据。
 *
 * @param timeWindow 限流的时间窗口时长
 * @return 一个新的流，该流仅在每个时间窗口内发射一个数据项
 */
fun <T> Flow<T>.throttle(timeWindow: Duration): Flow<T> = throttle { timeWindow }