package ciyin.material.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.material3.Material3Theme
import ciyin.designsystem.material3.toMaterial3ColorScheme
import ciyin.designsystem.material3.toMaterial3Shapes
import ciyin.designsystem.material3.toMaterial3Typography
import ciyin.designsystem.material3.toNomiTypography
import ciyin.designsystem.runtime.LocalNomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import ciyin.material.theme.DarkMode.Dark
import ciyin.material.theme.DarkMode.Light
import ciyin.material.theme.DarkMode.System

/**
 * 旧主题只读入口。
 *
 * 颜色、排版、形状、间距和尺寸直接读取当前中立主题；Material 3 环境中的排版 getter 仍返回
 * 实际原生 Typography 实例，保留原生专属角色与对象身份。
 */
object AppTheme {
    /**
     * 当前组合上下文已解析的明暗模式，映射为旧枚举类型。
     *
     * 主题值只保存已解析的 `isDark`，因此本 getter 不会返回 [DarkMode.System]；
     * 跟随系统的请求由 [AppTheme] 参数在进入主题前解析，未进入主题时返回浅色默认值。
     */
    val darkMode: DarkMode
        @Composable @ReadOnlyComposable get() = NomiTheme.isDark.toLegacyDarkMode()

    /**
     * 当前组合上下文的完整中立颜色投影，不独立维护旧包颜色状态。
     */
    val colorScheme: AppColorScheme
        @Composable @ReadOnlyComposable get() = NomiTheme.colorScheme

    /**
     * 当前 Material 排版。
     *
     * Material 3 环境返回完整原生实例；其他环境（含未提供设计系统时）将中立排版转换为旧类型，
     * 保留无主题时的旧默认值。
     */
    val typography: Typography
        @Composable @ReadOnlyComposable get() =
            if (LocalNomiDesignSystem.current?.id == NomiDesignSystem.Material3.id) {
                MaterialTheme.typography
            } else {
                NomiTheme.typography.toMaterial3Typography(base = DefaultLegacyTypography)
            }

    /**
     * 当前组合上下文的九级中立圆角投影，来源于当前生效主题。
     */
    val shapes: AppShapes
        @Composable @ReadOnlyComposable get() = NomiTheme.shapes

    /**
     * 当前组合上下文的中立间距投影，以 dp 度量表示布局留白。
     */
    val spacings: Spacings
        @Composable @ReadOnlyComposable get() = NomiTheme.spacings

    /**
     * 当前组合上下文的中立尺寸投影，聚合图标、头像、描边与布局度量。
     */
    val sizes: AppSizes
        @Composable @ReadOnlyComposable get() = NomiTheme.sizes
}

/**
 * 为内容建立旧参数语义的 Material 3 主题，并向中立组件提供兼容五组投影。
 *
 * 先把旧 [DarkMode] 解析为实际明暗，再用参数组装完整中立 `NomiTheme`，经 Material 3 适配器
 * 映射为原生主题并发布投影。省略颜色时按请求模式重新选择旧默认色，不继承父颜色；其余省略的令牌
 * 读取调用处的当前值。完整 [Typography] 实例原样交给 Material 3，不以中立角色往返转换替换；
 * 该映射随颜色、排版与形状参数记忆，避免父级重组时反复替换设计系统实例。
 *
 * @param darkMode 请求的明暗模式；默认继承 [AppTheme.darkMode]，[DarkMode.System] 根据系统状态解析。
 * @param colorScheme 完整颜色方案；默认按 darkMode 调用旧浅色或深色工厂，显式方案不会随模式改写。
 * @param typography 完整 Material 3 排版对象；默认读取 [AppTheme.typography]，包括原生专属角色。
 * @param shapes 完整九级圆角方案；默认读取调用处的 [AppTheme.shapes]。
 * @param spacings 完整间距方案；默认读取调用处的 [AppTheme.spacings]。
 * @param sizes 完整尺寸方案；默认读取调用处的 [AppTheme.sizes]，不会自动强制所有原生控件采用其度量。
 * @param content 在已建立的 Material 3 原生主题与中立主题上下文内组合的内容；无额外布局接收者，业务状态由调用方持有。
 */
@Composable
fun AppTheme(
    darkMode: DarkMode = AppTheme.darkMode,
    colorScheme: AppColorScheme = when (darkMode) {
        System -> if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
        Light -> lightColorScheme()
        Dark -> darkColorScheme()
    },
    typography: Typography = AppTheme.typography,
    shapes: AppShapes = AppTheme.shapes,
    spacings: Spacings = AppTheme.spacings,
    sizes: AppSizes = AppTheme.sizes,
    content: @Composable () -> Unit,
) {
    val isDark = when (darkMode) {
        System -> isSystemInDarkTheme()
        Light -> false
        Dark -> true
    }
    val theme = NomiTheme(
        colorScheme = colorScheme,
        typography = typography.toNomiTypography(),
        shapes = shapes,
        spacings = spacings,
        sizes = sizes,
        isDark = isDark,
    )
    val designSystem = remember(colorScheme, typography, shapes) {
        val map: NomiTheme.() -> Material3Theme = {
            Material3Theme(
                isDark = this.isDark,
                colorScheme = this.colorScheme.toMaterial3ColorScheme(this.isDark),
                typography = typography,
                shapes = shapes.toMaterial3Shapes(),
            )
        }
        NomiDesignSystem.Material3(map)
    }
    NomiTheme(theme = theme, designSystem = designSystem) {
        content()
    }
}
