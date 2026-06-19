package ciyin.ai.integrate.image.internal

import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.random.Random

/**
 * 进程内单调递增的生图聚合调用 ID 生成器。
 */
@OptIn(ExperimentalAtomicApi::class)
internal object InvocationIds {

    /** 当前进程内已分配的调用序列号。 */
    private val counter = AtomicLong(0L)

    /** 当前进程启动后固定的短随机前缀。 */
    private val prefix: String = randomPrefix()

    /**
     * 生成下一个调用关联 ID。
     */
    fun next(): String {
        val seq = counter.incrementAndFetch()
        return "$prefix-${seq.toString(36)}"
    }

    /**
     * 生成便于日志识别的短随机前缀。
     */
    private fun randomPrefix(): String {
        val chars = ('a'..'z') + ('0'..'9')
        val tail = (1..6).map { chars[Random.nextInt(chars.size)] }.joinToString("")
        return "ai-image-$tail"
    }
}
