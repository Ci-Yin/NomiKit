package ciyin.video.player.ui.gesture

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ciyin.video.player.ui.ControllerVisibility
import ciyin.video.player.ui.PlayerControllerState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Handles click events and auto-hide controller.
 *
 * @see LockableVideoGestureHost
 */
@Composable
fun LockedScreenGestureHost(
    controllerVisibility: () -> ControllerVisibility,
    setFullVisible: (visible: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clickable(
                remember { MutableInteractionSource() },
                indication = null,
                onClick = { setFullVisible(true) },
            ).fillMaxSize(),
    )

    if (controllerVisibility() == ControllerVisibility.Visible) {
        LaunchedEffect(true) {
            delay(2.seconds)
            setFullVisible(false)
        }
    }
    return
}


/** 根据锁定状态在普通手势层与锁定手势层之间切换。 */
@Composable
inline fun LockableVideoGestureHost(
    locked: Boolean,
    controllerState: PlayerControllerState,
    modifier: Modifier = Modifier,
    playerGestureHost: @Composable () -> Unit = {}
) {
    if (locked) {
        LockedScreenGestureHost(
            { controllerState.visibility },
            controllerState.setFullVisible,
            modifier.testTag("LockedScreenGestureHost"),
        )
    } else {
        playerGestureHost()
    }
}
