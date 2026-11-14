package com.ciyin.app.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.fluent.navigation.LocalFluentSystemBackdropActive

/**
 * 根据主窗口系统材质状态返回页面背景色。
 *
 * 材质生效时页面背景透明，让窗口材质透出；否则保持主题原色。
 *
 * @param fallback 系统材质不可用时使用的主题背景色
 * @return 系统材质生效时透明、否则保持原主题色
 */
@Composable
@ReadOnlyComposable
fun windowBackgroundColor(fallback: Color): Color =
    if (LocalFluentSystemBackdropActive.current) {
        fallback.copy(alpha = 0f)
    } else {
        fallback
    }
