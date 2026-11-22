package ciyin.designsystem.fluent

import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.component.control.NomiControlRenderer
import ciyin.designsystem.component.divider.NomiDividerRenderer
import ciyin.designsystem.component.feedback.NomiFeedbackRenderer
import ciyin.designsystem.component.icon.NomiIconRenderer
import ciyin.designsystem.component.list.NomiListItemRenderer
import ciyin.designsystem.component.menu.NomiMenuRenderer
import ciyin.designsystem.fluent.button.FluentButton
import ciyin.designsystem.fluent.control.FluentControlRenderer
import ciyin.designsystem.fluent.divider.FluentDividerRenderer
import ciyin.designsystem.fluent.feedback.FluentFeedbackRenderer
import ciyin.designsystem.fluent.icon.FluentIcon
import ciyin.designsystem.fluent.list.FluentListItemRenderer
import ciyin.designsystem.fluent.menu.FluentMenuRenderer
import ciyin.designsystem.fluent.navigation.FluentNavigationRenderer
import ciyin.designsystem.fluent.scaffold.FluentScaffoldRenderer
import ciyin.designsystem.fluent.surface.FluentSurface
import ciyin.designsystem.fluent.text.FluentText

/**
 * Fluent 十一族组件聚合，仅负责组装。
 */
internal object FluentComponents : NomiDesignSystemComponents {
    /**
     * 文本族。
     */
    override val text = FluentText

    /**
     * 操作族。
     */
    override val actions = FluentButton

    /**
     * 表面及卡片族。
     */
    override val surfaces = FluentSurface

    /**
     * 导航套件；三种布局由原生 NavigationView 的显示模式承担。
     */
    override val navigation = FluentNavigationRenderer

    /**
     * 页面脚手架；上游没有 Scaffold，由原生色板与内容环境建立等价外壳。
     */
    override val scaffold = FluentScaffoldRenderer

    /**
     * 列表项；可点击与静态行分别使用原生 CardExpanderItem 的对应重载。
     */
    override val listItems: NomiListItemRenderer = FluentListItemRenderer

    /**
     * 图标；内容色与透明度沿用原生 Fluent 环境。
     */
    override val icons: NomiIconRenderer = FluentIcon

    /**
     * 分割线；只有横向原生分隔线，纵向与粗细/颜色覆盖降级并记录警告。
     */
    override val decorations: NomiDividerRenderer = FluentDividerRenderer

    /**
     * 选择控件；开关、复选框与单选按钮分别映射到原生控件，文本输入框由文本族实现。
     */
    override val controls: NomiControlRenderer = FluentControlRenderer

    /**
     * 提示浮层；触发方式与位置由原生 TooltipBox 决定。
     */
    override val feedback: NomiFeedbackRenderer = FluentFeedbackRenderer

    /**
     * 菜单；浮层与菜单项分别映射到原生 DropdownMenu 与弱强调按钮行。
     */
    override val menus: NomiMenuRenderer = FluentMenuRenderer
}
