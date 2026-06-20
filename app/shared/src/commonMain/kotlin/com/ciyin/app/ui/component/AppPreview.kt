package com.ciyin.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ciyin.material.theme.AppTheme
import ciyin.material.theme.DarkMode
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
 * @param maxSize 是否填满可用预览尺寸
 * @param darkMode 预览使用的深色模式
 * @param backgroundColor 预览背景色
 * @param content 预览内容
 */
@Composable
fun AppPreview(
    maxSize: Boolean = true,
    darkMode: DarkMode = DarkMode.System,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable ColumnScope.() -> Unit
) = AppTheme(darkMode = darkMode) {
    Column(
        modifier = Modifier
            .thenIf(maxSize) { fillMaxSize() }
            .background(backgroundColor),
        content = content
    )
}
