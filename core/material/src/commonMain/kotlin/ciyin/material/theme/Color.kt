package ciyin.material.theme

import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme

/**
 * 旧浅色工厂的默认颜色快照，初始化时从中立默认工厂创建。
 *
 * 不读取当前主题；同一对象供后续旧工厂调用提取默认字段，颜色数值只由中立核心维护。
 */
internal val LightThemeColors: AppColorScheme = defaultLightNomiColorScheme()

/**
 * 旧深色工厂的默认颜色快照，初始化时从中立默认工厂创建。
 *
 * 不读取当前主题；同一对象供后续旧工厂调用提取默认字段，颜色数值只由中立核心维护。
 */
internal val DarkThemeColors: AppColorScheme = defaultDarkNomiColorScheme()
