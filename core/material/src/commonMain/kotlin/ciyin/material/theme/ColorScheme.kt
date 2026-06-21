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
 * 创建浅色主题颜色方案。
 *
 * 所有参数默认使用内置浅色主题令牌，调用方可以通过命名参数覆盖任意颜色。
 *
 * @param primary 主色，用于按钮、链接等主要交互元素
 * @param onPrimary 主色上的内容颜色
 * @param secondary 次要色，用于次要的交互元素
 * @param onSecondary 次要色上的内容颜色
 * @param background 主背景色，用于整个应用背景
 * @param onBackground 背景上的内容颜色
 * @param surface 中层容器表面色，用于卡片、对话框等标准容器
 * @param surfaceHigh 更高层级的表面色，用于比基础表面更突出的容器
 * @param surfaceHigher 最高层级的表面色，用于最突出的容器
 * @param surfaceLow 较低层级的表面色，用于轻量容器
 * @param surfaceLower 最低层级的表面色，用于最贴近背景的容器
 * @param onSurface 表面上的内容颜色
 * @param error 错误状态颜色
 * @param onError 错误色上的内容颜色
 * @param warning 警告状态颜色
 * @param onWarning 警告色上的内容颜色
 * @param success 成功状态颜色
 * @param onSuccess 成功色上的内容颜色
 * @param info 信息状态颜色
 * @param onInfo 信息色上的内容颜色
 * @param textPrimary 主要文本颜色，用于正文内容
 * @param textSecondary 次要文本颜色，用于辅助说明文字
 * @param textHint 提示文本颜色，用于占位符、提示文字等
 * @param textCaption 说明文本颜色，用于图注、辅助说明等
 * @param textDisabled 禁用文本颜色，用于禁用状态的文字
 * @param outline 边框颜色，用于输入框、卡片边框等
 * @param divider 分隔线颜色，用于列表项分隔等
 */
fun lightColorScheme(
    primary: Color = LightThemeColors.primary,
    onPrimary: Color = LightThemeColors.onPrimary,
    secondary: Color = LightThemeColors.secondary,
    onSecondary: Color = LightThemeColors.onSecondary,
    background: Color = LightThemeColors.background,
    onBackground: Color = LightThemeColors.onBackground,
    surface: Color = LightThemeColors.surface,
    surfaceHigh: Color = LightThemeColors.surfaceHigh,
    surfaceHigher: Color = LightThemeColors.surfaceHigher,
    surfaceLow: Color = LightThemeColors.surfaceLow,
    surfaceLower: Color = LightThemeColors.surfaceLower,
    onSurface: Color = LightThemeColors.onSurface,
    error: Color = LightThemeColors.error,
    onError: Color = LightThemeColors.onError,
    warning: Color = LightThemeColors.warning,
    onWarning: Color = LightThemeColors.onWarning,
    success: Color = LightThemeColors.success,
    onSuccess: Color = LightThemeColors.onSuccess,
    info: Color = LightThemeColors.info,
    onInfo: Color = LightThemeColors.onInfo,
    textPrimary: Color = LightThemeColors.textPrimary,
    textSecondary: Color = LightThemeColors.textSecondary,
    textHint: Color = LightThemeColors.textHint,
    textCaption: Color = LightThemeColors.textCaption,
    textDisabled: Color = LightThemeColors.textDisabled,
    outline: Color = LightThemeColors.outline,
    divider: Color = LightThemeColors.divider,
): AppColorScheme = AppColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    secondary = secondary,
    onSecondary = onSecondary,
    background = background,
    onBackground = onBackground,
    surface = surface,
    surfaceHigh = surfaceHigh,
    surfaceHigher = surfaceHigher,
    surfaceLow = surfaceLow,
    surfaceLower = surfaceLower,
    onSurface = onSurface,
    error = error,
    onError = onError,
    warning = warning,
    onWarning = onWarning,
    success = success,
    onSuccess = onSuccess,
    info = info,
    onInfo = onInfo,
    textPrimary = textPrimary,
    textSecondary = textSecondary,
    textHint = textHint,
    textCaption = textCaption,
    textDisabled = textDisabled,
    outline = outline,
    divider = divider,
)

/**
 * 创建深色主题颜色方案。
 *
 * 所有参数默认使用内置深色主题令牌，调用方可以通过命名参数覆盖任意颜色。
 *
 * @param primary 主色，用于按钮、链接等主要交互元素
 * @param onPrimary 主色上的内容颜色
 * @param secondary 次要色，用于次要的交互元素
 * @param onSecondary 次要色上的内容颜色
 * @param background 主背景色，用于整个应用背景
 * @param onBackground 背景上的内容颜色
 * @param surface 中层容器表面色，用于卡片、对话框等标准容器
 * @param surfaceHigh 更高层级的表面色，用于比基础表面更突出的容器
 * @param surfaceHigher 最高层级的表面色，用于最突出的容器
 * @param surfaceLow 较低层级的表面色，用于轻量容器
 * @param surfaceLower 最低层级的表面色，用于最贴近背景的容器
 * @param onSurface 表面上的内容颜色
 * @param error 错误状态颜色
 * @param onError 错误色上的内容颜色
 * @param warning 警告状态颜色
 * @param onWarning 警告色上的内容颜色
 * @param success 成功状态颜色
 * @param onSuccess 成功色上的内容颜色
 * @param info 信息状态颜色
 * @param onInfo 信息色上的内容颜色
 * @param textPrimary 主要文本颜色，用于正文内容
 * @param textSecondary 次要文本颜色，用于辅助说明文字
 * @param textHint 提示文本颜色，用于占位符、提示文字等
 * @param textCaption 说明文本颜色，用于图注、辅助说明等
 * @param textDisabled 禁用文本颜色，用于禁用状态的文字
 * @param outline 边框颜色，用于输入框、卡片边框等
 * @param divider 分隔线颜色，用于列表项分隔等
 */
fun darkColorScheme(
    primary: Color = DarkThemeColors.primary,
    onPrimary: Color = DarkThemeColors.onPrimary,
    secondary: Color = DarkThemeColors.secondary,
    onSecondary: Color = DarkThemeColors.onSecondary,
    background: Color = DarkThemeColors.background,
    onBackground: Color = DarkThemeColors.onBackground,
    surface: Color = DarkThemeColors.surface,
    surfaceHigh: Color = DarkThemeColors.surfaceHigh,
    surfaceHigher: Color = DarkThemeColors.surfaceHigher,
    surfaceLow: Color = DarkThemeColors.surfaceLow,
    surfaceLower: Color = DarkThemeColors.surfaceLower,
    onSurface: Color = DarkThemeColors.onSurface,
    error: Color = DarkThemeColors.error,
    onError: Color = DarkThemeColors.onError,
    warning: Color = DarkThemeColors.warning,
    onWarning: Color = DarkThemeColors.onWarning,
    success: Color = DarkThemeColors.success,
    onSuccess: Color = DarkThemeColors.onSuccess,
    info: Color = DarkThemeColors.info,
    onInfo: Color = DarkThemeColors.onInfo,
    textPrimary: Color = DarkThemeColors.textPrimary,
    textSecondary: Color = DarkThemeColors.textSecondary,
    textHint: Color = DarkThemeColors.textHint,
    textCaption: Color = DarkThemeColors.textCaption,
    textDisabled: Color = DarkThemeColors.textDisabled,
    outline: Color = DarkThemeColors.outline,
    divider: Color = DarkThemeColors.divider,
): AppColorScheme = AppColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    secondary = secondary,
    onSecondary = onSecondary,
    background = background,
    onBackground = onBackground,
    surface = surface,
    surfaceHigh = surfaceHigh,
    surfaceHigher = surfaceHigher,
    surfaceLow = surfaceLow,
    surfaceLower = surfaceLower,
    onSurface = onSurface,
    error = error,
    onError = onError,
    warning = warning,
    onWarning = onWarning,
    success = success,
    onSuccess = onSuccess,
    info = info,
    onInfo = onInfo,
    textPrimary = textPrimary,
    textSecondary = textSecondary,
    textHint = textHint,
    textCaption = textCaption,
    textDisabled = textDisabled,
    outline = outline,
    divider = divider,
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
