package ciyin.video.player.ui.gesture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ciyin.ui.foundation.effects.ComposeKey
import ciyin.ui.foundation.effects.onKey

/** 保存键盘水平跳转的前进与后退回调。 */
@Stable
class KeyboardHorizontalDirectionState(
    /** 向后跳转回调。 */
    val onBackward: () -> Unit,
    /** 向前跳转回调。 */
    val onForward: () -> Unit,
)


/** 使用状态对象为组件注册水平跳转快捷键。 */
fun Modifier.onKeyboardHorizontalDirection(
    state: KeyboardHorizontalDirectionState,
): Modifier = onKeyboardHorizontalDirection(
    onBackward = state.onBackward,
    onForward = state.onForward,
)

/** 为组件注册符合布局方向的水平跳转快捷键。 */
fun Modifier.onKeyboardHorizontalDirection(
    onBackward: () -> Unit,
    onForward: () -> Unit,
): Modifier = composed(
    inspectorInfo = {
        name = "keyboardSeek"
    },
) {
    val layoutDirection = LocalLayoutDirection.current
    val backwardKey = if (layoutDirection == LayoutDirection.Ltr) {
        ComposeKey.DirectionLeft
    } else {
        ComposeKey.DirectionRight
    }
    val forwardKey = if (layoutDirection == LayoutDirection.Ltr) {
        ComposeKey.DirectionRight
    } else {
        ComposeKey.DirectionLeft
    }

    val onBackwardState by rememberUpdatedState(onBackward)
    val onForwardState by rememberUpdatedState(onForward)
    onKey(backwardKey) {
        onBackwardState()
    }.onKey(forwardKey) {
        onForwardState()
    }
}
