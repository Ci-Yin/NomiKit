package com.ciyin.app.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ciyin.material.theme.AppTheme
import ciyin.ui.foundation.extension.thenIf
import ciyin.ui.foundation.widget.Title
import com.ciyin.app.shared.Res
import com.ciyin.app.shared.app_name
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/19 下午8:16
 */

/**
 * 应用页面通用脚手架。
 *
 * @param modifier 内容容器修饰符
 * @param title 页面标题
 * @param maxWidth 页面最大宽度，传入 null 时不限制
 * @param scrollState 页面内容滚动状态，传入 null 时不启用垂直滚动
 * @param contentPadding 页面内容内边距
 * @param containerColor 页面背景色
 * @param topBar 顶部栏附加内容
 * @param bottomBar 底部栏内容
 * @param content 页面主体内容
 */
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    title: String = "",
    maxWidth: Dp? = 1000.dp,
    scrollState: ScrollState? = null,
    contentPadding: PaddingValues = PaddingValues(AppTheme.spacings.large),
    containerColor: Color = AppTheme.colorScheme.background,
    topBar: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) = Box(
    modifier = Modifier
        .systemBarsPadding()
        .fillMaxSize()
        .background(containerColor),
    contentAlignment = Alignment.Center
) {
    Column(
        modifier = modifier
            .thenIf(maxWidth != null) {
                widthIn(max = maxWidth!!)
            }
            .fillMaxSize()
            .background(AppTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = AppTheme.spacings.small,
                    horizontal = AppTheme.spacings.large
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (title.isNotEmpty()) {
                Title(
                    style = AppTheme.typography.titleLarge,
                    text = title
                )
            }
            topBar()
        }
        Column(
            Modifier
                .padding(contentPadding)
                .thenIf(scrollState != null) { verticalScroll(scrollState!!) },
            content = content
        )
        bottomBar()
    }

}

/**
 * 页面脚手架预览。
 */
@Preview
@Composable
private fun ScreenPreview() {
    ScreenScaffold(
        title = stringResource(Res.string.app_name),
        topBar = {}
    ) {

    }
}
