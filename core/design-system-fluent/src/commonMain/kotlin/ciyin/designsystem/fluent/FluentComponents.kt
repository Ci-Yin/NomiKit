package ciyin.designsystem.fluent

import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.fluent.button.FluentButton
import ciyin.designsystem.fluent.navigation.FluentNavigationRenderer
import ciyin.designsystem.fluent.scaffold.FluentScaffoldRenderer
import ciyin.designsystem.fluent.surface.FluentSurface
import ciyin.designsystem.fluent.text.FluentText

/**
 * Fluent 四族组件聚合，仅负责组装。
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
}
