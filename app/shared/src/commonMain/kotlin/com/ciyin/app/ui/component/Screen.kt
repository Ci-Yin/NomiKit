package com.ciyin.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ciyin.ui.foundation.extension.thenIf
import ciyin.ui.foundation.widget.Title


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/19 下午8:16
 */

@Composable
fun Screen(
    title: String = "",
    maxWidth: Dp? = 1000.dp,
    isScroll: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(15.dp),
    modifier: Modifier = Modifier,
    toolbar: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) = Box(
    modifier = Modifier
        .systemBarsPadding()
        .fillMaxSize(),
    contentAlignment = Alignment.Center
) {

    Column(
        modifier = modifier
            .thenIf(maxWidth != null) {
                widthIn(max = maxWidth!!)
            }
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(45.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (title.isNotEmpty()) {
                Title(
                    modifier = Modifier.padding(start = 15.dp),
                    style = MaterialTheme.typography.titleLarge,
                    text = title
                )
            }
            toolbar()
        }
        Column(
            Modifier
                .padding(contentPadding)
                .thenIf(isScroll) { verticalScroll(rememberScrollState()) },
            content = content
        )

    }

}
