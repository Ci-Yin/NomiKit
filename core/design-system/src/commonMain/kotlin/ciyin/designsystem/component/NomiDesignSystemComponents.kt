package ciyin.designsystem.component

import androidx.compose.runtime.Stable
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

/**
 * 十一个组件族的 renderer 集合，仅负责组装；Card 由表面族独立分发，导航套件由导航族独立分发。
 */
@Stable
interface NomiDesignSystemComponents {
    /**
     * 文本组件；同时承载文本显示与文本输入框，二者共用同一实现替换点。
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
     * 导航套件；Bar、Rail 与 Drawer 的支持范围由各实现的实际能力决定，不支持时降级并记录警告。
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

    /**
     * 图标；着色与尺寸沿用原生默认，尺寸由调用方通过修饰符约束。
     */
    val icons: NomiIconRenderer

    /**
     * 装饰线；分割线方向、粗细与颜色由原生入口决定。
     */
    val decorations: NomiDividerRenderer

    /**
     * 选择控件（开关、复选框、单选按钮）；各入口拥有独立的专属参数契约。
     */
    val controls: NomiControlRenderer

    /**
     * 反馈与提示；提示浮层的触发方式与位置由原生实现决定。
     */
    val feedback: NomiFeedbackRenderer

}
