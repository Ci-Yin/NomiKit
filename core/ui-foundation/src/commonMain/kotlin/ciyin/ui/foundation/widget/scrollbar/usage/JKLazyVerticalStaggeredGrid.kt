package ciyin.ui.foundation.widget.scrollbar.usage

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ciyin.ui.foundation.widget.scrollbar.DraggableScrollbar
import ciyin.ui.foundation.widget.scrollbar.lazystate.rememberDraggableScroller
import ciyin.ui.foundation.widget.scrollbar.lazystate.scrollbarState

/**
 * 渲染带 full-height 可拖动滚动条的竖向懒加载瀑布流。
 *
 * @param modifier 网格根修饰符
 * @param itemSize 条目总数
 * @param columns 瀑布流列策略
 * @param state 瀑布流状态
 * @param contentPadding 内容边距
 * @param reverseLayout 是否反向布局
 * @param verticalItemSpacing 条目纵向间距
 * @param horizontalArrangement 横向排列
 * @param flingBehavior 惯性滚动行为
 * @param userScrollEnabled 是否允许用户滚动
 * @param content 瀑布流内容
 */
@Composable
fun JKLazyVerticalStaggeredGrid(
    modifier: Modifier = Modifier,
    itemSize: Int,// 项目总数
    columns: StaggeredGridCells,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalItemSpacing: Dp = 0.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyStaggeredGridScope.() -> Unit
) {

    // 将 LazyList/LazyGrid 和滚动组件 DecorativeScrollbar 放在同一个 Box 内部
    Box(
        modifier = modifier,
    ) {
        // 1 滚动网格
        LazyVerticalStaggeredGrid(
            modifier = modifier,
            columns = columns,
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalItemSpacing = verticalItemSpacing,
            horizontalArrangement = horizontalArrangement,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = content,
        )


        // 2 滚动条——————默认项目数量大于20才出现
        //if (itemSize > 20)
        state.DraggableScrollbar(
            modifier = Modifier.fillMaxHeight()
                .align(Alignment.CenterEnd),//
            state = state.scrollbarState(
                itemsAvailable = itemSize,// 传入大小
            ),
            orientation = fullHeightGridScrollbarOrientation,
            isSupperSmall = itemSize < 100, // 小列表使用紧凑拇指，仍保留拖动能力。
            // 拇指移动，带动屏幕同步滚动...
            onThumbMoved = state.rememberDraggableScroller(
                itemsAvailable = itemSize,// 传入项目数量
            ),
        )
    }
}
