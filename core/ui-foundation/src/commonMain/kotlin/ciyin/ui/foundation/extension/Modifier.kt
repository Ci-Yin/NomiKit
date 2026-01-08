package ciyin.ui.foundation.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/8/26 上午3:01
 */

inline fun Modifier.thenIf(condition: Boolean, block: Modifier.() -> Modifier) =
    if (condition) block() else this

inline fun Modifier.thenIf(
    condition: Boolean,
    crossinline block: Modifier.() -> Modifier,
    crossinline block2: Modifier.() -> Modifier
) =
    if (condition) block() else block2()

@Composable
fun Modifier.onDpSizeChanged(onDpSizeChanged: (DpSize) -> Unit): Modifier {
    val density = LocalDensity.current
    return onSizeChanged {
        with(density) {
            onDpSizeChanged(DpSize(it.width.toDp(), it.height.toDp()))
        }
    }
}

@Composable
fun Modifier.clickableNoRipple(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier {
    return clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}

inline fun Modifier.swipeTrigger(
    direction: Direction = Direction.Left,
    triggerDistance: Float = 100f,
    crossinline onTrigger: () -> Unit
) = pointerInput(Unit) {
    var offsetFirst = Offset.Zero
    var offsetLast = Offset.Zero

    detectDragGestures(

        onDragStart = {
            offsetFirst = it
        },
        onDrag = { pointerInputChange, _ ->
            pointerInputChange.consume()//消费滑动
            offsetLast = pointerInputChange.position
        },
        onDragEnd = {
            val isTrigger = when (direction) {
                Direction.Top -> offsetLast.y - offsetFirst.y < triggerDistance
                Direction.Bottom -> offsetLast.y - offsetFirst.y > triggerDistance
                Direction.Left -> offsetLast.x - offsetFirst.x > triggerDistance
                Direction.Right -> offsetLast.x - offsetFirst.x < triggerDistance
            }
            if (isTrigger) {
                onTrigger()
            }
        },
    )

}

/**
 * 表示方向的枚举类。
 */
enum class Direction {
    /**
     * 上方。
     */
    Top,

    /**
     * 下方。
     */
    Bottom,

    /**
     * 左侧。
     */
    Left,

    /**
     * 右侧。
     */
    Right
}
