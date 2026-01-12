package ciyin.ui.foundation.effects

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import com.yy.myuko.core.coroutines.flows.throttle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlin.time.Duration.Companion.milliseconds

/**
 * 在滚动到列表末尾附近时触发加载更多的 Compose 效果。
 *
 * 默认情况下，只有在用户停止滑动时才会触发加载，避免滑动过程中加载数据导致的画面掉帧。
 *
 * @param listState 当前的懒加载列表状态，用于监控列表的滚动事件和可见项信息。
 * @param threshold 触发加载更多的阈值，表示当前可见的最后一项索引与列表总项数的差值，默认为 20。
 * @param loadWhileScrolling 是否在滑动过程中触发加载，默认为 false。设为 true 时恢复滑动中加载的行为。
 * @param onLoadMore 当需要加载更多数据时触发的回调函数。
 */
@Composable
fun LazyListPaginationEffect(
    listState: LazyListState,
    threshold: Int = 20,
    loadWhileScrolling: Boolean = false,
    onLoadMore: () -> Unit
) {
    // 使用 rememberUpdatedState 确保总是调用最新的回调
    val currentOnLoadMore = rememberUpdatedState(onLoadMore)

    LaunchedEffect(listState, threshold, loadWhileScrolling) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            // lastVisibleIndex >= totalItems - 1 - threshold
            // 等价于：当剩余 item <= threshold 时触发
            val nearEnd = totalItems > 0 && lastVisibleIndex >= totalItems - 1 - threshold
            nearEnd && (loadWhileScrolling || !listState.isScrollInProgress)
        }.collectOnTrue {
            currentOnLoadMore.value()
        }

    }
}

/**
 * 一个分页加载的效果工具方法，适用于 `LazyGrid` 组件。当列表滚动到接近底部时触发加载更多的数据。
 *
 * 默认情况下，只有在用户停止滑动时才会触发加载，避免滑动过程中加载数据导致的画面掉帧。
 *
 * @param gridState 当前 `LazyGrid` 的状态，用于监听可见项和滚动行为。
 * @param threshold 触发加载更多的阈值，表示距离列表底部还有多少项时开始加载更多，默认为 20。
 * @param loadWhileScrolling 是否在滑动过程中触发加载，默认为 false。设为 true 时恢复滑动中加载的行为。
 * @param onLoadMore 当满足分页触发条件时调用的回调函数，用户实现加载更多逻辑。
 */
@Composable
fun LazyGridPaginationEffect(
    gridState: LazyGridState,
    threshold: Int = 20,
    loadWhileScrolling: Boolean = false,
    onLoadMore: () -> Unit
) {
    val currentOnLoadMore = rememberUpdatedState(onLoadMore)

    LaunchedEffect(gridState, threshold, loadWhileScrolling) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            // lastVisibleIndex >= totalItems - 1 - threshold
            // 等价于：当剩余 item <= threshold 时触发
            val nearEnd = totalItems > 0 && lastVisibleIndex >= totalItems - 1 - threshold
            nearEnd && (loadWhileScrolling || !gridState.isScrollInProgress)
        }.collectOnTrue { currentOnLoadMore.value() }
    }
}

/**
 * 用于实现懒加载瀑布流网格的分页效果。当用户滚动到网格的末尾，且未达到指定的阈值时，触发加载更多数据的回调。
 *
 * 默认情况下，只有在用户停止滑动时才会触发加载，避免滑动过程中加载数据导致的画面掉帧。
 *
 * @param staggeredGridState 当前懒加载瀑布流网格的状态对象，用于跟踪网格的滚动位置和布局信息。
 * @param threshold 加载更多的阈值，当最后一个可见项目的索引与总项目数量之差小于此阈值时，触发加载更多操作，默认值为 20。
 * @param loadWhileScrolling 是否在滑动过程中触发加载，默认为 false。设为 true 时恢复滑动中加载的行为。
 * @param onLoadMore 当需要加载更多数据时触发的回调函数。
 */
@Composable
fun LazyStaggeredGridPaginationEffect(
    staggeredGridState: LazyStaggeredGridState,
    threshold: Int = 20,
    loadWhileScrolling: Boolean = false,
    onLoadMore: () -> Unit
) {
    val currentOnLoadMore = rememberUpdatedState(onLoadMore)

    LaunchedEffect(staggeredGridState, threshold, loadWhileScrolling) {
        snapshotFlow {
            val layoutInfo = staggeredGridState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            // lastVisibleIndex >= totalItems - 1 - threshold
            // 等价于：当剩余 item <= threshold 时触发
            val nearEnd = totalItems > 0 && lastVisibleIndex >= totalItems - 1 - threshold
            nearEnd && (loadWhileScrolling || !staggeredGridState.isScrollInProgress)
        }.collectOnTrue { currentOnLoadMore.value() }
    }
}

/**
 * 收集布尔流中从 false 变为 true 的边沿触发。
 *
 * 使用 [distinctUntilChanged] 去重，只在值从 false 变为 true 时触发回调，避免重复触发。
 *
 * @param action 当值变为 true 时执行的回调。
 */
private suspend fun Flow<Boolean>.collectOnTrue(action: suspend () -> Unit) =
    distinctUntilChanged()
        .filter { it }
        .throttle(1000.milliseconds)
        .collect { action() }


