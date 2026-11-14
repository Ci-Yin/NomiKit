package com.ciyin.app.presentation.screen.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoTags
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_card_body
import com.ciyin.app.sample.design_system_demo_card_button
import com.ciyin.app.sample.design_system_demo_card_title
import com.ciyin.app.sample.design_system_demo_cards_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 展示非点击原生 Card；唯一交互是明确的内部按钮。
 *
 * @param enabled 卡片内部按钮是否可用，不为卡片容器增加点击语义。
 * @param onButtonClick 内部按钮可用且被点击时调用，由调用方处理展示计数。
 */
@Composable
internal fun DesignSystemCardShowcase(
    enabled: Boolean,
    onButtonClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
        NomiText(
            text = stringResource(Res.string.design_system_demo_cards_title),
            style = NomiTheme.typography.titleLarge
        )
        NomiCard(modifier = Modifier.fillMaxWidth().testTag(DesignSystemDemoTags.Card)) {
            Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium)) {
                NomiText(
                    text = stringResource(Res.string.design_system_demo_card_title),
                    style = NomiTheme.typography.titleMedium
                )
                NomiText(stringResource(Res.string.design_system_demo_card_body))
                NomiButton(
                    onClick = onButtonClick,
                    modifier = Modifier.testTag(DesignSystemDemoTags.CardButton),
                    enabled = enabled,
                ) { NomiText(stringResource(Res.string.design_system_demo_card_button)) }
            }
        }
    }
}

/**
 * 卡片展示纯内容预览。
 */
@AppPreview
@Composable
private fun DesignSystemCardShowcasePreview() {
    SamplePreviewTheme { DesignSystemCardShowcase(enabled = true, onButtonClick = {}) }
}
