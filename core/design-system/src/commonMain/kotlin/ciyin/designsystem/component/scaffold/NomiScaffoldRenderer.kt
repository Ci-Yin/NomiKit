package ciyin.designsystem.component.scaffold

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * 页面外壳与顶栏的原生渲染契约。
 *
 * 实现负责建立当前设计系统的页面外壳，并按 topBar、content、bottomBar 的顺序排布三个槽位；
 * content 必须位于顶栏与底栏之间且不被原生栏覆盖，让位方式由实现决定：原生内容内边距或带权重的
 * 内容容器都合法。空的 topBar 或 bottomBar 不应保留占位高度。顶栏方法只负责栏体、槽位顺序与颜色，
 * 标题与图标由调用方组合，不追加点击层。
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
     * @param nativeSpec 页面脚手架专属参数；null 或未携带本系统类型时全部沿用原生默认。
     * @param content 页面主体内容，由实现放入顶栏与底栏之间。
     */
    @Composable
    fun Scaffold(
        modifier: Modifier,
        colors: NomiScaffoldColors,
        topBar: @Composable () -> Unit,
        bottomBar: @Composable () -> Unit,
        nativeSpec: NomiScaffoldSpec?,
        content: @Composable () -> Unit,
    )

    /**
     * 渲染一个原生顶栏。
     *
     * 实现负责栏高、排版、系统栏内边距与颜色映射；标题、导航图标与操作槽按顺序放入，
     * navigationIcon 为 null 时不占位，也不保留占位宽度。
     *
     * @param title 标题槽，位于导航图标与操作槽之间并占据剩余宽度。
     * @param modifier 应用于顶栏根节点的修饰符。
     * @param navigationIcon 导航图标槽；null 表示不占位。
     * @param actions 操作槽，在标题之后按行排列。
     * @param colors 逐字段颜色覆盖；Unspecified 保留当前设计系统的原生值。
     * @param nativeSpec 顶栏专属参数；null 或未携带本系统类型时全部沿用原生默认。
     */
    @Composable
    fun TopAppBar(
        title: @Composable () -> Unit,
        modifier: Modifier,
        navigationIcon: (@Composable () -> Unit)?,
        actions: @Composable RowScope.() -> Unit,
        colors: NomiTopAppBarColors,
        nativeSpec: NomiTopAppBarSpec?,
    )
}
