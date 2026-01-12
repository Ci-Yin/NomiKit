package ciyin.ui.foundation.effects

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * 同步 Pager 的当前索引到指定值的效果方法。
 *
 * @param pagerState Pager 的状态对象，用于控制和获取当前页面索引。
 * @param index 目标页面的索引，当提供的新索引与当前索引不同时将执行页面滚动。
 */
@Composable
fun SyncPagerIndexEffect(
    pagerState: PagerState,
    index: Int,
) {
    val latestIndex by rememberUpdatedState(index)

    // 当外部索引发生变化时，自动滚动到指定页面
    LaunchedEffect(latestIndex) {
        if (pagerState.currentPage != latestIndex) {
            pagerState.animateScrollToPage(latestIndex)
        }
    }
}

/**
 * 监听 Pager 页面稳定后的变化并触发回调。
 *
 * 使用 settledPage 而非 currentPage 或 isScrollInProgress，确保只在页面完全稳定后才触发回调，
 * 这样可以避免在其他方向滑动（如垂直滑动）时误触发。
 *
 * @param pagerState 当前的分页状态
 * @param excludeIndex 需要排除的索引，当稳定页面等于此索引时不触发回调（用于避免重复触发）
 * @param onPageSettled 当分页器页面稳定变化时触发的回调，参数为新的页面索引
 */
@Composable
inline fun OnPagerSettledEffect(
    pagerState: PagerState,
    excludeIndex: Int,
    crossinline onPageSettled: (Int) -> Unit
) {
    val latestExcludeIndex by rememberUpdatedState(excludeIndex)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .filter { it != latestExcludeIndex }
            .collect { page ->
                onPageSettled(page)
            }
    }
}