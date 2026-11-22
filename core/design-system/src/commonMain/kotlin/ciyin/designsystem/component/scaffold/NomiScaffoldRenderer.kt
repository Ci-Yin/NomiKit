package ciyin.designsystem.component.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * 页面脚手架的原生渲染契约。
 *
 * 实现负责建立当前设计系统的页面外壳，并按 topBar、content、bottomBar 的顺序排布三个槽位；
 * content 必须位于顶栏与底栏之间且不被原生栏覆盖，让位方式由实现决定：原生内容内边距或带权重的
 * 内容容器都合法。空的 topBar 或 bottomBar 不应保留占位高度。
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
}
