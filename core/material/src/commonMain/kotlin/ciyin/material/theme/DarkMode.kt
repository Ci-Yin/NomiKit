package ciyin.material.theme

/**
 * 旧主题的深色模式枚举。
 *
 * 保留独立枚举以兼容直接导入 DarkMode.Light、Dark、System 的源码；不使用枚举 typealias。
 */
enum class DarkMode {
    /**
     * 强制浅色模式。
     */
    Light,

    /**
     * 强制深色模式。
     */
    Dark,

    /**
     * 跟随系统设置。
     */
    System;
}

/**
 * 把已解析的中立明暗映射回旧枚举，保留旧属性类型。
 *
 * @receiver 中立主题记录的实际明暗标记。
 * @return 深色为 [DarkMode.Dark]，浅色为 [DarkMode.Light]。
 */
internal fun Boolean.toLegacyDarkMode(): DarkMode = if (this) DarkMode.Dark else DarkMode.Light
