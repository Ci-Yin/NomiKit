package ciyin.ui.foundation.viewmodel

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 表示一个 Model-View-Intent (MVI) 架构的 ViewModel 接口。
 *
 * @param S 表示 ViewModel 的状态类型。
 * @param A 表示能够触发状态改变的动作（Action）类型。
 * @param E 表示 ViewModel 中产生的副作用（Side Effect）类型。
 */
interface MviViewModel<S : Any, A : Any, E : Any> : EffectViewModel<E>,
    StateViewModel<S> {


    override val state: StateFlow<S>

    /**
     * 一个高阶函数，接受类型为 `A` 的参数并执行特定的动作。
     * 用于分发用户操作、事件或动作到 ViewModel 内部的状态机进行处理。
     *
     * - 属于 `MviViewModel` 接口（Model-View-Intent 架构的一部分）。
     * - 通过 `StateMachine` 内部实现，每次调用时触发对应的状态转换逻辑。
     *
     * 使用场景包括处理来自视图层的动作，将其传递给 ViewModel 来更新状态或触发副作用。
     */
    val dispatchAction: (A) -> Unit

    override val sideEffects: SharedFlow<E>

    operator fun invoke(action: A) {
        dispatchAction(action)
    }

}
