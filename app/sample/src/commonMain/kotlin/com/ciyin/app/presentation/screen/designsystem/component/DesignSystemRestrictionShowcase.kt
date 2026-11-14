package com.ciyin.app.presentation.screen.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.component.NomiComponentCapabilities
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoModel
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_restriction_card_border
import com.ciyin.app.sample.design_system_demo_restriction_text_auto_size
import com.ciyin.app.sample.design_system_demo_restriction_card_colors
import com.ciyin.app.sample.design_system_demo_restriction_padding
import com.ciyin.app.sample.design_system_demo_restriction_shape
import com.ciyin.app.sample.design_system_demo_restriction_size
import com.ciyin.app.sample.design_system_demo_restrictions_summary
import com.ciyin.app.sample.design_system_demo_show_details
import com.ciyin.app.sample.design_system_demo_hide_details
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 显示当前设计系统不支持的显式覆盖能力，避免把 fail-fast 误认为页面故障。
 *
 * 没有限制时不产生分区；存在限制时默认收起，展开状态由当前组合的 rememberSaveable 保存。
 *
 * @param model 从其 capabilities 读取按钮尺寸、形状、内边距、卡片颜色、描边与文本自动字号支持状态。
 */
@Composable
internal fun DesignSystemRestrictionShowcase(model: DesignSystemDemoModel) {
    val capabilities = model.capabilities
    val messages = buildList {
        if (!capabilities.buttonSize) add(Res.string.design_system_demo_restriction_size)
        if (!capabilities.buttonShape) add(Res.string.design_system_demo_restriction_shape)
        if (!capabilities.buttonContentPadding) add(Res.string.design_system_demo_restriction_padding)
        if (!capabilities.cardColors) add(Res.string.design_system_demo_restriction_card_colors)
        if (!capabilities.cardBorder) add(Res.string.design_system_demo_restriction_card_border)
        if (!capabilities.textAutoSize) add(Res.string.design_system_demo_restriction_text_auto_size)
    }
    if (messages.isNotEmpty()) {
        var expanded by rememberSaveable { mutableStateOf(false) }
        DesignSystemSection {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium),
                verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
            ) {
                NomiText(
                    text = stringResource(
                        Res.string.design_system_demo_restrictions_summary,
                        messages.size
                    ),
                    style = NomiTheme.typography.titleSmall,
                )
                NomiButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.testTag("design-system-demo-restrictions-toggle"),
                    variant = NomiButtonVariant.Text,
                ) {
                    NomiText(stringResource(if (expanded) Res.string.design_system_demo_hide_details else Res.string.design_system_demo_show_details))
                }
            }
            if (expanded) {
                Column(
                    modifier = Modifier.testTag("design-system-demo-restrictions-details"),
                    verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
                ) {
                    messages.forEach { message ->
                        NomiText(
                            text = stringResource(message),
                            style = NomiTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * 限制提示纯内容预览。
 */
@AppPreview
@Composable
private fun DesignSystemRestrictionShowcasePreview() {
    SamplePreviewTheme {
        DesignSystemRestrictionShowcase(
            model = DesignSystemDemoModel(
                size = null,
                variant = null,
                enabled = true,
                override = false,
                clickCount = 0,
                capabilities = NomiComponentCapabilities(buttonSize = false, buttonShape = false),
                alternateDesignSystem = null,
            ),
        )
    }
}
