package ciyin.designsystem.nomi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.nomi.button.DefaultNomiActionRenderer
import ciyin.designsystem.nomi.navigation.DefaultNomiNavigationRenderer
import ciyin.designsystem.nomi.surface.DefaultNomiSurfaceRenderer
import ciyin.designsystem.nomi.text.DefaultNomiTextRenderer
import ciyin.designsystem.runtime.ResolvedNomiTheme
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultSpacings
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.runtime.NomiThemeRequest

/**
 * NomiKit 自身设计系统的唯一实现；其默认令牌不再作为其他 adapter 的默认输入。
 */
internal object DefaultNomiSystem : NomiDesignSystem {
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
         * 导航套件实现；Bar 与 Rail 受支持，Drawer 由能力声明禁用。
         */
        override val navigation = DefaultNomiNavigationRenderer
    }

    /**
     * 构建自身默认值并只应用显式覆盖，再提供中立投影。
     *
     * 非空覆盖组整体替换默认组，accent 仅覆盖所选色板的 primary。
     *
     * @param request 使用 isDark 选择 Nomi 明暗默认值，并读取显式覆盖；requestedMode 由外层主题边界记录。
     * @param content 在 Nomi 原生内容色和正文排版环境中组合的内容，接收直接包含五组数据的解析结果。
     */
    @Composable
    override fun ProvideTheme(
        request: NomiThemeRequest,
        content: @Composable (ResolvedNomiTheme) -> Unit
    ) {
        val overrides = request.overrides
        val baseColors = overrides.colorScheme
            ?: if (request.isDark) defaultDarkNomiColorScheme() else defaultLightNomiColorScheme()
        val resolved = ResolvedNomiTheme(
            requestedMode = request.requestedMode,
            isDark = request.isDark,
            overrides = overrides,
            colorScheme = if (overrides.accentColor == Color.Unspecified) baseColors else {
                baseColors.copy(primary = overrides.accentColor)
            },
            typography = overrides.typography ?: defaultNomiTypography(),
            shapes = overrides.shapes ?: defaultNomiShapes(),
            spacings = overrides.spacings ?: defaultSpacings(),
            sizes = overrides.sizes ?: defaultNomiSizes(),
        )
        CompositionLocalProvider(
            LocalNomiNativeContentColor provides resolved.colorScheme.textPrimary,
            LocalNomiNativeTextStyle provides resolved.typography.bodyMedium,
        ) { content(resolved) }
    }
}

/**
 * NomiKit 自身设计系统，绑定在 [NomiDesignSystem.Companion] 上。
 *
 * 通过 `NomiDesignSystem.Default` 访问；实现对象不对外暴露，调用方只依赖核心契约。
 */
val NomiDesignSystem.Companion.Default: NomiDesignSystem get() = DefaultNomiSystem
