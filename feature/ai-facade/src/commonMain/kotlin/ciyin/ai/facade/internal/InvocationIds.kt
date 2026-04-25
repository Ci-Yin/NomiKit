package ciyin.ai.facade.internal

import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.random.Random

/**
 * 进程内单调递增的 invocation id 生成器。
 *
 * commonMain 没有 `UUID`，这里用"启动时随机前缀 + 单调递增序列"作为可读 id：
 * - 跨 listener 关联用：足够；
 * - 跨进程持久化：**不**适合。上层若要长期归档应自己生成真正的 UUID。
 */
@OptIn(ExperimentalAtomicApi::class)
internal object InvocationIds {

    private val counter = AtomicLong(0L)
    private val prefix: String = randomPrefix()

    fun next(): String {
        val seq = counter.incrementAndFetch()
        return "$prefix-${seq.toString(36)}"
    }

    private fun randomPrefix(): String {
        val chars = ('a'..'z') + ('0'..'9')
        val tail = (1..6).map { chars[Random.nextInt(chars.size)] }.joinToString("")
        return "ai-$tail"
    }
}
