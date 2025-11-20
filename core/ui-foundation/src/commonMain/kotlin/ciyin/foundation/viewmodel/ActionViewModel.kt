package ciyin.foundation.viewmodel

import kotlin.reflect.KClass


/**
 *
 * kotlin接口作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/13 19:32
 * @version: 1.0
 */
interface ActionViewModel<A : Any> {

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

    operator fun invoke(action: A): () -> Unit = {
        dispatchAction(action)
    }

    fun <A : Any> on(
        actionClass: KClass<A>,
        handler: suspend (action: A) -> Unit,
    ) {

    }

}

