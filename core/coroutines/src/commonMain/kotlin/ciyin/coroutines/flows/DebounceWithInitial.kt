package ciyin.coroutines.flows

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlin.concurrent.Volatile
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 等同于 [debounce], 但是会直接 emit 第一个值, 随后再开始 debounce.
 *
 * 适用于 StateFlow 情况. 这可以让 collector StateFlow 的丢一个
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("debounceWithInitialDuration")
fun <T> Flow<T>.debounceWithInitial(
    timeout: () -> Duration,
): Flow<T> {
    val isInitial = object {
        @Volatile
        var value = true
    }
    return debounce {
        if (isInitial.value) {
            isInitial.value = false
            Duration.ZERO
        } else {
            timeout()
        }
    }
}

/**
 * @see debounceWithInitial
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
fun <T> Flow<T>.debounceWithInitial(timeoutMillis: () -> Long): Flow<T> =
    debounceWithInitial { timeoutMillis().milliseconds }

/**
 * @see debounceWithInitial
 */
fun <T> Flow<T>.debounceWithInitial(timeout: Duration): Flow<T> =
    debounceWithInitial { timeout }

/**
 * @see debounceWithInitial
 */
fun <T> Flow<T>.debounceWithInitial(timeoutMillis: Long): Flow<T> =
    debounceWithInitial { timeoutMillis.milliseconds }
