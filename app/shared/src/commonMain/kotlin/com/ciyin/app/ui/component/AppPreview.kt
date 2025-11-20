package com.ciyin.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ciyin.foundation.thenIf
import com.ciyin.app.ui.theme.AppTheme


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/10/26 17:37
 * @version: 1.0
 */

@Composable
fun AppPreview(
    maxSize: Boolean = true,
    darkTheme: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable ColumnScope.() -> Unit
) = AppTheme(darkTheme = darkTheme) {
    Column(
        modifier = Modifier
            .thenIf(maxSize) { fillMaxSize() }
            .background(backgroundColor),
        content = content
    )
}