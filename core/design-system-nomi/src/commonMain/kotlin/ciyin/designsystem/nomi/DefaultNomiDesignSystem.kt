package ciyin.designsystem.nomi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.nomi.button.DefaultNomiActionRenderer
import ciyin.designsystem.nomi.list.DefaultNomiListItemRenderer
import ciyin.designsystem.nomi.navigation.DefaultNomiNavigationRenderer
import ciyin.designsystem.nomi.scaffold.DefaultNomiScaffoldRenderer
import ciyin.designsystem.nomi.surface.DefaultNomiSurfaceRenderer
import ciyin.designsystem.nomi.text.DefaultNomiTextRenderer
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.theme.NomiTheme

/**
 * NomiKit 自身设计系统，绑定在 [NomiDesignSystem.Companion] 上。
 *
 * 通过 `NomiDesignSystem.Default` 访问；实现对象不对外暴露，调用方只依赖核心契约。
 */
val NomiDesignSystem.Companion.Default: NomiDesignSystem get() = DefaultNomiSystem

/**
 * NomiKit 自身设计系统的唯一实现；直接发布调用方传入的完整主题，不做字段级合并。
 */
private object DefaultNomiSystem : NomiDesignSystem {
    /**
     * Nomi 标识。
     */
    override val id = NomiDesignSystemId("nomi")

    /**
     * Nomi 组件族组装。
     */
    override val components: NomiDesignSystemComponents = object : NomiDesignSystemComponents {
        /**
         * 文本实现。
         */
        override val text = DefaultNomiTextRenderer

        /**
         * 按钮实现。
         */
        override val actions = DefaultNomiActionRenderer

        /**
         * 表面及卡片实现。
         */
        override val surfaces = DefaultNomiSurfaceRenderer

        /**
         * 导航套件实现；Bar、Rail 与 Drawer 三种布局都受支持。
         */
        override val navigation = DefaultNomiNavigationRenderer

        /**
         * 页面脚手架实现；顶栏、主体与底栏按 Nomi 自身令牌排布。
         */
        override val scaffold = DefaultNomiScaffoldRenderer

        /**
         * 列表项实现；行内边距、文本样式与内容色读取 Nomi 令牌。
         */
        override val listItems = DefaultNomiListItemRenderer
    }

    /**
     * 安装 Nomi 原生内容环境，并把本层主题发布为中立主题。
     *
     * @param theme 本层显式提供的中立主题；五组均为完整值，适配器不按字段回填默认值。
     * @param content 在 Nomi 原生内容色与正文排版环境中组合的子树。
     */
    @Composable
    override fun ProvideTheme(theme: NomiTheme, content: @Composable () -> Unit) {
        CompositionLocalProvider(
            LocalNomiNativeContentColor provides theme.colorScheme.textPrimary,
            LocalNomiNativeTextStyle provides theme.typography.bodyMedium,
            content = content,
        )
    }
}
