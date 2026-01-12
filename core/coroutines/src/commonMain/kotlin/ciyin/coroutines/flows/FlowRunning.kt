package coroutines.flows

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Flow 是否正在运行中的监听器.
 *
 * 可用于搜索时配合 [combine] 使用, 搜索结束后自动触发更新.
 * @see withRunning
 */
class FlowRunning {
    @Suppress("PropertyName")
    @PublishedApi
    internal val _isRunning: MutableStateFlow<Int> = MutableStateFlow<Int>(0)

    /**
     * 是否有至少一个 [withRunning] 正在运行.
     */
    val isRunning = _isRunning.map { it > 0 }

    /**
     * 执行 [block], 并在执行期间将 [isRunning] 设置为 `true`, 执行完毕后设置为 `false`.
     *
     * 此函数线程是线程安全的. 多个线程/协程同时调用 [withRunning], [isRunning] 将保持为 `true`, 直到它们全部结束运行.
     */
    inline fun <R> withRunning(block: () -> R): R {
        _isRunning.update { it + 1 }
        try {
            return block()
        } finally {
            _isRunning.update { it - 1 }
        }
    }
}
