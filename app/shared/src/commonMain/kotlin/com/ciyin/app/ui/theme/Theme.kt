package com.ciyin.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.ciyin.app.ui.theme.DarkMode.Dark
import com.ciyin.app.ui.theme.DarkMode.Light
import com.ciyin.app.ui.theme.DarkMode.System


/**
 * 应用全局主题对象
 *
 * 提供对当前应用主题配置的只读访问。
 * 包含深色模式、颜色方案、排版、形状、间距和尺寸等设计系统令牌。
 */
object AppTheme {
    /** 当前的深色模式设置 */
    val darkMode: DarkMode
        @Composable @ReadOnlyComposable get() = LocalDarkMode.current

    /** 当前的颜色方案 */
    val colorScheme: AppColorScheme
        @Composable @ReadOnlyComposable get() = LocalColorScheme.current

    /** 当前的排版样式 */
    val typography: Typography
        @Composable @ReadOnlyComposable get() = LocalTypography.current

    /** 当前的形状样式 */
    val shapes: AppShapes
        @Composable @ReadOnlyComposable get() = LocalShapes.current

    /** 当前的间距样式 */
    val spacings: Spacings
        @Composable @ReadOnlyComposable get() = LocalSpacings.current

    /** 当前的尺寸样式 */
    val sizes: AppSizes
        @Composable @ReadOnlyComposable get() = LocalSizes.current
}

/**
 * 应用主题 Composable
 *
 * 应用程序的根主题包装器。负责初始化并提供设计系统所需的 CompositionLocal 环境。
 * 内部封装了 MaterialTheme，并将自定义的设计令牌注入到组件树中。
 *
 * @param darkMode 深色模式设置，默认为 [System]（跟随系统）。
 * @param colorScheme 颜色方案，根据 [darkMode] 自动选择深色或浅色方案。
 * @param typography 排版配置，默认为全局定义的 [Typography]。
 * @param shapes 形状配置，默认为当前 [AppTheme.shapes]。
 * @param spacings 间距配置，默认为当前 [AppTheme.spacings]。
 * @param sizes 尺寸配置，默认为当前 [AppTheme.sizes]。
 * @param content 需要应用主题的内容 lambda。
 */
@Composable
fun AppTheme(
    darkMode: DarkMode = System,
    colorScheme: AppColorScheme = when (darkMode) {
        System -> if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
        Light -> lightColorScheme()
        Dark -> darkColorScheme()
    },
    typography: Typography = AppTheme.typography,
    shapes: AppShapes = AppTheme.shapes,
    spacings: Spacings = AppTheme.spacings,
    sizes: AppSizes = AppTheme.sizes,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme.toMaterialColorScheme(darkMode),
        shapes = shapes.toMaterialShapes(),
        typography = typography,
    ) {
        CompositionLocalProvider(
            LocalDarkMode provides darkMode,
            LocalColorScheme provides colorScheme,
            LocalTypography provides typography,
            LocalSpacings provides spacings,
            LocalShapes provides shapes,
            LocalSizes provides sizes,
        ) {
            content()
        }
    }
}

