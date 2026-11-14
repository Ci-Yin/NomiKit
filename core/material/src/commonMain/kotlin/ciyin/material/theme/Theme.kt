package ciyin.material.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.material3.rememberLegacyMaterial3DesignSystem
import ciyin.designsystem.material3.toMaterial3Typography
import ciyin.designsystem.runtime.LocalNomiDesignSystem
import ciyin.designsystem.runtime.LocalResolvedNomiTheme
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme as DesignSystemTheme
import ciyin.designsystem.theme.NomiThemeOverrides
import ciyin.material.theme.DarkMode.Dark
import ciyin.material.theme.DarkMode.Light
import ciyin.material.theme.DarkMode.System

/**
 * 旧主题只读入口。
 *
 * 颜色、排版、形状、间距和尺寸均直接读取当前中立主题的五组结果；Material 3 环境中的
 * 排版 getter 仍返回实际原生 Typography 实例，保留原生专属角色与对象身份。
 */
object AppTheme {
    /**
     * 当前组合上下文请求的深浅色模式，映射为旧枚举类型。
     *
     * 跟随系统的请求仍为 [DarkMode.System]，不表示解析后的实际明暗值。
     */
    val darkMode: DarkMode
        @Composable @ReadOnlyComposable get() = LocalResolvedNomiTheme.current.requestedMode.toLegacyDarkMode()

    /**
     * 当前组合上下文的完整中立颜色投影，不独立维护旧包颜色状态。
     */
    val colorScheme: AppColorScheme
        @Composable @ReadOnlyComposable get() = DesignSystemTheme.colorScheme

    /**
     * 当前 Material 排版。
     *
     * Material3 环境返回完整原生实例；其他环境（含未提供设计系统时）将中立排版转换为旧类型，保留无主题时的旧默认值。
     */
    val typography: Typography
        @Composable @ReadOnlyComposable get() =
            if (LocalNomiDesignSystem.current?.id == NomiDesignSystem.Material3.id) {
                MaterialTheme.typography
            } else {
                DesignSystemTheme.typography.toMaterial3Typography(base = DefaultLegacyTypography)
            }

    /**
     * 当前组合上下文的九级中立圆角投影，来源于已解析主题。
     */
    val shapes: AppShapes
        @Composable @ReadOnlyComposable get() = DesignSystemTheme.shapes

    /**
     * 当前组合上下文的中立间距投影，以 dp 度量表示布局留白。
     */
    val spacings: Spacings
        @Composable @ReadOnlyComposable get() = DesignSystemTheme.spacings

    /**
     * 当前组合上下文的中立尺寸投影，聚合图标、头像、描边与布局度量。
     */
    val sizes: AppSizes
        @Composable @ReadOnlyComposable get() = DesignSystemTheme.sizes
}

/**
 * 为内容建立旧参数语义的 Material 3 主题，并向中立组件提供兼容五组投影。
 *
 * 省略颜色时按请求模式重新选择旧默认色，而不是继承父颜色；其余省略的令牌读取调用处的当前值。
 * 默认参数先在父环境求值，再隔离父级显式覆盖来源，避免父中立配置二次覆盖这些参数。
 * 完整 [Typography] 实例直接交给 Material 3，不以中立角色往返转换替换；此行为仅作用于本次内容范围。
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
    val designSystem = rememberLegacyMaterial3DesignSystem(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        spacings = spacings,
        sizes = sizes,
    )
    // 旧参数已在父环境求值；隔离父中立请求，避免再次覆盖完整 Material Typography 实例。
    val parent = LocalResolvedNomiTheme.current
    CompositionLocalProvider(
        LocalResolvedNomiTheme provides parent.copy(overrides = NomiThemeOverrides()),
    ) {
        DesignSystemTheme(
            designSystem = designSystem,
            darkMode = darkMode.toDesignSystemDarkMode(),
            content = content,
        )
    }
}
