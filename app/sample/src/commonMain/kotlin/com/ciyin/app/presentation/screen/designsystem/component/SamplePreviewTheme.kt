package com.ciyin.app.presentation.screen.designsystem.component

import androidx.compose.runtime.Composable
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme

/**
 * 预览专用主题包装：显式提供内置 Nomi 实现与浅色默认主题。
 *
 * 运行期页面由应用根显式提供设计系统；此处只服务 @AppPreview 的组合入口。
 *
 * @param content 在 Nomi 主题环境中组合的预览内容。
 */
@Composable
internal fun SamplePreviewTheme(content: @Composable () -> Unit) {
    NomiTheme(
        designSystem = NomiDesignSystem.Default,
        darkMode = DarkMode.Light,
        content = content,
    )
}
