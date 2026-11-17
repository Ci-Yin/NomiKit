package ciyin.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 应用颜色方案。
 *
 * 支持公开构造和编译器生成的成员 copy，无需导入复制扩展。
 * 直接构造时未传字段使用 Nomi 浅色默认值；深色基线使用 [defaultDarkNomiColorScheme]。
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
data class NomiColorScheme(
    // ========== 主色 ==========
    val primary: Color = LightThemeColors.primary,
    val onPrimary: Color = LightThemeColors.onPrimary,

    val secondary: Color = LightThemeColors.secondary,
    val onSecondary: Color = LightThemeColors.onSecondary,

    // ========== 表面色 ==========
    val background: Color = LightThemeColors.background,
    val onBackground: Color = LightThemeColors.onBackground,

    val surface: Color = LightThemeColors.surface,
    val surfaceHigh: Color = LightThemeColors.surfaceHigh,
    val surfaceHigher: Color = LightThemeColors.surfaceHigher,
    val surfaceLow: Color = LightThemeColors.surfaceLow,
    val surfaceLower: Color = LightThemeColors.surfaceLower,
    val onSurface: Color = LightThemeColors.onSurface,

    // ========== 语义色 ==========
    val error: Color = LightThemeColors.error,
    val onError: Color = LightThemeColors.onError,

    val warning: Color = LightThemeColors.warning,
    val onWarning: Color = LightThemeColors.onWarning,

    val success: Color = LightThemeColors.success,
    val onSuccess: Color = LightThemeColors.onSuccess,

    val info: Color = LightThemeColors.info,
    val onInfo: Color = LightThemeColors.onInfo,

    // ========== 文本色 ==========
    val textPrimary: Color = LightThemeColors.textPrimary,
    val textSecondary: Color = LightThemeColors.textSecondary,
    val textHint: Color = LightThemeColors.textHint,
    val textCaption: Color = LightThemeColors.textCaption,
    val textDisabled: Color = LightThemeColors.textDisabled,

    // ========== 中性色 ==========
    val outline: Color = LightThemeColors.outline,
    val divider: Color = LightThemeColors.divider,
)

/**
 * 创建基于 NomiKit 默认浅色色板的完整颜色方案。
 *
 * 未传参数沿用内置浅色值，可通过命名参数替换任意颜色。本函数不读取当前主题或系统明暗模式，
 * 也不代表任何原生设计系统的默认色板；传给 [NomiTheme] 时表示明确提供整组颜色。
 * 显式传入的 [Color.Unspecified] 和 [Color.Transparent] 均原样写入，不触发默认颜色回填。
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
 * @return 采用上述颜色值的新完整色板；未传字段取本工厂对应的内置明暗默认值。
 */
fun defaultLightNomiColorScheme(
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
): NomiColorScheme = NomiColorScheme(
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
 * 创建基于 NomiKit 默认深色色板的完整颜色方案。
 *
 * 未传参数沿用内置深色值，可通过命名参数替换任意颜色。本函数不读取当前主题或系统明暗模式，
 * 也不代表任何原生设计系统的默认色板；传给 [NomiTheme] 时表示明确提供整组颜色。
 * 显式传入的 [Color.Unspecified] 和 [Color.Transparent] 均原样写入，不触发默认颜色回填。
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
 * @return 采用上述颜色值的新完整色板；未传字段取本工厂对应的内置明暗默认值。
 */
fun defaultDarkNomiColorScheme(
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
): NomiColorScheme = NomiColorScheme(
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
