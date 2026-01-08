package ciyin.ui.foundation.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ciyin.ui.foundation.extension.thenIf


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/10/26 17:37
 */

@Composable
fun MaterialPreview(
    maxSize: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable ColumnScope.() -> Unit
) = MaterialTheme {
    Column(
        modifier = Modifier
            .thenIf(maxSize) { fillMaxSize() }
            .background(backgroundColor),
        content = content
    )
}