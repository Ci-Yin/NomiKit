package ciyin.ui.foundation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ciyin.ui.foundation.savedstate.createSavedMutableStateFlow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 表示一个抽象的 Model-View-Intent (MVI) 架构的 ViewModel 基类。
 *
 * 提供状态流 (StateFlow) 和副作用流 (SharedFlow) 的管理功能，并定义如何触发副作用，
 * 用于管理状态及事件的单向数据流体系。
 *
 * @param S 表示 ViewModel 持有的状态 (State) 类型。
 * @param A 表示触发状态改变的动作 (Action) 类型。
 * @param E 表示产生的副作用 (Effect) 类型。
 */
abstract class AbsMviViewModel<S : Any, A : Any, E : Any>(
    savedStateHandle: SavedStateHandle?,
) : AbstractViewModel(),
    MviViewModel<S, A, E> {

    protected abstract val initState: S

    private val stateMachine by lazy {
        val initialValue = initState
        val stateFlow = savedStateHandle?.createSavedMutableStateFlow(
            scope = viewModelScope,
            key = "${this::class.qualifiedName}.state",
            initialValue = initialValue
        ) ?: MutableStateFlow(initialValue)
        SingleStateMachine<S, A>(
            state = stateFlow,
            scope = viewModelScope
        ).apply { spec() }
    }

    // 对外只读状态
    override val state: StateFlow<S> by lazy { stateMachine.state.asStateFlow() }

    protected val stateValue: S get() = state.value

    override val dispatchAction: (A) -> Unit get() = stateMachine::action

    /**
     * 表示一个可变的副作用流 (MutableSharedFlow)，此变量用于在 ViewModel 内部存储和管理副作用事件。
     *
     * - 类型参数 E 代表副作用事件的类型。
     * - `_sideEffects` 是 `sideEffects` 的实际存储实现，它是一个内部可变的 `SharedFlow`。
     * - 典型用例包括广播导航事件、消息提示等不会直接修改状态的行为。
     * - 该流是 ViewModel 内部使用，外部只能通过只读的 `sideEffects` 流访问其值。
     */
    private val _sideEffects = MutableSharedFlow<E>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * 表示一个共享的副作用事件流 (Flow) 的只读属性，用于外部订阅和监听 ViewModel 中产生的副作用事件。
     *
     * - 副作用事件通常用于通知视图层执行导航、提示信息或其他与状态无关的操作。
     * - 内部通过 `MutableSharedFlow` 实现，确保事件的安全分发和管理。
     * - 该属性通过 `asSharedFlow` 转换为只读形式，从而保护内部事件流免受外部直接修改。
     *
     * 类型参数 `E` 表示副作用事件的具体类型。
     */
    override val sideEffects = _sideEffects.asSharedFlow()

    /**
     * 触发副作用并将其分发到副作用流中。
     *
     * @param effect 需要触发的副作用实例，表示某种特定的事件或状态更新。
     */
    override suspend fun poseEffect(effect: E) {
        _sideEffects.emit(effect)
    }


    /**
     * 尝试触发一个副作用事件并将其分发到副作用流中。
     *
     * @param effect 需要触发的副作用实例，表示某种特定的事件或状态更新。
     * @return 如果成功触发并分发了副作用，返回 true；否则返回 false。
     */
    override fun tryPoseEffect(effect: E): Boolean = _sideEffects.tryEmit(effect)

    @SingleStateMachineDsl
    suspend fun SingleStateMachine<S, A>.effect(effect: E) {
        poseEffect(effect)
    }

    /**
     * 配置状态机的规范内容。用于定义状态机的状态转换逻辑。
     */
    abstract fun SingleStateMachine<S, A>.spec()

}

