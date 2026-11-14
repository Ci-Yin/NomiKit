package ciyin.material.theme

import ciyin.designsystem.theme.DarkMode as DesignSystemDarkMode

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
 * 将旧枚举显式映射到中立主题枚举，不依赖名称或序号。
 *
 * @receiver 旧主题请求的明暗模式；[DarkMode.System] 仍表示跟随系统，不在此解析系统状态。
 * @return 含义一致的中立主题模式。
 */
internal fun DarkMode.toDesignSystemDarkMode(): DesignSystemDarkMode = when (this) {
    DarkMode.Light -> DesignSystemDarkMode.Light
    DarkMode.Dark -> DesignSystemDarkMode.Dark
    DarkMode.System -> DesignSystemDarkMode.System
}

/**
 * 将中立主题请求映射回旧枚举，保留旧属性类型。
 *
 * @receiver 中立主题记录的请求模式，而非已解析的实际明暗状态。
 * @return 与请求含义一致的旧模式；跟随系统的请求仍返回 [DarkMode.System]。
 */
internal fun DesignSystemDarkMode.toLegacyDarkMode(): DarkMode = when (this) {
    DesignSystemDarkMode.Light -> DarkMode.Light
    DesignSystemDarkMode.Dark -> DarkMode.Dark
    DesignSystemDarkMode.System -> DarkMode.System
}
