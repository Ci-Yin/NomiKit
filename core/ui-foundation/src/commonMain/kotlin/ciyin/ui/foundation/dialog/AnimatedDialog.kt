package ciyin.ui.foundation.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlin.math.roundToInt

/**
 * 带有进入和退出动画效果的 Dialog 组件。
 *
 * @param visible 控制 Dialog 的可见性
 * @param onDismissRequest 当用户请求关闭 Dialog 时的回调
 * @param properties Dialog 的属性配置
 * @param enter 进入动画，默认为淡入 + 从下方滑入
 * @param exit 退出动画，默认为淡出 + 向下滑出
 * @param onEnterAnimationFinished 进入动画完成时的回调，可选
 * @param onExitAnimationFinished 退出动画完成时的回调，可选
 * @param content Dialog 的内容
 */
@Composable
fun AnimatedDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    enter: EnterTransition = fadeIn() + slideInVertically { (it * 0.2).roundToInt() },
    exit: ExitTransition = fadeOut() + slideOutVertically { (it * 0.2).roundToInt() },
    onEnterAnimationFinished: (() -> Unit)? = null,
    onExitAnimationFinished: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val transitionState = remember { MutableTransitionState(false) }
    LaunchedEffect(visible) { transitionState.targetState = visible }
    // 监听动画完成状态
    LaunchedEffect(Unit) {
        snapshotFlow { transitionState.isIdle to transitionState.currentState }
            .filter { (isIdle, _) -> isIdle }
            .drop(1) // 跳过初始状态
            .collect { (_, currentState) ->
                if (currentState) {
                    onEnterAnimationFinished?.invoke()
                } else {
                    onExitAnimationFinished?.invoke()
                }
            }
    }

    if (transitionState.currentState || !transitionState.isIdle) {
        Dialog(onDismissRequest = onDismissRequest, properties = properties) {
            AnimatedVisibility(
                modifier = Modifier,
                visibleState = transitionState,
                enter = enter,
                exit = exit
            ) {
                content()
            }
        }
    }
}
