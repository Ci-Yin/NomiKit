package ciyin.designsystem.material3

import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.component.button.NomiActionRenderer
import ciyin.designsystem.component.control.NomiControlRenderer
import ciyin.designsystem.component.divider.NomiDividerRenderer
import ciyin.designsystem.component.feedback.NomiFeedbackRenderer
import ciyin.designsystem.component.icon.NomiIconRenderer
import ciyin.designsystem.component.list.NomiListItemRenderer
import ciyin.designsystem.component.navigation.NomiNavigationRenderer
import ciyin.designsystem.component.scaffold.NomiScaffoldRenderer
import ciyin.designsystem.component.surface.NomiSurfaceRenderer
import ciyin.designsystem.component.text.NomiTextRenderer
import ciyin.designsystem.material3.button.Material3ActionRenderer
import ciyin.designsystem.material3.control.Material3ControlRenderer
import ciyin.designsystem.material3.divider.Material3DividerRenderer
import ciyin.designsystem.material3.feedback.Material3FeedbackRenderer
import ciyin.designsystem.material3.icon.Material3IconRenderer
import ciyin.designsystem.material3.list.Material3ListItemRenderer
import ciyin.designsystem.material3.navigation.Material3NavigationRenderer
import ciyin.designsystem.material3.scaffold.Material3ScaffoldRenderer
import ciyin.designsystem.material3.surface.Material3SurfaceRenderer
import ciyin.designsystem.material3.text.Material3TextRenderer

/**
 * Material 3 十一族渲染器装配；各组件实现分别位于独立文件。
 */
internal object Material3Components : NomiDesignSystemComponents {
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
    override val scaffold: NomiScaffoldRenderer = Material3ScaffoldRenderer

    /**
     * 列表项的原生适配；行高、文本样式与内容色由原生 ListItem 决定。
     */
    override val listItems: NomiListItemRenderer = Material3ListItemRenderer

    /**
     * 图标的原生适配；着色与尺寸由原生 Icon 与调用方修饰符决定。
     */
    override val icons: NomiIconRenderer = Material3IconRenderer

    /**
     * 分割线的原生适配；水平与垂直方向都由原生分割线承担。
     */
    override val decorations: NomiDividerRenderer = Material3DividerRenderer

    /**
     * 选择控件的原生适配；三个入口分别使用对应的原生控件，文本输入框由文本族实现。
     */
    override val controls: NomiControlRenderer = Material3ControlRenderer

    /**
     * 提示浮层的原生适配；触发方式与位置由原生 TooltipBox 决定。
     */
    override val feedback: NomiFeedbackRenderer = Material3FeedbackRenderer

}
