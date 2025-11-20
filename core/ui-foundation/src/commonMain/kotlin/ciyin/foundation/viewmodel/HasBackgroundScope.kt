package ciyin.foundation.viewmodel

import androidx.annotation.UiThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ciyin.platform.logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

/**
 * 提供用于启动后台任务的后台作用域。
 *
 * [HasBackgroundScope] 还为流提供了各种辅助函数。
 *
 * ## 创建后台作用域
 *
 * 建议使用构造函数式的 [BackgroundScope] 函数来创建后台作用域。
 *
 * 一个特殊的用例是 [AbstractViewModel]，它手动实现了 [HasBackgroundScope] 接口
 * 以符合 Android 生命周期管理。
 *
 * ## 使用示例
 *
 * 推荐的用法是在全局维护的实现 [HasBackgroundScope] 的类中使用它：
 * ```
 * class SessionManagerImpl : HasBackgroundScope by BackgroundScope() {
 * }
 * ```
 *
 * SessionManager 是一个单例，并被注入到其他对象中。
 * 对于 SessionManager 实现来说，后台作用域可以用于启动后台任务。
 *
 * ## 在公共 API 中隐藏 BackgroundScope
 *
 * 建议仅在内部实现中使用 [HasBackgroundScope]，
 * 这样 API 的公共用户就不会看到后台作用域，也不会误用它 - 在用户无法控制的作用域中启动任务是不好的做法。
 */
@Stable
interface HasBackgroundScope {
    /**
     * 用于启动后台任务的后台作用域。
     *
     * 它必须有一个 [SupervisorJob]，以控制结构化并发。
     * 还安装了 [CoroutineExceptionHandler] 以防止应用崩溃。
     */
    val backgroundScope: CoroutineScope

    /**
     * 将_冷_[Flow]转换为在**后台作用域**中启动的_热_[SharedFlow]。
     *
     * ## 流操作中不允许 UI 操作
     *
     * 由于流在后台作用域中启动，因此不得在流操作中执行任何 UI 操作。
     * 所有 UI 操作都将引发异常。
     *
     * ## 懒加载共享
     *
     * 默认情况下，共享**仅在**第一个订阅者出现时启动，在最后一个订阅者消失时立即停止（默认情况下），
     * 永久保留重放缓存（默认情况下）。
     *
     * 如果没有订阅者，则不会收集流。因此，返回的流不会立即有值。
     *
     * @see Flow.shareIn
     */
    fun <T> Flow<T>.shareInBackground(
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
        replay: Int = 1,
    ): SharedFlow<T> = shareIn(backgroundScope, started, replay)

    /**
     * 将_冷_[Flow]转换为在后台作用域中启动的_热_[StateFlow]。
     *
     * ## 流操作中不允许 UI 操作
     *
     * 由于流在后台作用域中启动，因此不得在流操作中执行任何 UI 操作。
     * 所有 UI 操作都将引发异常。
     *
     * ## 懒加载共享
     *
     * 默认情况下，共享**仅在**第一个订阅者出现时启动，在最后一个订阅者消失时立即停止（默认情况下），
     * 永久保留重放缓存（默认情况下）。
     *
     * 如果没有订阅者，则不会收集流。因此，
     * 返回的 [StateFlow] 的 [StateFlow.value] 将保持为 [initialValue]，除非有订阅者。
     *
     * ## `StateFlow.first` 不是订阅者
     *
     * 调用 `StateFlow.first` 不被视为订阅者。因此，在调用 `first` 时，
     * 您将始终获得 `initialValue`，除非流正在被收集。
     *
     * @see Flow.stateIn
     */
    fun <T> Flow<T>.stateInBackground(
        initialValue: T,
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
    ): StateFlow<T> = stateIn(backgroundScope, started, initialValue)

    /**
     * 将_冷_[Flow]转换为在**后台作用域**中启动的_热_[StateFlow]。
     *
     * 返回的 [StateFlow] 初始值为 `null` [StateFlow.value]。
     *
     * ## 流操作中不允许 UI 操作
     *
     * 由于流在后台作用域中启动，因此不得在流操作中执行任何 UI 操作。
     * 所有 UI 操作都将引发异常。
     *
     * ## 懒加载共享
     *
     * 默认情况下，共享**仅在**第一个订阅者出现时启动，在最后一个订阅者消失时立即停止（默认情况下），
     * 永久保留重放缓存（默认情况下）。
     *
     * 如果没有订阅者，则不会收集流。因此，
     * 返回的 [StateFlow] 的 [StateFlow.value] 将保持为 [initialValue]，除非有订阅者。
     *
     * ## `StateFlow.first` 不是订阅者
     *
     * 调用 `StateFlow.first` 不被视为订阅者。因此，在调用 `first` 时，
     * 您将始终获得 `initialValue`，除非流正在被收集。
     *
     * @see Flow.stateIn
     */
    fun <T> Flow<T>.stateInBackground(
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
    ): StateFlow<T?> = stateIn(backgroundScope, started, null)

    private val <T> Flow<T>.valueOrNull: T?
        get() = when (this) {
            is StateFlow<T> -> this.value
            is SharedFlow<T> -> this.replayCache.firstOrNull()
            else -> null
        }

    /**
     * Collects the flow on the main thread into a [State].
     */
    fun <T> Flow<T>.produceState(
        initialValue: T,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
    ): State<T> {
        val state = mutableStateOf(valueOrNull ?: initialValue)
        launchInBackground(coroutineContext) {
            flowOn(Dispatchers.Default) // compute in background
                .collect { value ->
                    withContext(Dispatchers.Main) { // ensure a dispatch happens
                        state.value = value
                    }
                } // update state in main
        }
        return state
    }

    /**
     * Collects the flow on the main thread into a [State].
     */
    fun Flow<Float>.produceState(
        initialValue: Float,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
    ): FloatState {
        val state = mutableFloatStateOf(this.valueOrNull ?: initialValue)
        launchInBackground(coroutineContext) {
            flowOn(Dispatchers.Default) // compute in background
                .collect {
                    withContext(Dispatchers.Main) { // ensure a dispatch happens
                        state.value = it
                    }
                } // update state in main
        }
        return state
    }

    /**
     * Collects the flow on the main thread into a [State].
     */
    fun Flow<Int>.produceState(
        initialValue: Int,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
    ): IntState {
        val state = mutableIntStateOf(this.valueOrNull ?: initialValue)
        launchInBackground(coroutineContext) {
            flowOn(Dispatchers.Default) // compute in background
                .collect {
                    withContext(Dispatchers.Main) { // ensure a dispatch happens
                        state.value = it
                    }
                } // update state in main
        }
        return state
    }

    /**
     * 在主线程上收集流到 [State]。
     */
    fun Flow<Long>.produceState(
        initialValue: Long,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
    ): LongState {
        val state = mutableLongStateOf(this.valueOrNull ?: initialValue)
        launchInBackground(coroutineContext) {
            flowOn(Dispatchers.Default) // compute in background
                .collect {
                    withContext(Dispatchers.Main) { // ensure a dispatch happens
                        state.value = it
                    }
                } // update state in main
        }
        return state
    }

    /**
     * Collects the flow on the main thread into a [State].
     */
    fun <T> StateFlow<T>.produceState(
        initialValue: T = this.value,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
    ): State<T> {
        val state = mutableStateOf(initialValue)
        launchInBackground(coroutineContext) {
            // no need for flowOn as it's SharedFlow
            collect {
                withContext(Dispatchers.Main) {
                    state.value = it
                }
            }
        }
        return state
    }
}

/**
 * 创建一个新的后台作用域。
 *
 * 注意，此函数不适合直接使用。
 * 执行 `BackgroundScope().backgroundScope.launch { }` 是错误的 - 它会导致协程泄漏到一个不受管理的作用域中。
 *
 * @param parentCoroutineContext 传递到后台作用域的父协程上下文。
 * 如果父上下文有 [Job]，作用域将使用它作为父任务。
 *
 * @see HasBackgroundScope
 */
@Suppress("FunctionName")
fun BackgroundScope(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
): HasBackgroundScope = SimpleBackgroundScope(parentCoroutineContext)

/**
 * @param coroutineContext 变化不会反应到返回的 [HasBackgroundScope].
 */
@Composable
inline fun rememberBackgroundScope(
    crossinline coroutineContext: @DisallowComposableCalls () -> CoroutineContext = { EmptyCoroutineContext }
): HasBackgroundScope = remember { RememberedBackgroundScope(coroutineContext()) }

private class SimpleBackgroundScope(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : HasBackgroundScope {
    override val backgroundScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))
}

@PublishedApi
internal class RememberedBackgroundScope(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : HasBackgroundScope, RememberObserver {
    private companion object {
        private val logger = logger("RememberedBackgroundScope")
    }

    private val creationStacktrace =
//        if (BuildConfig.DEBUG) Throwable("Stacktrace for background scope creation") else null
        if (true) Throwable("Stacktrace for background scope creation") else null

    override val backgroundScope: CoroutineScope =
        CoroutineScope(
            CoroutineExceptionHandler { coroutineContext, throwable ->
                if (throwable is CancellationException) return@CoroutineExceptionHandler
                creationStacktrace?.let { throwable.addSuppressed(it) }
                logger.e(throwable) { "An error occurred in the background scope in coroutine $coroutineContext" }
            }.plus(parentCoroutineContext)
                .plus(SupervisorJob(parentCoroutineContext[Job])),
        )

    override fun onAbandoned() {
        backgroundScope.cancel("RememberedBackgroundScope left the composition")
    }

    override fun onForgotten() {
        backgroundScope.cancel("RememberedBackgroundScope left the composition")
    }

    override fun onRemembered() {
    }
}

/**
 * 在后台作用域中启动一个带有加载状态的协程任务。
 *
 * @param isLoadingState 加载状态的可变状态
 * @param context 协程上下文
 * @param start 启动模式
 * @param block 要执行的挂起函数块
 * @return 协程任务
 */
fun <V : HasBackgroundScope> V.launchInBackgroundAnimated(
    isLoadingState: MutableState<Boolean>,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend V.() -> Unit,
): Job {
    isLoadingState.value = true
    return backgroundScope.launch(context, start) {
        block()
        isLoadingState.value = false
    }
}


fun <T> CoroutineScope.deferFlow(value: suspend () -> T): MutableStateFlow<T?> {
    val flow = MutableStateFlow<T?>(null)
    launch {
        flow.value = value()
    }
    return flow
}


/**
 * 在后台作用域中启动一个新的协程任务。
 *
 * 注意，此作用域中不允许 UI 任务。要启动 UI 任务，请使用 [launchInMain]。
 */
fun <V : HasBackgroundScope> V.launchInBackground(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend V.() -> Unit,
): Job {
    return backgroundScope.launch(start = start) {
        block()
    }
}


/**
 * 在后台作用域中启动一个新的协程任务。
 *
 * 注意，此作用域中不允许 UI 任务。要启动 UI 任务，请使用 [launchInMain]。
 */
fun <V : HasBackgroundScope> V.launchInBackground(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend V.() -> Unit,
): Job {
    return backgroundScope.launch(context, start) {
        block()
    }
}

/**
 * 在 UI 作用域中启动一个新的协程任务。
 *
 * 注意，在此作用域中不得执行任何耗时操作，因为这会阻塞 UI。
 * 要执行耗时计算，请使用 [launchInBackground]。
 */
fun <V : HasBackgroundScope> V.launchInMain(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    @UiThread block: suspend V.() -> Unit,
): Job {
    return backgroundScope.launch(context + Dispatchers.Main, start) {
        block()
    }
}

/**
 * 将主线程上的流收集到 [State] 中。
 */
fun <T> Flow<T>.produceState(
    initialValue: T,
    scope: CoroutineScope,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
): State<T> {
    val state = mutableStateOf(initialValue)
    scope.launch(coroutineContext + Dispatchers.Main) {
        flowOn(Dispatchers.Default) // compute in background
            .collect {
                // update state in main
                state.value = it
            }
    }
    return state
}

/**
 * 将主线程上的流收集到 [State] 中。
 */
fun Flow<Float>.produceState(
    initialValue: Float,
    scope: CoroutineScope,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
): FloatState {
    val state = mutableFloatStateOf(initialValue)
    scope.launch(coroutineContext + Dispatchers.Main) {
        flowOn(Dispatchers.Default) // compute in background
            .collect {
                // update state in main
                state.floatValue = it
            }
    }
    return state
}

/**
 * 将主线程上的流收集到 [State] 中。
 */
fun Flow<Int>.produceState(
    initialValue: Int,
    scope: CoroutineScope,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
): IntState {
    val state = mutableIntStateOf(initialValue)
    scope.launch(coroutineContext + Dispatchers.Main) {
        flowOn(Dispatchers.Default) // compute in background
            .collect {
                // update state in main
                state.intValue = it
            }
    }
    return state
}

/**
 * 将主线程上的流收集到 [State] 中。
 */
fun Flow<Long>.produceState(
    initialValue: Long,
    scope: CoroutineScope,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
): LongState {
    val state = mutableLongStateOf(initialValue)
    scope.launch(coroutineContext + Dispatchers.Main) {
        flowOn(Dispatchers.Default) // compute in background
            .collect {
                // update state in main
                state.longValue = it
            }
    }
    return state
}

/**
 * 在主线程上收集流到 [State]。
 */
fun <T> StateFlow<T>.produceState(
    initialValue: T = this.value,
    scope: CoroutineScope,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
): State<T> {
    val state = mutableStateOf(initialValue)
    scope.launch(coroutineContext + Dispatchers.Main) {
        collect {
            // update state in main
            state.value = it
        }
    }
    return state
}
