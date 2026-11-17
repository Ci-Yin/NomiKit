package com.ciyin.app.presentation.screen.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoTags
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_long_chinese
import com.ciyin.app.sample.design_system_demo_multiline
import com.ciyin.app.sample.design_system_demo_role_body_large
import com.ciyin.app.sample.design_system_demo_role_body_medium
import com.ciyin.app.sample.design_system_demo_role_body_small
import com.ciyin.app.sample.design_system_demo_role_display_large
import com.ciyin.app.sample.design_system_demo_role_display_medium
import com.ciyin.app.sample.design_system_demo_role_display_small
import com.ciyin.app.sample.design_system_demo_role_headline_large
import com.ciyin.app.sample.design_system_demo_role_headline_medium
import com.ciyin.app.sample.design_system_demo_role_headline_small
import com.ciyin.app.sample.design_system_demo_role_label_large
import com.ciyin.app.sample.design_system_demo_role_label_medium
import com.ciyin.app.sample.design_system_demo_role_label_small
import com.ciyin.app.sample.design_system_demo_role_title_large
import com.ciyin.app.sample.design_system_demo_role_title_medium
import com.ciyin.app.sample.design_system_demo_role_title_small
import com.ciyin.app.sample.design_system_demo_text_field_overrides
import com.ciyin.app.sample.design_system_demo_text_rich_inline
import com.ciyin.app.sample.design_system_demo_text_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 默认选中的 BodyMedium 在十五种排版角色中的零基索引。
 */
private const val DefaultTextRoleIndex = 10

/**
 * 展示排版角色选择与单个真实字号预览，并保留长中文和显式多行示例。
 *
 * 当前角色索引由 rememberSaveable 保存，默认选中 BodyMedium；角色样式随当前主题重新读取。
 */
@Composable
internal fun DesignSystemTextShowcase() {
    val roles = listOf(
        Res.string.design_system_demo_role_display_large to nativeTextStyle(ShowcaseTextRole.DisplayLarge),
        Res.string.design_system_demo_role_display_medium to nativeTextStyle(ShowcaseTextRole.DisplayMedium),
        Res.string.design_system_demo_role_display_small to nativeTextStyle(ShowcaseTextRole.DisplaySmall),
        Res.string.design_system_demo_role_headline_large to nativeTextStyle(ShowcaseTextRole.HeadlineLarge),
        Res.string.design_system_demo_role_headline_medium to nativeTextStyle(ShowcaseTextRole.HeadlineMedium),
        Res.string.design_system_demo_role_headline_small to nativeTextStyle(ShowcaseTextRole.HeadlineSmall),
        Res.string.design_system_demo_role_title_large to nativeTextStyle(ShowcaseTextRole.TitleLarge),
        Res.string.design_system_demo_role_title_medium to nativeTextStyle(ShowcaseTextRole.TitleMedium),
        Res.string.design_system_demo_role_title_small to nativeTextStyle(ShowcaseTextRole.TitleSmall),
        Res.string.design_system_demo_role_body_large to nativeTextStyle(ShowcaseTextRole.BodyLarge),
        Res.string.design_system_demo_role_body_medium to nativeTextStyle(ShowcaseTextRole.BodyMedium),
        Res.string.design_system_demo_role_body_small to nativeTextStyle(ShowcaseTextRole.BodySmall),
        Res.string.design_system_demo_role_label_large to nativeTextStyle(ShowcaseTextRole.LabelLarge),
        Res.string.design_system_demo_role_label_medium to nativeTextStyle(ShowcaseTextRole.LabelMedium),
        Res.string.design_system_demo_role_label_small to nativeTextStyle(ShowcaseTextRole.LabelSmall),
    )
    var selectedIndex by rememberSaveable { mutableIntStateOf(DefaultTextRoleIndex) }
    val (selectedLabel, selectedStyle) = roles[selectedIndex]

    Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium)) {
        NomiText(
            text = stringResource(Res.string.design_system_demo_text_title),
            style = nativeTextStyle(ShowcaseTextRole.TitleLarge),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
            verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
        ) {
            roles.forEachIndexed { index, (label, _) ->
                NomiButton(
                    onClick = { selectedIndex = index },
                    modifier = Modifier.testTag("text-role-choice-$index")
                        .semantics { selected = index == selectedIndex },
                    variant = if (index == selectedIndex) {
                        NomiButtonVariant.Primary
                    } else {
                        NomiButtonVariant.Text
                    },
                ) {
                    NomiText(text = stringResource(label).substringBefore(" ·"))
                }
            }
        }
        NomiText(
            text = stringResource(selectedLabel),
            modifier = Modifier.fillMaxWidth().testTag("text-role-preview"),
            style = selectedStyle,
        )
        NomiText(
            text = stringResource(Res.string.design_system_demo_long_chinese),
            modifier = Modifier.fillMaxWidth().testTag(DesignSystemDemoTags.LongText),
        )
        NomiText(
            text = stringResource(Res.string.design_system_demo_multiline),
            modifier = Modifier.fillMaxWidth().testTag(DesignSystemDemoTags.MultilineText),
        )
        Column(
            modifier = Modifier.testTag("text-field-overrides"),
            verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
        ) {
            NomiText(
                text = stringResource(Res.string.design_system_demo_text_field_overrides),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            NomiText(
                text = stringResource(Res.string.design_system_demo_text_field_overrides),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
            )
        }
        NomiText(
            text = buildAnnotatedString {
                append(stringResource(Res.string.design_system_demo_text_rich_inline))
                appendInlineContent("dot")
            },
            modifier = Modifier.fillMaxWidth().testTag("text-rich-inline"),
            inlineContent = mapOf(
                "dot" to InlineTextContent(
                    Placeholder(
                        width = 14.sp,
                        height = 14.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                    ),
                ) {
                    Box(Modifier.fillMaxSize().background(NomiTheme.colorScheme.primary))
                },
            ),
        )
    }
}

/**
 * 文本角色选择与单个预览的纯内容预览。
 */
@AppPreview
@Composable
private fun DesignSystemTextShowcasePreview() {
    SamplePreviewTheme { DesignSystemTextShowcase() }
}
