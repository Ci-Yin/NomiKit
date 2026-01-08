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
 * 多平台滚动状态监听器
 *
 * 监听滚动状态变化并通过回调通知:
 * - Idle: 静止状态
 * - Dragging: 用户拖拽中
 * - Settling: 惯性滚动中
 *
 * @param scrollState 可滚动状态对象
 * @param onStateChanged 状态变化回调 (state, direction, scrollState)
 *
 * 使用示例:
 * ```
 * val listState = rememberLazyListState()
 * LazyColumn(
 *     state = listState,
 *     modifier = Modifier.onScrollStateChanged(listState) { state, direction, _ ->
 *         when (state) {
 *             ScrollState.Idle -> println("滚动停止")
 *             ScrollState.Dragging -> println("用户拖拽: ${direction.name}")
 *             ScrollState.Settling -> println("惯性滚动: ${direction.name}")
 *         }
 *     }
 * )
 * ```
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
                val event = awaitPointerEvent(PointerEventPass.Initial)

                when (event.type) {
                    PointerEventType.Press -> {
                        logger.d { "手势按下" }
                        currentState = ScrollStatus.Dragging
                        onStateChanged(currentState, Direction.Top, scrollState)
                    }

                    PointerEventType.Move -> {
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

                    PointerEventType.Release -> {
                        logger.d { "手势释放" }
                        if (scrollState.isScrollInProgress) {
                            currentState = ScrollStatus.Settling
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
