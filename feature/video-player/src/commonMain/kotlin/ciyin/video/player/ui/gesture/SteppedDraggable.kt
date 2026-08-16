package ciyin.video.player.ui.gesture

import androidx.annotation.MainThread
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope


/** 为离散步进拖动补充开始与结束回调。 */
interface SteppedDraggableState : DraggableState {
    /** 记录拖动起点。 */
    fun onDragStarted(offset: Offset, orientation: Orientation)

    /** 结束本次拖动。 */
    fun onDragStopped(velocity: Float)
}

/** 拖动步进方向。 */
enum class StepDirection {
    /**
     * - [Orientation.Horizontal]: To the right
     * - [Orientation.Vertical]: Down
     */
    Forward,
    Backward,
}

/** 步进拖动状态的默认实现。 */
private class SteppedDraggableStateImpl(
    @MainThread private val onStep: (StepDirection) -> Unit,
    /** 触发一次步进所需的像素距离。 */
    private val stepSizePx: Float,
) : SteppedDraggableState {
    /** 本次拖动开始位置。 */
    var startOffset: Float by mutableFloatStateOf(Float.NaN)

    /** 当前拖动位置。 */
    var currentOffset: Float by mutableFloatStateOf(0f)

    /** 上一次触发回调的位置。 */
    var lastCallbackOffset: Float by mutableFloatStateOf(0f)

    /** 记录一次拖动开始。 */
    override fun onDragStarted(offset: Offset, orientation: Orientation) {
        startOffset = if (orientation == Orientation.Horizontal) {
            offset.x
        } else {
            offset.y
        }
        currentOffset = startOffset
    }

    /** 结束拖动并清理累计距离。 */
    override fun onDragStopped(velocity: Float) {
        startOffset = Float.NaN
        currentOffset = 0f
    }

    /** 累计原始拖动距离并派发离散步进。 */
    override fun dispatchRawDelta(delta: Float) {
        draggableState.dispatchRawDelta(delta)
    }

    /** 委托给 Compose 原生拖动状态。 */
    override suspend fun drag(dragPriority: MutatePriority, block: suspend DragScope.() -> Unit) {
        draggableState.drag(dragPriority, block)
    }

    /** Compose 原生拖动状态。 */
    private val draggableState: DraggableState = DraggableState { delta ->
        currentOffset += delta
        val deltaOffset = currentOffset - startOffset
        val step = (deltaOffset / stepSizePx).toInt()
        val callbackOffset = step * stepSizePx
        if (callbackOffset != lastCallbackOffset) {
            if (callbackOffset > lastCallbackOffset) {
                onStep(StepDirection.Backward) // delta is inverted
            } else {
                onStep(StepDirection.Forward)
            }
            lastCallbackOffset = callbackOffset
        }
    }
}

/** 记住按照指定步长换算拖动距离的状态。 */
@Composable
fun rememberSteppedDraggableState(
    stepSize: Dp,
    @MainThread onStep: (StepDirection) -> Unit,
): SteppedDraggableState {
    val onStepState by rememberUpdatedState(onStep)
    val stepSizePx by rememberUpdatedState(with(LocalDensity.current) { stepSize.toPx() })
    return remember {
        SteppedDraggableStateImpl(
            onStep = { onStepState(it) },
            stepSizePx = stepSizePx,
        )
    }
}

/** 为组件添加离散步进拖动行为。 */
fun Modifier.steppedDraggable(
    state: SteppedDraggableState,
    orientation: Orientation,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    startDragImmediately: Boolean = false,
    onDragStarted: suspend CoroutineScope.(startedPosition: Offset) -> Unit = {},
    onDragStopped: suspend CoroutineScope.(velocity: Float) -> Unit = {},
    reverseDirection: Boolean = false,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "steppedDraggable"
        properties["state"] = state
        properties["orientation"] = orientation
        properties["enabled"] = enabled
        properties["interactionSource"] = interactionSource
        properties["startDragImmediately"] = startDragImmediately
        properties["onDragStarted"] = onDragStarted
        properties["onDragStopped"] = onDragStopped
        properties["reverseDirection"] = reverseDirection
    },
) {
    val onDragStartedState by rememberUpdatedState(onDragStarted)
    val onDragStoppedState by rememberUpdatedState(onDragStopped)
    draggable(
        state = state,
        orientation = orientation,
        enabled = enabled,
        interactionSource = interactionSource,
        startDragImmediately = startDragImmediately,
        onDragStarted = { offset ->
            state.onDragStarted(offset, orientation)
            onDragStartedState(offset)
        },
        onDragStopped = {
            state.onDragStopped(it)
            onDragStoppedState(it)
        },
        reverseDirection = reverseDirection,
    )
}


//fun Modifier.combinedSteppedDraggable(
//    division: List<Pair<Float, SteppedDraggableState>>,
//    orientation: Orientation,
//    enabled: Boolean = true,
//    interactionSource: MutableInteractionSource? = null,
//    startDragImmediately: Boolean = false,
//    onDragStarted: suspend CoroutineScope.(startedPosition: Offset) -> Unit = {},
//    onDragStopped: suspend CoroutineScope.(velocity: Float) -> Unit = {},
//    reverseDirection: Boolean = false,
//): Modifier = composed(
//    inspectorInfo = debugInspectorInfo {
//        name = "steppedDraggable"
//        properties["division"] = division
//        properties["orientation"] = orientation
//        properties["enabled"] = enabled
//        properties["interactionSource"] = interactionSource
//        properties["startDragImmediately"] = startDragImmediately
//        properties["onDragStarted"] = onDragStarted
//        properties["onDragStopped"] = onDragStopped
//        properties["reverseDirection"] = reverseDirection
//    }
//) {
//    val onDragStartedState by rememberUpdatedState(onDragStarted)
//    val onDragStoppedState by rememberUpdatedState(onDragStopped)
//    draggable(
//        state = state.draggableState,
//        orientation = orientation,
//        enabled = enabled,
//        interactionSource = interactionSource,
//        startDragImmediately = startDragImmediately,
//        onDragStarted = { offset ->
//            state.onDragStarted(offset, orientation)
//            onDragStartedState(offset)
//        },
//        onDragStopped = {
//            state.onDragStopped(it)
//            onDragStoppedState(it)
//        },
//        reverseDirection = reverseDirection,
//    )
//}
