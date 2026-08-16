package ciyin.video.player.ui.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openani.mediamp.features.PlaybackSpeed

/** 记住与播放器倍速能力绑定的长按快进状态。 */
@Composable
fun rememberPlayerFastSkipState(
    playerState: PlaybackSpeed,
    gestureIndicatorState: GestureIndicatorState,
    fastForwardSpeed: Float = 3f,
): FastSkipState {
    return remember(playerState, fastForwardSpeed) {
        PlayerFastSkipState(playerState, gestureIndicatorState, fastForwardSpeed).fastSkipState
    }
}

/** 协调播放器倍速与手势提示状态。 */
class PlayerFastSkipState(
    playbackSpeed: PlaybackSpeed,
    /** 显示快进状态的手势指示器。 */
    private val gestureIndicatorState: GestureIndicatorState,
    /** 长按期间使用的播放倍速。 */
    private val fastForwardSpeed: Float = 3f,
) {
    /** 开始快进前的播放倍速。 */
    private var originalSpeed = 0f

    /** 当前手势指示器票据。 */
    private var gestureIndicatorTicket = 0

    /** 可直接绑定到手势修饰符的快进状态。 */
    val fastSkipState: FastSkipState = FastSkipState(
        onStart = { skipDirection ->
            originalSpeed = playbackSpeed.value
            playbackSpeed.set(
                when (skipDirection) {
                    SkipDirection.FORWARD -> fastForwardSpeed
                    SkipDirection.BACKWARD -> error("Backward skipping is not supported")
                },
            )
            gestureIndicatorTicket = gestureIndicatorState.startFastForward()
        },
        onStop = {
            playbackSpeed.set(originalSpeed)
            gestureIndicatorState.stopFastForward(gestureIndicatorTicket)
        },
    )
}

/** 跟踪单次长按快进手势的方向与有效票据。 */
@Stable
class FastSkipState(
    /** 开始跳转时的回调。 */
    private val onStart: (skipDirection: SkipDirection) -> Unit,
    /** 停止跳转时的回调。 */
    private val onStop: () -> Unit,
) {
    /** 当前持续跳转方向。 */
    private var skippingDirection: SkipDirection? by mutableStateOf(null)

    /** 用于忽略过期结束事件的票据。 */
    private var ticket: Int = 0

    /** 开始沿指定方向持续跳转并返回票据。 */
    fun startSkipping(direction: SkipDirection): Int {
        skippingDirection = direction
        onStart(direction)
        return ++ticket
    }

    /** 在票据有效时停止持续跳转。 */
    fun stopSkipping(ticket: Int) {
        if (ticket == this.ticket) {
            skippingDirection = null
            onStop()
        }
    }
}

/** 长按跳转方向。 */
enum class SkipDirection {
    FORWARD, BACKWARD
}

/** 为组件添加长按快进或快退手势。 */
fun Modifier.longPressFastSkip(
    state: FastSkipState,
    direction: SkipDirection,
): Modifier {
    var ticket = 0
    return detectLongPressGesture(
        onStart = {
            ticket = state.startSkipping(direction)
        },
        onEnd = {
            state.stopSkipping(ticket)
        },
    )
}
//    pointerInput(Unit) {
//    detectLongPressGesture()
////    detectTapGestures(
////        onPress = {
////            val ticket = state.startSkipping(direction)
////            awaitPointerEventScope {
////                var event = awaitPointerEvent()
////                while (event.changes.any { it.pressed }) {
////                    event = awaitPointerEvent()
////                }
////
////                state.stopSkipping(ticket)
////            }
////        }
////    )
//}

/** 检测长按并回调开始、结束与取消事件。 */
fun Modifier.detectLongPressGesture(
    onStart: () -> Unit,
    onEnd: () -> Unit,
    longPressTimeout: Long = 500L
): Modifier = pointerInput(Unit) {
    coroutineScope {
        val touchSlop = viewConfiguration.touchSlop
        var isLongPressDetected = false

        awaitEachGesture {
            val initialPosition = awaitFirstDown(requireUnconsumed = false).position
            // note: we don't consume the down event

            // Starts a job to mark long press detected if the user does not move the pointer,
            // i.e. is holding at the same position for a certain time).
            val longPressJob = launch {
                delay(longPressTimeout)
                onStart()
                isLongPressDetected = true
            }

            var change = awaitPointerEvent()
            while (change.changes.any { it.pressed }) { // Pointer is still down
                val pointer = change.changes[0]
                if (isLongPressDetected) {
                    // Consume all events so that we won't trigger other gestures like swiping
                    change.changes.forEach { it.consume() }
                }
                if ((pointer.position - initialPosition).getDistance() > touchSlop) {
                    // User is swiping.
                    // Note, this can also happen if the long press has already been detected.
                    longPressJob.cancel() // Stop detecting long press if it hasn't been detected yet
                }
                change = awaitPointerEvent()
            }
            // Not pressing anymore
            if (isLongPressDetected) {
                // Consume the pointer up event
                change.changes.forEach { it.consume() }
            }

            longPressJob.cancel()
            if (isLongPressDetected) {
                onEnd()
                isLongPressDetected = false
            }
        }
    }
}
