package ciyin.coroutines.flows

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <T, R> FlowCollector<List<T>>.runOrEmitEmptyList(block: () -> R): R {
    try {
        return block()
    } catch (e: Throwable) {
        emit(emptyList())
        throw e
    }
}

/**
 * 将 [this] 的成功结果封装为 [Result.success], 异常结果封装为 [Result.failure].
 * 于是, 返回的 flow 将不会抛出异常 (除了 [CancellationException]).
 */
fun <T> Flow<T>.catching(): Flow<Result<T>> = map {
    Result.success(it)
}.catch {
    if (it is CancellationException) {
        throw it
    }

    emit(Result.failure(it))
}

/**
 * 类似于 [shareIn], 但是会将上游异常传递给下游.
 *
 * 注意, 如果 [started] 配置为 [SharingStarted.WhileSubscribed] 而其 `replayExpirationMillis != 0` 并且 [replay] != 0,
 * 则每次重新 collect 都会传递上次发生的异常, 而导致 flow 永远不会 restart.
 */
fun <T> Flow<T>.shareTransparentlyIn(
    scope: CoroutineScope,
    started: SharingStarted,
    replay: Int = 0,
) = this
    .catching()
    .shareIn(
        scope, started, replay,
    )
    .map {
        it.getOrThrow() // 透明异常. 上游的异常传递给下游
    }
