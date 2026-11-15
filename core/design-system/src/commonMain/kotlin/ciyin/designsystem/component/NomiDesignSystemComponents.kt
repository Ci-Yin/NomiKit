package ciyin.designsystem.component

import androidx.compose.runtime.Stable
import ciyin.designsystem.component.button.NomiActionRenderer
import ciyin.designsystem.component.list.NomiListItemRenderer
import ciyin.designsystem.component.navigation.NomiNavigationRenderer
import ciyin.designsystem.component.scaffold.NomiScaffoldRenderer
import ciyin.designsystem.component.surface.NomiSurfaceRenderer
import ciyin.designsystem.component.text.NomiTextRenderer

/**
 * 六族组件的 renderer 集合，仅负责组装；Card 由表面族独立分发，导航套件由导航族独立分发。
 */
@Stable
interface NomiDesignSystemComponents {
    /**
     * 文本组件。
     */
    val text: NomiTextRenderer

    /**
     * 操作组件。
     */
    val actions: NomiActionRenderer

    /**
     * 表面及非点击卡片。
     */
    val surfaces: NomiSurfaceRenderer

    /**
     * 导航套件；Bar、Rail、Drawer 的支持范围由能力声明决定。
     */
    val navigation: NomiNavigationRenderer

    /**
     * 页面脚手架；顶栏、主体内容与底栏三个槽位由页面的原生外壳排布。
     */
    val scaffold: NomiScaffoldRenderer

    /**
     * 列表项；行高、容器、文本样式与交互外观由原生列表组件决定。
     */
    val listItems: NomiListItemRenderer
}
