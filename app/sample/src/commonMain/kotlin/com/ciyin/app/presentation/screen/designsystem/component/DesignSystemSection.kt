package com.ciyin.app.presentation.screen.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 展示页的分区容器，只组织间距和边界，不给内部被测组件注入视觉覆盖。
 *
 * @param content 在卡片内 ColumnScope 中组合的分区内容；容器提供主题间距，不额外指定文本颜色或样式。
 */
@Composable
internal fun DesignSystemSection(content: @Composable ColumnScope.() -> Unit) {
    NomiCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(NomiTheme.spacings.large),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium),
            content = content,
        )
    }
}

/**
 * 分区容器的纯内容预览。
 */
@AppPreview
@Composable
private fun DesignSystemSectionPreview() {
    SamplePreviewTheme { DesignSystemSection { NomiText("Section") } }
}
