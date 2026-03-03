package ciyin.system.time

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 *
 * 一个简单的计时工具（多平台版本）
 *
 * 用于统计从 [start] 调用以来经过的时间，并提供一组便捷的比较函数。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/2 00:00
 */
class Stopwatch {

    @OptIn(ExperimentalTime::class)
    private var mark = TimeSource.Monotonic.markNow()

    /**
     * 开始（或重新开始）计时
     */
    @OptIn(ExperimentalTime::class)
    fun start() {
        mark = TimeSource.Monotonic.markNow()
    }

    /**
     * 获取从 [start] 以来的耗时
     *
     * @return 耗时 [kotlin.time.Duration]
     */
    @OptIn(ExperimentalTime::class)
    fun elapsed(): Duration = mark.elapsedNow()

    operator fun invoke(): Duration = elapsed()

}