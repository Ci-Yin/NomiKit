package ciyin.material.theme

import androidx.compose.ui.graphics.Color
import ciyin.designsystem.theme.NomiColorScheme as DesignSystemColorScheme
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme

/**
 * 旧颜色类型的源码兼容别名，承载主色、表面色、状态色、文本色与边框色。
 *
 * 使用 [lightColorScheme]、[darkColorScheme] 和生成的成员 copy创建或复制中立模型。
 */
typealias AppColorScheme = DesignSystemColorScheme

/**
 * 创建基于旧浅色默认值的完整颜色方案。
 *
 * 未传参数沿用中立工厂 [defaultLightNomiColorScheme] 的浅色默认值，不读取当前主题或系统明暗模式。
 * 显式传入的颜色原样保存，包括 [Color.Unspecified] 与 [Color.Transparent]；二者都不会触发默认色回填。
 * 此工厂保留旧配色，不代表 Material 3 的原生默认色板。
 *
 * @param primary 主色，用于按钮、链接等主要交互元素。
 * @param onPrimary 主色上的文字、图标等内容颜色。
 * @param secondary 次要交互元素使用的辅助色。
 * @param onSecondary 次要色上的内容颜色。
 * @param background 应用主背景色。
 * @param onBackground 主背景上的内容颜色。
 * @param surface 卡片、对话框等标准容器的中层表面色。
 * @param surfaceHigh 比基础表面更突出的高层容器表面色。
 * @param surfaceHigher 最高层级容器的表面色。
 * @param surfaceLow 轻量容器使用的较低层级表面色。
 * @param surfaceLower 最贴近背景的最低层级表面色。
 * @param onSurface 容器表面上的内容颜色。
 * @param error 错误状态提示色。
 * @param onError 错误色上的内容颜色。
 * @param warning 警告状态提示色。
 * @param onWarning 警告色上的内容颜色。
 * @param success 成功状态提示色。
 * @param onSuccess 成功色上的内容颜色。
 * @param info 一般信息提示色。
 * @param onInfo 信息色上的内容颜色。
 * @param textPrimary 正文等主要文本的颜色。
 * @param textSecondary 辅助文本的颜色。
 * @param textHint 占位符、输入提示等提示文本的颜色。
 * @param textCaption 图注、说明等补充文本的颜色。
 * @param textDisabled 禁用状态文本的颜色。
 * @param outline 输入框、卡片等元素的边框颜色。
 * @param divider 列表项等内容之间的分隔线颜色。
 * @return 应用上述颜色的新 [AppColorScheme] 实例，未指定字段为浅色默认值。
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
): AppColorScheme = defaultLightNomiColorScheme(
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
 * 创建基于旧深色默认值的完整颜色方案。
 *
 * 未传参数沿用中立工厂 [defaultDarkNomiColorScheme] 的深色默认值，不读取当前主题或系统明暗模式。
 * 显式传入的颜色原样保存，包括 [Color.Unspecified] 与 [Color.Transparent]；二者都不会触发默认色回填。
 * 此工厂保留旧配色，不代表 Material 3 的原生默认色板。
 *
 * @param primary 主色，用于按钮、链接等主要交互元素。
 * @param onPrimary 主色上的文字、图标等内容颜色。
 * @param secondary 次要交互元素使用的辅助色。
 * @param onSecondary 次要色上的内容颜色。
 * @param background 应用主背景色。
 * @param onBackground 主背景上的内容颜色。
 * @param surface 卡片、对话框等标准容器的中层表面色。
 * @param surfaceHigh 比基础表面更突出的高层容器表面色。
 * @param surfaceHigher 最高层级容器的表面色。
 * @param surfaceLow 轻量容器使用的较低层级表面色。
 * @param surfaceLower 最贴近背景的最低层级表面色。
 * @param onSurface 容器表面上的内容颜色。
 * @param error 错误状态提示色。
 * @param onError 错误色上的内容颜色。
 * @param warning 警告状态提示色。
 * @param onWarning 警告色上的内容颜色。
 * @param success 成功状态提示色。
 * @param onSuccess 成功色上的内容颜色。
 * @param info 一般信息提示色。
 * @param onInfo 信息色上的内容颜色。
 * @param textPrimary 正文等主要文本的颜色。
 * @param textSecondary 辅助文本的颜色。
 * @param textHint 占位符、输入提示等提示文本的颜色。
 * @param textCaption 图注、说明等补充文本的颜色。
 * @param textDisabled 禁用状态文本的颜色。
 * @param outline 输入框、卡片等元素的边框颜色。
 * @param divider 列表项等内容之间的分隔线颜色。
 * @return 应用上述颜色的新 [AppColorScheme] 实例，未指定字段为深色默认值。
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
): AppColorScheme = defaultDarkNomiColorScheme(
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
