package ciyin.video.player.ui.gesture

import androidx.annotation.MainThread
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope

/** 为音量或亮度手势提供统一的层级控制。 */
interface LevelController {
    /** 当前层级。 */
    val level: Float

    /** 可调层级范围。 */
    val range: ClosedRange<Float>

    /** 更新到指定层级。 */
    @MainThread
    fun setLevel(level: Float)
}

/** 不支持层级调节时使用的空实现。 */
object NoOpLevelController : LevelController {
    /** 固定层级为零。 */
    override val level: Float
        get() = 0f

    /** 空实现仍使用标准比例范围。 */
    override val range: ClosedRange<Float> = 0f..1f

    /** 空实现忽略层级更新。 */
    override fun setLevel(level: Float) {

    }
}

/** 将层级提高指定步长。 */
@MainThread
fun LevelController.increaseLevel(step: Float = 0.05f) {
    setLevel((level + step).coerceAtMost(range.endInclusive))
}

/** 将层级降低指定步长。 */
@MainThread
fun LevelController.decreaseLevel(step: Float = 0.05f) {
    setLevel((level - step).coerceAtLeast(range.start))
}

/** 添加垂直层级手势并同步显示手势提示。 */
fun Modifier.swipeLevelControlWithIndicator(
    controller: LevelController,
    stepSize: Dp,
    orientation: Orientation,
    indicatorState: GestureIndicatorState,
    step: Float = 0.05f,
    setup: () -> Unit = {}
): Modifier = this then swipeLevelControl(
    controller = controller, stepSize = stepSize, orientation = orientation, step = step,
    afterStep = {
        setup()
        indicatorState.progressValue = controller.level
    },
    onDragStarted = {
        indicatorState.visible = true
    },
    onDragStopped = {
        indicatorState.visible = false
    },
)

/** 为组件添加垂直滑动层级控制。 */
fun Modifier.swipeLevelControl(
    controller: LevelController,
    stepSize: Dp,
    orientation: Orientation,
    step: Float = 0.05f,
    afterStep: (StepDirection) -> Unit = {},
    onDragStarted: suspend CoroutineScope.(startedPosition: Offset) -> Unit = {},
    onDragStopped: suspend CoroutineScope.(velocity: Float) -> Unit = {},
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "swipeLevelControl"
        properties["controller"] = controller
        properties["stepSize"] = stepSize
        properties["orientation"] = orientation
    },
) {
    steppedDraggable(
        rememberSteppedDraggableState(
            stepSize = stepSize,
            onStep = { direction ->
                when (direction) {
                    StepDirection.Forward -> controller.increaseLevel(step)
                    StepDirection.Backward -> controller.decreaseLevel(step)
                }
                afterStep(direction)
            },
        ),
        orientation = orientation,
        onDragStarted = onDragStarted,
        onDragStopped = onDragStopped,
    )

}
