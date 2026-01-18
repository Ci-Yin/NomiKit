package ciyin.ui.foundation.viewmodel

import com.freeletics.flowredux2.FlowReduxBuilder
import com.freeletics.flowredux2.FlowReduxStateMachine
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext


/**
 * 基于 MVI（Model-View-Intent）架构的 ViewModel 基类，结合状态机 (StateMachine) 以及流式处理，负责管理
 * 状态、动作分发和副作用。
 *
 * @param S 表示状态的类型。
 * @param A 表示动作（Action）的类型，用于触发状态转换。
 * @param E 表示副作用（Side Effect）的类型。
 *
 * 此类扩展了 [AbstractViewModel] 提供的生命周期管理能力，并实现了 [MviViewModel] 接口，
 * 用于面向用户界面的状态管理及意图处理。
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class StateMachine<S : Any, A : Any, E : Any>(scope: CoroutineScope) :
    MviViewModel<S, A, E> {

    /**
     * `stateMachine` 是一个延迟初始化的状态机实例，用于管理状态流和动作分发机制。
     * 它基于 FlowRedux 定义，通过 `specBlock` 配置状态转换逻辑。
     *
     * 特性:
     * - 延迟初始化，只有在首次访问时实例化。
     * - 自动绑定到 ViewModel 的 `viewModelScope`，生命周期由 ViewModel 控制。
     * - 定义与 State 和 Action 类型泛型相关的状态转换逻辑。
     *
     * 使用场景:
     * - 在 Model-View-Intent (MVI) 架构中，用于处理状态更新和动作分发。
     * - 管理复杂业务逻辑的状态流和状态变化。
     */
    private val stateMachine: FlowReduxStateMachine<StateFlow<S>, A> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        object : FlowReduxStateMachineFactory<S, A>() {
            init {
                initialize()
                spec { spec() }
            }
        }.launchIn(scope)
    }

    /**
     * 表示当前 ViewModel 的状态流，这是一个不可变的状态流（StateFlow）。
     *
     * - 此状态流通过内部的状态机（StateMachine）进行管理。
     * - 主要用于向 UI 层提供实时的状态更新，符合 MVI 架构的单一数据源模式。
     * - `state` 始终包含最新的状态，订阅者可接收即时的状态变更通知。
     *
     * @return 返回类型为 `StateFlow<S>`，表示由 ViewModel 持有和管理的状态数据流。
     */
    override val state: StateFlow<S> get() = stateMachine.state

    /**
     * 一个高阶函数，用于分发类型为 `A` 的动作，在视图模型中触发相应的状态转化逻辑。
     *
     * 此变量通过 `stateMachine` 的 `dispatchAction` 实现，直接引用状态机的动作分发机制。
     *
     * - 适用于接收来自用户界面或外部的动作，并将其交由状态机处理。
     * - 作为 Model-View-Intent (MVI) 框架的一部分，确保动作被正确分发和处理。
     *
     * 动作参数 `A` 通常用于描述用户事件或需要引发状态变化的操作，通过状态机实现业务逻辑与状态流转。
     */
    override val dispatchAction: (A) -> Unit get() = stateMachine.dispatchAction

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
     * 初始化状态机，并配置状态机的规范内容。
     *
     * @param specBlock 用于配置状态机的高阶函数。在此函数中可以定义状态机的多个状态及动作的转换逻辑。
     */
    abstract fun FlowReduxStateMachineFactory<S, A>.initialize()

    /**
     * 配置状态机的规范内容。用于定义状态机的状态转换逻辑。
     *
     * @param specBlock 用于配置状态机的高阶函数。在此函数中可以定义状态机的多个状态及动作的转换逻辑。
     */
    abstract fun FlowReduxBuilder<S, A>.spec()


    /**
     * 触发副作用并将其分发到副作用流中。
     *
     * @param effect 需要触发的副作用实例，表示某种特定的事件或状态更新。
     */
    override suspend fun poseEffect(effect: E) = withContext(Dispatchers.Default) {
        _sideEffects.emit(effect)
    }


    /**
     * 尝试触发一个副作用事件并将其分发到副作用流中。
     *
     * @param effect 需要触发的副作用实例，表示某种特定的事件或状态更新。
     * @return 如果成功触发并分发了副作用，返回 true；否则返回 false。
     */
    override fun tryPoseEffect(effect: E): Boolean = _sideEffects.tryEmit(effect)

}