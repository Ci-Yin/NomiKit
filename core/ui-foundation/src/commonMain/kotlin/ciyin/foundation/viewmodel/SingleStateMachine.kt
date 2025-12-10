package ciyin.foundation.viewmodel

import ciyin.platform.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@SsmDsl
class SingleStateMachine<S : Any, A : Any>(
    val state: MutableStateFlow<S>,
    private val scope: CoroutineScope
) {
    private val logger = logger("SingleStateMachine")

    /**
     * 使用 SharedFlow 来分发 Action，保证所有订阅者都能“看到”同一条 Action。
     *
     * 之前使用 Channel + receiveAsFlow 的实现会导致：
     * - 多个 collector 之间会竞争消费同一个 Channel 中的元素；
     * - 从而导致某些 Action 只会被某一个订阅者处理，而不是所有符合条件的订阅者都处理。
     *
     * 对于 MVI 的 Action 分发，一般期望是「广播」（publish-subscribe）语义，
     * 因此这里改为 SharedFlow 以保证每个 on(...) 订阅都能独立匹配并处理同一条 Action。
     */
    private val actions = MutableSharedFlow<A>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val snapshot: S get() = state.value

    fun action(action: A) {
        scope.launch {
            logger.i { "dispatchAction $action" }
            actions.emit(action)
        }
    }

    /**
     * Triggers every time the state machine enters this state. The passed [flow] will be collected
     * and any emission will be passed to [handler].
     *
     * The collection as well as any ongoing [handler] is cancelled when leaving this state.
     */
    fun <T> collectWhileInState(
        flow: Flow<T>,
        handler: suspend (item: T) -> Unit,
    ) {
        flow.onEach(handler).launchIn(scope)
    }

    fun onEnter(handler: suspend () -> Unit) {
        scope.launch { handler() }
    }

    @Suppress("UNCHECKED_CAST")
    fun <SubAction : A> on(
        actionClass: KClass<SubAction>,
        handler: suspend (action: SubAction) -> Unit,
    ) {
        actions.filter(actionClass::isInstance)
            .onEach { action ->
                handler(action as SubAction)
            }.launchIn(scope)
    }

    inline fun <reified SubAction : A> on(
        noinline handler: suspend (action: SubAction) -> Unit,
    ) = on(SubAction::class, handler)

    /**
     * 基于当前状态做不可变更新。
     * 注意：transform 应该是快速且纯函数，避免重活。
     */
    fun update(transform: S.() -> S) {
        state.value = transform(state.value)
    }

}

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
internal annotation class SsmDsl