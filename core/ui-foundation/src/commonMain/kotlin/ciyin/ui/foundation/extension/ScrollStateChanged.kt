package ciyin.ui.foundation.extension

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs

/**
 * 滚动状态枚举
 */
enum class ScrollStatus {
    /** 静止状态 */
    Idle,

    /** 用户拖拽中 */
    Dragging,

    /** 惯性滚动中 */
    Settling
}

/**
 * 监听触摸拖拽与桌面鼠标滚轮产生的多平台滚动状态。
 *
 * 单独按下或普通点击保持 [ScrollStatus.Idle]；真实拖动和滚轮事件报告
 * [ScrollStatus.Dragging]，滚动完全停止后报告 [ScrollStatus.Idle]。
 * [Direction.Top] 表示向内容末端浏览，[Direction.Bottom] 表示向内容起点浏览。
 *
 * @param scrollState 被监听的可滚动状态
 * @param onStateChanged 滚动状态、方向与原状态对象回调
 * @return 附加多平台指针监听能力的修饰符
 */
@Composable
fun <S : ScrollableState> Modifier.onScrollStateChanged(
    scrollState: S,
    onStateChanged: (state: ScrollStatus, direction: Direction, scrollState: S) -> Unit
): Modifier {
    val logger = remember { Logger.withTag("ScrollState") }

    var currentState by remember { mutableStateOf(ScrollStatus.Idle) }

    // 多平台手势监听
    val gestureModifier = pointerInput(scrollState) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Final)

                when (event.type) {
                    PointerEventType.Press -> {
                        logger.d { "手势按下" }
                    }

                    PointerEventType.Move -> {
                        val isPressedMove = event.changes.any { change ->
                            change.pressed && change.position != change.previousPosition
                        }
                        if (!isPressedMove) continue
                        if (currentState != ScrollStatus.Dragging) {
                            logger.d { "手势移动" }
                            currentState = ScrollStatus.Dragging
                        }
                        val direction = if (scrollState.lastScrolledForward) {
                            Direction.Top
                        } else {
                            Direction.Bottom
                        }
                        onStateChanged(currentState, direction, scrollState)
                    }

                    PointerEventType.Scroll -> {
                        val delta = event.changes.firstOrNull()?.scrollDelta ?: continue
                        val axisDelta = if (abs(delta.y) >= abs(delta.x)) delta.y else delta.x
                        if (axisDelta == 0f) continue
                        currentState = ScrollStatus.Dragging
                        val direction = if (axisDelta > 0f) Direction.Top else Direction.Bottom
                        logger.d { "滚轮滚动: $direction" }
                        onStateChanged(currentState, direction, scrollState)
                    }

                    PointerEventType.Release -> {
                        logger.d { "手势释放" }
                        if (currentState == ScrollStatus.Dragging && scrollState.isScrollInProgress) {
                            currentState = ScrollStatus.Settling
                            val direction = if (scrollState.lastScrolledForward) {
                                Direction.Top
                            } else {
                                Direction.Bottom
                            }
                            onStateChanged(currentState, direction, scrollState)
                        } else if (currentState != ScrollStatus.Idle) {
                            currentState = ScrollStatus.Idle
                            val direction = if (scrollState.lastScrolledForward) {
                                Direction.Top
                            } else {
                                Direction.Bottom
                            }
                            onStateChanged(currentState, direction, scrollState)
                        }
                    }
                }
            }
        }
    }

    // 监听滚动位置变化 (用于检测惯性滚动结束)
    LaunchedEffect(scrollState) {
        snapshotFlow {
            when (scrollState) {
                is LazyStaggeredGridState -> scrollState.firstVisibleItemScrollOffset
                is LazyGridState -> scrollState.firstVisibleItemScrollOffset
                is ScrollState -> scrollState.value
                else -> 0
            }
        }
            .distinctUntilChanged()
            .collect { offset ->
                if (currentState == ScrollStatus.Dragging && scrollState.isScrollInProgress) {
                    logger.v { "滚动位置: $offset" }
                }
            }
    }

    // 监听滚动进行状态 (检测滚动是否完全停止)
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                logger.d { "滚动进行中: $isScrolling" }

                if (!isScrolling && currentState != ScrollStatus.Idle) {
                    currentState = ScrollStatus.Idle
                    val direction = if (scrollState.lastScrolledForward) {
                        Direction.Top
                    } else {
                        Direction.Bottom
                    }
                    onStateChanged(currentState, direction, scrollState)
                }
            }
    }

    return this.then(gestureModifier)
}
