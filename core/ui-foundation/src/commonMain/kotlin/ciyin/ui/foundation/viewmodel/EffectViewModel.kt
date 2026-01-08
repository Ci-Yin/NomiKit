package ciyin.ui.foundation.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.SharedFlow

interface EffectViewModel<E : Any> {

    /**
     * 表示副作用的流 (Flow)，用于在状态管理或响应用户操作时将某些事件广播给观察者。
     *
     * 典型用例包括导航命令、消息提示或其他不直接修改状态的行为。
     *
     *
     * 类型参数 E 代表副作用事件的具体类型。
     */
    val sideEffects: SharedFlow<E>

    /**
     * 触发副作用并将其分发到副作用流中。
     *
     * @param effect 需要触发的副作用实例，表示某种特定的事件或状态更新。
     */
    suspend fun poseEffect(effect: E)


    /**
     * 尝试触发一个副作用事件并将其分发到副作用流中。
     *
     * @param effect 需要触发的副作用实例，表示某种特定的事件或状态更新。
     * @return 如果成功触发并分发了副作用，返回 true；否则返回 false。
     */
    fun tryPoseEffect(effect: E): Boolean

}

@Composable
@Suppress("ComposableNaming")
inline fun <E : Any> EffectViewModel<E>.collectSideEffects(
    crossinline onEffect: suspend (E) -> Unit
) {
    LaunchedEffect(this) {
        sideEffects.collect { effect ->
            onEffect(effect)
        }
    }
}