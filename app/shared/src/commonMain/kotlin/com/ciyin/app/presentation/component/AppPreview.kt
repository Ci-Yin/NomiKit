package com.ciyin.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import ciyin.ui.foundation.extension.thenIf


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/10/26 17:37
 */

/**
 * 应用通用预览容器。
 *
 * 显式建立 Material 3 设计系统；背景色未指定时读取当前主题的中立背景色。
 *
 * @param maxSize 是否填满可用预览尺寸
 * @param darkMode 预览使用的深色模式，取中立 DarkMode
 * @param backgroundColor 预览背景色；Unspecified 表示使用当前主题背景色
 * @param content 预览内容
 */
@Composable
fun AppPreview(
    maxSize: Boolean = true,
    darkMode: DarkMode = DarkMode.System,
    backgroundColor: Color = Color.Unspecified,
    content: @Composable ColumnScope.() -> Unit
) = NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = darkMode) {
    Column(
        modifier = Modifier
            .thenIf(maxSize) { fillMaxSize() }
            .background(backgroundColor.takeOrElse { NomiTheme.colorScheme.background }),
        content = content
    )
}
