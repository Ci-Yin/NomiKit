package com.ciyin.app.presentation.component

import androidx.compose.foundation.ScrollState
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
import ciyin.designsystem.component.scaffold.NomiScaffold
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import ciyin.ui.foundation.extension.thenIf
import com.ciyin.app.shared.Res
import com.ciyin.app.shared.app_name
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 应用页面通用脚手架。
 *
 * 只保留应用层策略：桌面最大宽度、系统栏避让、页面滚动、窗口材质透明与标题文案；
 * 页面外壳（顶栏、主体、底栏的排布与背景）交给当前设计系统的 [NomiScaffold]。
 *
 * @param modifier 内容容器修饰符
 * @param title 页面标题
 * @param maxWidth 页面最大宽度，传入 null 时不限制
 * @param scrollState 页面内容滚动状态，传入 null 时不启用垂直滚动
 * @param contentPadding 页面内容内边距
 * @param containerColor 页面背景色
 * @param topBar 顶部栏附加内容，追加在标题之后
 * @param bottomBar 底部栏内容
 * @param content 页面主体内容
 */
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    title: String = "",
    maxWidth: Dp? = 1000.dp,
    scrollState: ScrollState? = null,
    contentPadding: PaddingValues = PaddingValues(NomiTheme.spacings.large),
    containerColor: Color = windowBackgroundColor(NomiTheme.colorScheme.background),
    topBar: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) = Box(
    modifier = Modifier
        .systemBarsPadding()
        .fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    NomiScaffold(
        modifier = modifier
            .thenIf(maxWidth != null) {
                widthIn(max = maxWidth!!)
            }
            .fillMaxSize(),
        colors = NomiScaffoldColors(containerColor = containerColor),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = NomiTheme.spacings.small,
                        horizontal = NomiTheme.spacings.large
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (title.isNotEmpty()) {
                    NomiText(
                        text = title,
                        style = NomiTheme.typography.titleLarge,
                    )
                }
                topBar()
            }
        },
        bottomBar = bottomBar,
        content = {
            Column(
                Modifier
                    .padding(contentPadding)
                    .thenIf(scrollState != null) { verticalScroll(scrollState!!) },
                content = content
            )
        },
    )
}

/**
 * 页面脚手架预览。
 */
@Preview
@Composable
private fun ScreenPreview() = AppPreview {
    ScreenScaffold(
        title = stringResource(Res.string.app_name),
        topBar = {}
    ) {

    }
}
