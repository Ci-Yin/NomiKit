package ciyin.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiDesignSystem
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.NomiDesignSystem

/**
 * 当前设计系统发布的中立主题值，也是主题入口唯一的显式主题输入。
 *
 * 五组均为完整值，不做字段级合并；[isDark] 是已经解析的明暗结果，`DarkMode.System`
 * 由主题入口在省略 `theme` 时解析一次。
 *
 * @property isDark 是否使用深色令牌。
 * @property colorScheme 完整中立色板。
 * @property typography 完整十五角色中立排版。
 * @property shapes 完整九级中立圆角。
 * @property spacings 完整页面间距。
 * @property sizes 完整布局尺寸。
 */
@Immutable
data class NomiTheme(
    val isDark: Boolean = false,
    val colorScheme: NomiColorScheme = defaultLightNomiColorScheme(),
    val typography: NomiTypography = defaultNomiTypography(),
    val shapes: NomiShapes = defaultNomiShapes(),
    val spacings: Spacings = defaultSpacings(),
    val sizes: NomiSizes = defaultNomiSizes(),
) {
    /**
     * 当前分组读取入口与浅色/深色默认工厂。
     */
    companion object {
        /**
         * 当前主题的颜色组。
         */
        val colorScheme: NomiColorScheme
            @Composable @ReadOnlyComposable get() = LocalNomiTheme.current.colorScheme

        /**
         * 当前主题的排版组。
         */
        val typography: NomiTypography
            @Composable @ReadOnlyComposable get() = LocalNomiTheme.current.typography

        /**
         * 当前主题的形状组。
         */
        val shapes: NomiShapes
            @Composable @ReadOnlyComposable get() = LocalNomiTheme.current.shapes

        /**
         * 当前主题的间距组。
         */
        val spacings: Spacings
            @Composable @ReadOnlyComposable get() = LocalNomiTheme.current.spacings

        /**
         * 当前主题的尺寸组。
         */
        val sizes: NomiSizes
            @Composable @ReadOnlyComposable get() = LocalNomiTheme.current.sizes

        /**
         * 当前主题是否使用深色令牌。
         */
        val isDark: Boolean
            @Composable @ReadOnlyComposable get() = LocalNomiTheme.current.isDark

        /**
         * 浅色默认主题。
         *
         * @return 浅色色板与浅色标记的默认主题。
         */
        fun light(): NomiTheme =
            NomiTheme(colorScheme = defaultLightNomiColorScheme(), isDark = false)

        /**
         * 深色默认主题。
         *
         * @return 深色色板与深色标记的默认主题。
         */
        fun dark(): NomiTheme =
            NomiTheme(colorScheme = defaultDarkNomiColorScheme(), isDark = true)
    }
}

/**
 * 当前组合分支发布的中立主题；未提供时回退浅色默认值，供 Preview 与孤立组件读取。
 */
val LocalNomiTheme = staticCompositionLocalOf { NomiTheme() }

/**
 * 设计系统主题入口：按明暗请求选择默认主题，或安装显式主题，再映射为原生主题。
 *
 * 入口发布 [LocalNomiTheme] 与 [LocalNomiDesignSystem]，并重置显式内容 slot。
 * [designSystem] 必填，没有隐式回退；[darkMode] 只在 [theme] 省略时生效，
 * 显式传入 [theme] 时按原值安装。嵌套继承需显式传 `theme = LocalNomiTheme.current`。
 *
 * @param designSystem 负责把主题映射为原生主题的实现。
 * @param darkMode 请求的明暗模式；[DarkMode.System] 在省略 [theme] 时解析一次。
 * @param theme 本层安装的主题；默认按 [darkMode] 取浅色或深色默认值。
 * @param content 在目标原生主题与中立主题环境中组合的内容。
 */
@Composable
fun NomiTheme(
    designSystem: NomiDesignSystem,
    darkMode: DarkMode = DarkMode.System,
    theme: NomiTheme = rememberNomiTheme(darkMode),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalNomiDesignSystem provides designSystem,
        LocalNomiTheme provides theme,
        LocalNomiContentColor provides Color.Unspecified,
        LocalNomiTextStyle provides TextStyle.Default,
    ) {
        designSystem.ProvideTheme(theme, content)
    }
}

/**
 * 按明暗请求解析一次浅色或深色默认主题。
 *
 * @param darkMode 请求的明暗模式；[DarkMode.System] 在此读取系统状态。
 * @return 浅色或深色默认主题。
 */
@Composable
private fun rememberNomiTheme(darkMode: DarkMode): NomiTheme {
    val isDark = darkMode.resolveIsDark()
    return remember(isDark) { if (isDark) NomiTheme.dark() else NomiTheme.light() }
}

/**
 * 把明暗请求解析为一次布尔结果；[DarkMode.System] 只在主题边界读取系统状态。
 *
 * @return 深色请求为 true，浅色请求为 false。
 */
@Composable
private fun DarkMode.resolveIsDark(): Boolean = when (this) {
    DarkMode.System -> isSystemInDarkTheme()
    DarkMode.Light -> false
    DarkMode.Dark -> true
}
