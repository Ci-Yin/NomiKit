package com.ciyin.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


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
 * @property surface 表面色，用于卡片、对话框等表面元素
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
    primary = LightPrimary,
    onPrimary = LightOnPrimary,

    secondary = LightSecondary,
    onSecondary = LightOnSecondary,

    // 表面色
    background = LightBackground,
    onBackground = LightOnBackground,

    surface = LightSurface,
    onSurface = LightOnSurface,

    // 语义色
    error = LightError,
    onError = LightOnError,

    warning = LightWarning,
    onWarning = LightOnWarning,

    success = LightSuccess,
    onSuccess = LightOnSuccess,

    info = LightInfo,
    onInfo = LightOnInfo,

    // 文本色
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textHint = LightTextHint,
    textCaption = LightTextCaption,
    textDisabled = LightTextDisabled,

    // 中性色
    outline = LightOutline,
    divider = LightDivider,
)

/**
 * 深色主题
 */
fun darkColorScheme(): AppColorScheme = AppColorScheme(
    // 主色
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,

    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,

    // 表面色
    background = DarkBackground,
    onBackground = DarkOnBackground,

    surface = DarkSurface,
    onSurface = DarkOnSurface,

    // 语义色
    error = DarkError,
    onError = DarkOnError,

    warning = DarkWarning,
    onWarning = DarkOnWarning,

    success = DarkSuccess,
    onSuccess = DarkOnSuccess,

    info = DarkInfo,
    onInfo = DarkOnInfo,

    // 文本色
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textHint = DarkTextHint,
    textCaption = DarkTextCaption,
    textDisabled = DarkTextDisabled,

    // 中性色
    outline = DarkOutline,
    divider = DarkDivider,
)

/**
 * CompositionLocal 用于在组件树中传递 [AppColorScheme] 配色方案。
 *
 * 该值的设置通常作为 [AppTheme] 的一部分来完成。要获取此 CompositionLocal 的当前值，
 * 可以使用 [AppTheme.colorScheme] 来访问。
 */
internal val LocalColorScheme = staticCompositionLocalOf { lightColorScheme() }