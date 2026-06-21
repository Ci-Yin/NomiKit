package ciyin.material.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import ciyin.material.theme.DarkMode.Dark
import ciyin.material.theme.DarkMode.Light
import ciyin.material.theme.DarkMode.System


/**
 * 应用颜色方案
 *
 * 简化的颜色系统，包含必要的颜色类别：
 * - 主色（Primary/Secondary）：品牌主色和次要色
 * - 表面色（Surface/Background）：背景和卡片表面
 * - 语义色（Error/Warning/Success/Info）：状态提示颜色
 * - 文本色（Text）：主要、次要、提示、说明、禁用五级文本颜色
 * - 中性色（Outline/Divider）：边框和分隔线
 *
 * @property primary 主色，用于按钮、链接等主要交互元素
 * @property onPrimary 主色上的内容颜色（通常是文字或图标）
 * @property secondary 次要色，用于次要的交互元素
 * @property onSecondary 次要色上的内容颜色
 * @property background 主背景色，用于整个应用背景
 * @property onBackground 背景上的内容颜色（通常是文字）
 * @property surface 中层容器表面色，用于卡片、对话框等标准容器
 * @property surfaceHigh 更高层级的表面色，用于比基础表面更突出的容器
 * @property surfaceHigher 最高层级的表面色，用于最突出的容器
 * @property surfaceLow 较低层级的表面色，用于轻量容器
 * @property surfaceLower 最低层级的表面色，用于最贴近背景的容器
 * @property onSurface 表面上的内容颜色
 * @property error 错误状态颜色
 * @property onError 错误色上的内容颜色
 * @property warning 警告状态颜色
 * @property onWarning 警告色上的内容颜色
 * @property success 成功状态颜色
 * @property onSuccess 成功色上的内容颜色
 * @property info 信息状态颜色
 * @property onInfo 信息色上的内容颜色
 * @property textPrimary 主要文本颜色，用于正文内容
 * @property textSecondary 次要文本颜色，用于辅助说明文字
 * @property textHint 提示文本颜色，用于占位符、提示文字等
 * @property textCaption 说明文本颜色，用于图注、辅助说明等
 * @property textDisabled 禁用文本颜色，用于禁用状态的文字
 * @property outline 边框颜色，用于输入框、卡片边框等
 * @property divider 分隔线颜色，用于列表项分隔等
 */
@Immutable
@ConsistentCopyVisibility
data class AppColorScheme internal constructor(
    // ========== 主色 ==========
    val primary: Color,
    val onPrimary: Color,

    val secondary: Color,
    val onSecondary: Color,

    // ========== 表面色 ==========
    val background: Color,
    val onBackground: Color,

    val surface: Color,
    val surfaceHigh: Color,
    val surfaceHigher: Color,
    val surfaceLow: Color,
    val surfaceLower: Color,
    val onSurface: Color,

    // ========== 语义色 ==========
    val error: Color,
    val onError: Color,

    val warning: Color,
    val onWarning: Color,

    val success: Color,
    val onSuccess: Color,

    val info: Color,
    val onInfo: Color,

    // ========== 文本色 ==========
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val textCaption: Color,
    val textDisabled: Color,

    // ========== 中性色 ==========
    val outline: Color,
    val divider: Color,
)

/**
 * 浅色主题
 */
fun lightColorScheme(): AppColorScheme = AppColorScheme(
    // 主色
    primary = LightThemeColors.primary,
    onPrimary = LightThemeColors.onPrimary,

    secondary = LightThemeColors.secondary,
    onSecondary = LightThemeColors.onSecondary,

    // 表面色
    background = LightThemeColors.background,
    onBackground = LightThemeColors.onBackground,

    surface = LightThemeColors.surface,
    surfaceHigh = LightThemeColors.surfaceHigh,
    surfaceHigher = LightThemeColors.surfaceHigher,
    surfaceLow = LightThemeColors.surfaceLow,
    surfaceLower = LightThemeColors.surfaceLower,
    onSurface = LightThemeColors.onSurface,

    // 语义色
    error = LightThemeColors.error,
    onError = LightThemeColors.onError,

    warning = LightThemeColors.warning,
    onWarning = LightThemeColors.onWarning,

    success = LightThemeColors.success,
    onSuccess = LightThemeColors.onSuccess,

    info = LightThemeColors.info,
    onInfo = LightThemeColors.onInfo,

    // 文本色
    textPrimary = LightThemeColors.textPrimary,
    textSecondary = LightThemeColors.textSecondary,
    textHint = LightThemeColors.textHint,
    textCaption = LightThemeColors.textCaption,
    textDisabled = LightThemeColors.textDisabled,

    // 中性色
    outline = LightThemeColors.outline,
    divider = LightThemeColors.divider,
)

/**
 * 深色主题
 */
fun darkColorScheme(): AppColorScheme = AppColorScheme(
    // 主色
    primary = DarkThemeColors.primary,
    onPrimary = DarkThemeColors.onPrimary,

    secondary = DarkThemeColors.secondary,
    onSecondary = DarkThemeColors.onSecondary,

    // 表面色
    background = DarkThemeColors.background,
    onBackground = DarkThemeColors.onBackground,

    surface = DarkThemeColors.surface,
    surfaceHigh = DarkThemeColors.surfaceHigh,
    surfaceHigher = DarkThemeColors.surfaceHigher,
    surfaceLow = DarkThemeColors.surfaceLow,
    surfaceLower = DarkThemeColors.surfaceLower,
    onSurface = DarkThemeColors.onSurface,

    // 语义色
    error = DarkThemeColors.error,
    onError = DarkThemeColors.onError,

    warning = DarkThemeColors.warning,
    onWarning = DarkThemeColors.onWarning,

    success = DarkThemeColors.success,
    onSuccess = DarkThemeColors.onSuccess,

    info = DarkThemeColors.info,
    onInfo = DarkThemeColors.onInfo,

    // 文本色
    textPrimary = DarkThemeColors.textPrimary,
    textSecondary = DarkThemeColors.textSecondary,
    textHint = DarkThemeColors.textHint,
    textCaption = DarkThemeColors.textCaption,
    textDisabled = DarkThemeColors.textDisabled,

    // 中性色
    outline = DarkThemeColors.outline,
    divider = DarkThemeColors.divider,
)

/**
 * 将 [AppColorScheme] 转换为 Material3 的 [ColorScheme]。
 *
 * 将应用自定义颜色方案映射到 Material3 的标准颜色方案，用于 MaterialTheme。
 */
@Composable
internal fun AppColorScheme.toMaterialColorScheme(darkMode: DarkMode): ColorScheme {
    val lightColorScheme = androidx.compose.material3.lightColorScheme()
    val darkColorScheme = androidx.compose.material3.darkColorScheme()
    val colorScheme = when (darkMode) {
        Light -> lightColorScheme
        Dark -> darkColorScheme
        System -> if (isSystemInDarkTheme()) darkColorScheme else lightColorScheme
    }
    return colorScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        error = error,
        onError = onError,
        background = background,
        onBackground = onBackground,
        surface = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surfaceHigh,
        surfaceContainerHighest = surfaceHigher,
        surfaceContainerLow = surfaceLow,
        surfaceContainerLowest = surfaceLower,
        onSurface = onSurface,
        outline = outline,
    )
}

/**
 * CompositionLocal 用于在组件树中传递 [AppColorScheme] 配色方案。
 *
 * 该值的设置通常作为 [AppTheme] 的一部分来完成。要获取此 CompositionLocal 的当前值，
 * 可以使用 [AppTheme.colorScheme] 来访问。
 */
internal val LocalColorScheme = staticCompositionLocalOf { lightColorScheme() }
