package com.ciyin.app.presentation.screen.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.control.NomiCheckbox
import ciyin.designsystem.component.control.NomiComboBox
import ciyin.designsystem.component.control.NomiRadioButton
import ciyin.designsystem.component.control.NomiSlider
import ciyin.designsystem.component.control.NomiSwitch
import ciyin.designsystem.component.feedback.NomiProgressIndicator
import ciyin.designsystem.component.feedback.NomiProgressIndicatorType
import ciyin.designsystem.component.menu.NomiMenuItem
import ciyin.designsystem.component.text.NomiTextField
import ciyin.designsystem.component.divider.NomiDivider
import ciyin.designsystem.component.divider.NomiDividerOrientation
import ciyin.designsystem.component.feedback.NomiTooltip
import ciyin.designsystem.component.icon.NomiIcon
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.icon.ArrowRight
import ciyin.designsystem.icon.Home
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoTags
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_combo_box_label
import com.ciyin.app.sample.design_system_demo_combo_box_option_first
import com.ciyin.app.sample.design_system_demo_combo_box_option_second
import com.ciyin.app.sample.design_system_demo_combo_box_placeholder
import com.ciyin.app.sample.design_system_demo_controls_checkbox_label
import com.ciyin.app.sample.design_system_demo_controls_icon_label
import com.ciyin.app.sample.design_system_demo_controls_progress_label
import com.ciyin.app.sample.design_system_demo_controls_radio_label
import com.ciyin.app.sample.design_system_demo_controls_radio_other_label
import com.ciyin.app.sample.design_system_demo_controls_slider_label
import com.ciyin.app.sample.design_system_demo_controls_switch_label
import com.ciyin.app.sample.design_system_demo_controls_text_field_error_label
import com.ciyin.app.sample.design_system_demo_controls_text_field_label
import com.ciyin.app.sample.design_system_demo_controls_text_field_placeholder
import com.ciyin.app.sample.design_system_demo_controls_title
import com.ciyin.app.sample.design_system_demo_controls_tooltip_anchor
import com.ciyin.app.sample.design_system_demo_controls_tooltip_hint
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 图标、分割线与控件展示分区：每个示例都不传视觉参数，保留当前设计系统的原生默认。
 *
 * 图标只约束尺寸；分割线同时演示横向与纵向；开关、复选框与单选按钮的状态由本分区自己持有；
 * 输入框演示标签、占位符、尾部图标与错误态；提示浮层把触发内容包在原生浮层里。
 *
 * @param modifier 应用于展示分区根 Column 的布局与语义修饰符。
 */
@Composable
internal fun DesignSystemControlsShowcase(modifier: Modifier = Modifier) {
    var enabled by remember { mutableStateOf(true) }
    var accepted by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var comboExpanded by remember { mutableStateOf(false) }
    var comboSelected by remember { mutableStateOf<Int?>(null) }
    var sliderValue by remember { mutableStateOf(0.4f) }
    val comboOptions = listOf(
        stringResource(Res.string.design_system_demo_combo_box_option_first),
        stringResource(Res.string.design_system_demo_combo_box_option_second),
    )

    Column(
        modifier = modifier.fillMaxWidth().testTag(DesignSystemDemoTags.ControlsSection),
        verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium),
    ) {
        NomiText(
            text = stringResource(Res.string.design_system_demo_controls_title),
            style = nativeTextStyle(ShowcaseTextRole.TitleLarge),
        )
        NomiText(
            text = stringResource(Res.string.design_system_demo_controls_icon_label),
            style = nativeTextStyle(ShowcaseTextRole.LabelMedium),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium),
        ) {
            NomiIcon(
                imageVector = IconPack.Home,
                contentDescription = null,
                modifier = Modifier
                    .size(NomiTheme.sizes.icon.medium)
                    .testTag(DesignSystemDemoTags.IconSample),
            )
            NomiIcon(
                imageVector = IconPack.ArrowRight,
                contentDescription = null,
                modifier = Modifier.size(NomiTheme.sizes.icon.large),
            )
            NomiDivider(
                orientation = NomiDividerOrientation.Vertical,
                modifier = Modifier
                    .height(NomiTheme.sizes.icon.large)
                    .testTag(DesignSystemDemoTags.DividerSample),
            )
        }
        NomiDivider(modifier = Modifier.fillMaxWidth())
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.large),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
                NomiText(
                    text = stringResource(Res.string.design_system_demo_controls_switch_label),
                    style = nativeTextStyle(ShowcaseTextRole.LabelMedium),
                )
                NomiSwitch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    modifier = Modifier.testTag(DesignSystemDemoTags.Switch),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
                NomiText(
                    text = stringResource(Res.string.design_system_demo_controls_checkbox_label),
                    style = nativeTextStyle(ShowcaseTextRole.LabelMedium),
                )
                NomiCheckbox(
                    checked = accepted,
                    onCheckedChange = { accepted = it },
                    modifier = Modifier.testTag(DesignSystemDemoTags.Checkbox),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
                NomiText(
                    text = stringResource(Res.string.design_system_demo_controls_radio_label),
                    style = nativeTextStyle(ShowcaseTextRole.LabelMedium),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
                    NomiRadioButton(
                        selected = selectedIndex == 0,
                        onClick = { selectedIndex = 0 },
                        modifier = Modifier.testTag(DesignSystemDemoTags.Radio),
                    )
                    NomiRadioButton(
                        selected = selectedIndex == 1,
                        onClick = { selectedIndex = 1 },
                    )
                    NomiText(
                        text = stringResource(Res.string.design_system_demo_controls_radio_other_label),
                        style = nativeTextStyle(ShowcaseTextRole.BodySmall),
                    )
                }
            }
        }
        NomiTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().testTag(DesignSystemDemoTags.TextField),
            singleLine = true,
            label = { NomiText(stringResource(Res.string.design_system_demo_controls_text_field_label)) },
            placeholder = {
                NomiText(stringResource(Res.string.design_system_demo_controls_text_field_placeholder))
            },
            trailingIcon = {
                NomiIcon(
                    imageVector = IconPack.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(NomiTheme.sizes.icon.small),
                )
            },
        )
        NomiTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().testTag(DesignSystemDemoTags.TextFieldError),
            label = {
                NomiText(stringResource(Res.string.design_system_demo_controls_text_field_error_label))
            },
            isError = true,
        )
        Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
            NomiText(
                text = stringResource(Res.string.design_system_demo_combo_box_label),
                style = nativeTextStyle(ShowcaseTextRole.LabelMedium),
            )
            NomiComboBox(
                expanded = comboExpanded,
                onExpandedChange = { comboExpanded = it },
                menuItems = {
                    comboOptions.forEachIndexed { index, option ->
                        NomiMenuItem(
                            onClick = {
                                comboSelected = index
                                comboExpanded = false
                            },
                            selected = comboSelected == index,
                            modifier = if (index == 0) {
                                Modifier.testTag(DesignSystemDemoTags.ComboBoxOption)
                            } else {
                                Modifier
                            },
                        ) { NomiText(option) }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag(DesignSystemDemoTags.ComboBox),
            ) {
                NomiText(
                    text = comboSelected?.let { comboOptions[it] }
                        ?: stringResource(Res.string.design_system_demo_combo_box_placeholder),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
            NomiText(
                text = stringResource(Res.string.design_system_demo_controls_slider_label),
                style = nativeTextStyle(ShowcaseTextRole.LabelMedium),
            )
            NomiSlider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                modifier = Modifier.fillMaxWidth().testTag(DesignSystemDemoTags.Slider),
                steps = 4,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
            NomiText(
                text = stringResource(Res.string.design_system_demo_controls_progress_label),
                style = nativeTextStyle(ShowcaseTextRole.LabelMedium),
            )
            NomiProgressIndicator(
                progress = 0.6f,
                modifier = Modifier.fillMaxWidth().testTag(DesignSystemDemoTags.ProgressLinear),
            )
            NomiProgressIndicator(
                progress = null,
                modifier = Modifier.testTag(DesignSystemDemoTags.ProgressCircular),
                type = NomiProgressIndicatorType.Circular,
            )
        }
        NomiTooltip(
            tooltip = {
                NomiText(stringResource(Res.string.design_system_demo_controls_tooltip_hint))
            },
        ) {
            NomiText(
                text = stringResource(Res.string.design_system_demo_controls_tooltip_anchor),
                modifier = Modifier
                    .padding(vertical = NomiTheme.spacings.small)
                    .testTag(DesignSystemDemoTags.Tooltip),
            )
        }
    }
}

/**
 * 控件分区纯内容预览。
 */
@AppPreview
@Composable
private fun DesignSystemControlsShowcasePreview() {
    SamplePreviewTheme {
        DesignSystemControlsShowcase(modifier = Modifier.padding(NomiTheme.spacings.large))
    }
}
