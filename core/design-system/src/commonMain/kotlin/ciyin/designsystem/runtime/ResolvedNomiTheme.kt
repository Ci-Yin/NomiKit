package ciyin.designsystem.runtime

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiColorScheme
import ciyin.designsystem.theme.NomiShapes
import ciyin.designsystem.theme.NomiSizes
import ciyin.designsystem.theme.NomiThemeOverrides
import ciyin.designsystem.theme.NomiTypography
import ciyin.designsystem.theme.Spacings
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings

/**
 * 当前主题的五组解析结果及显式输入来源，不再套用额外的主题数据包装。
 *
 * 构造默认值只用于内置 Nomi 和无主题环境的兜底；其他设计系统应传入自己的原生映射。
 * 五组结果即使与默认值不同也不自动成为显式覆盖；跨系统继承只依据 [overrides]。
 * 核心主题边界发布适配器结果时，以请求重新确定模式和覆盖来源，保留五组结果对象。
 *
 * @property requestedMode 调用方请求的明暗模式，不从解析色板推导。
 * @property isDark 主题边界解析的实际模式；显式原生色板可以具有不同明暗，不据此修改输入。
 * @property colorScheme 当前完整中立色板，未提供时按 isDark 选择 Nomi 默认明暗色板。
 * @property typography 当前十五角色排版，默认使用 Nomi 自有排版，不反向覆盖其他原生系统。
 * @property shapes 当前九级圆角，默认使用 Nomi 自有形状，不强制改写原生组件形状。
 * @property spacings 当前页面间距，默认使用 Nomi 间距，不强制原生控件内部留白。
 * @property sizes 当前布局尺寸，默认使用 Nomi 尺寸，不强制原生控件高度。
 * @property overrides 已合并的显式覆盖来源；空配置不代表五组结果为空，也不继承结果中的默认值。
 */
@Immutable
data class ResolvedNomiTheme(
    val requestedMode: DarkMode,
    val isDark: Boolean,
    val colorScheme: NomiColorScheme = if (isDark) defaultDarkNomiColorScheme() else defaultLightNomiColorScheme(),
    val typography: NomiTypography = defaultNomiTypography(),
    val shapes: NomiShapes = defaultNomiShapes(),
    val spacings: Spacings = defaultSpacings(),
    val sizes: NomiSizes = defaultNomiSizes(),
    val overrides: NomiThemeOverrides = NomiThemeOverrides(),
)

/**
 * 未建立主题时使用固定浅色 Nomi 结果，保留 System 请求但不在 Local 工厂查询系统状态。
 */
val LocalResolvedNomiTheme = staticCompositionLocalOf {
    ResolvedNomiTheme(
        requestedMode = DarkMode.System,
        isDark = false,
    )
}
