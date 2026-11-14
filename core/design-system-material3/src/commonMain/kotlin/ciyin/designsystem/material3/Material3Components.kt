package ciyin.designsystem.material3

import ciyin.designsystem.component.button.NomiActionRenderer
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.component.navigation.NomiNavigationRenderer
import ciyin.designsystem.component.surface.NomiSurfaceRenderer
import ciyin.designsystem.component.text.NomiTextRenderer
import ciyin.designsystem.material3.button.Material3ActionRenderer
import ciyin.designsystem.material3.navigation.Material3NavigationRenderer
import ciyin.designsystem.material3.scaffold.Material3ScaffoldRenderer
import ciyin.designsystem.material3.surface.Material3SurfaceRenderer
import ciyin.designsystem.material3.text.Material3TextRenderer

/**
 * Material 3 四族渲染器装配；各组件实现分别位于独立文件。
 */
object Material3DesignSystemComponents : NomiDesignSystemComponents {
    /**
     * 文本原生适配。
     */
    override val text: NomiTextRenderer = Material3TextRenderer

    /**
     * 操作原生适配。
     */
    override val actions: NomiActionRenderer = Material3ActionRenderer

    /**
     * Surface 和 Card 的原生适配。
     */
    override val surfaces: NomiSurfaceRenderer = Material3SurfaceRenderer

    /**
     * 导航套件的原生适配；Bar、Rail 与永久抽屉均由对应原生组件实现。
     */
    override val navigation: NomiNavigationRenderer = Material3NavigationRenderer

    /**
     * 页面脚手架的原生适配；外壳与内容让位由原生 Scaffold 承担。
     */
    override val scaffold = Material3ScaffoldRenderer
}
