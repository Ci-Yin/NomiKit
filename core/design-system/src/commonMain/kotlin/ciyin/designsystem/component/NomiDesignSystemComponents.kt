package ciyin.designsystem.component

import androidx.compose.runtime.Stable
import ciyin.designsystem.component.button.NomiActionRenderer
import ciyin.designsystem.component.navigation.NomiNavigationRenderer
import ciyin.designsystem.component.surface.NomiSurfaceRenderer
import ciyin.designsystem.component.text.NomiTextRenderer

/**
 * 四组件的 renderer 集合，仅负责组装；Card 由表面族独立分发，导航套件由导航族独立分发。
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
}
