package com.ciyin.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 应用的深色模式设置
 *
 * 用于控制应用的主题显示模式。
 */
enum class DarkMode {
    /** 强制浅色模式 */
    Light,

    /** 强制深色模式 */
    Dark,

    /** 跟随系统设置 */
    System;
}

/**
 * 保存当前 [DarkMode] 配置的 CompositionLocal
 *
 * 默认为 [DarkMode.System]。
 */
internal val LocalDarkMode = staticCompositionLocalOf { DarkMode.System }
