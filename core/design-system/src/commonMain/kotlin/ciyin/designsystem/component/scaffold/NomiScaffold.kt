package ciyin.designsystem.component.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 页面脚手架的颜色稀疏覆盖；Transparent 是有效覆盖，Unspecified 才表示未指定。
 *
 * @property containerColor 页面容器背景色，未指定时由当前设计系统的原生默认决定。
 * @property contentColor 页面内容色，未指定时由当前设计系统的原生默认决定。
 */
@Immutable
data class NomiScaffoldColors(
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
)

/**
 * 页面脚手架的原生渲染契约。
 *
 * 实现负责建立当前设计系统的页面外壳，并按 topBar、content、bottomBar 的顺序排布三个槽位；
 * content 必须位于顶栏与底栏之间且不被原生栏覆盖（Material 3 用 Scaffold 的内容内边距让出空间，
 * 其余实现用带权重的内容容器排布）。空的 topBar 或 bottomBar 不应保留占位高度。
 */
@Stable
interface NomiScaffoldRenderer {
    /**
     * 建立原生页面外壳。
     *
     * @param modifier 应用于页面外壳根节点的修饰符。
     * @param colors 容器色与内容色的逐字段覆盖；Unspecified 保留当前设计系统的原生值。
     * @param topBar 顶部栏槽；空 lambda 表示没有顶部栏。
     * @param bottomBar 底部栏槽；空 lambda 表示没有底部栏。
     * @param content 页面主体内容，由实现放入顶栏与底栏之间。
     */
    @Composable
    fun Scaffold(
        modifier: Modifier,
        colors: NomiScaffoldColors,
        topBar: @Composable () -> Unit,
        bottomBar: @Composable () -> Unit,
        content: @Composable () -> Unit,
    )
}

/**
 * 统一页面脚手架入口；页面标题、最大宽度、滚动状态与系统栏避让仍由调用方持有。
 *
 * 颜色、排版与尺寸沿用当前设计系统的原生默认值，不在此入口补齐统一视觉；
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param modifier 应用于页面外壳根节点的修饰符。
 * @param colors 容器色与内容色的逐字段覆盖；Unspecified 保留原生值，Transparent 是有效覆盖。
 * @param topBar 顶部栏槽；空 lambda 表示没有顶部栏。
 * @param bottomBar 底部栏槽；空 lambda 表示没有底部栏。
 * @param content 页面主体内容，位于顶栏与底栏之间。
 */
@Composable
fun NomiScaffold(
    modifier: Modifier = Modifier,
    colors: NomiScaffoldColors = NomiScaffoldColors(),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) = requireNomiDesignSystem().components.scaffold.Scaffold(
    modifier = modifier,
    colors = colors,
    topBar = topBar,
    bottomBar = bottomBar,
    content = content,
)
