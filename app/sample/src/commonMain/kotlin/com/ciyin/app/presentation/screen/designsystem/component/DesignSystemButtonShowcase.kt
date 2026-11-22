package com.ciyin.app.presentation.screen.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.button.NomiDropDownButton
import ciyin.designsystem.component.button.NomiToggleButton
import ciyin.designsystem.component.divider.NomiDivider
import ciyin.designsystem.component.icon.NomiIcon
import ciyin.designsystem.component.menu.NomiMenu
import ciyin.designsystem.component.menu.NomiMenuItem
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.icon.Settings
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoModel
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoTags
import com.ciyin.app.presentation.screen.designsystem.designSystemSizeLabel
import com.ciyin.app.presentation.screen.designsystem.designSystemVariantLabel
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_button_label
import com.ciyin.app.sample.design_system_demo_button_long
import com.ciyin.app.sample.design_system_demo_click
import com.ciyin.app.sample.design_system_demo_counter
import com.ciyin.app.sample.design_system_demo_disabled_button
import com.ciyin.app.sample.design_system_demo_drop_down_button
import com.ciyin.app.sample.design_system_demo_explicit_overrides_title
import com.ciyin.app.sample.design_system_demo_menu_item_first
import com.ciyin.app.sample.design_system_demo_menu_item_second
import com.ciyin.app.sample.design_system_demo_toggle_button
import com.ciyin.app.sample.design_system_demo_toggle_icon_button
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 显式按钮覆盖矩阵与按钮族新增入口；不支持尺寸覆盖的实现由适配器降级为原生默认。
 *
 * 开关按钮、图标开关与下拉按钮的状态由本分区自己持有；下拉菜单与触发按钮放进同一个父布局，
 * 浮层位置由当前设计系统按父节点计算。
 *
 * @param model 提供当前尺寸与变体、启用状态和计数。
 * @param onButtonClick 任一可用展示按钮被点击时调用；计数由调用方维护，始终禁用的按钮不会触发。
 */
@Composable
internal fun DesignSystemButtonShowcase(model: DesignSystemDemoModel, onButtonClick: () -> Unit) {
    var toggled by remember { mutableStateOf(false) }
    var iconToggled by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var selectedMenuItem by remember { mutableStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium)) {
        NomiText(
            text = stringResource(Res.string.design_system_demo_explicit_overrides_title),
            style = NomiTheme.typography.titleLarge
        )
        NomiControlSize.entries.forEach { size ->
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
                verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
            ) {
                NomiButtonVariant.entries.forEach { variant ->
                    NomiButton(
                        onClick = onButtonClick,
                        modifier = Modifier.testTag(
                            "design-system-demo-explicit-button-" + variant + "-" + size
                        ),
                        enabled = model.enabled,
                        variant = variant,
                        size = size,
                    ) {
                        val label = stringResource(designSystemVariantLabel(variant))
                        NomiText(
                            text = stringResource(
                                Res.string.design_system_demo_button_label,
                                label,
                                stringResource(designSystemSizeLabel(size)),
                            ),
                        )
                    }
                }
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium),
            verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
        ) {
            NomiButton(
                onClick = onButtonClick,
                modifier = Modifier.testTag(DesignSystemDemoTags.CounterButton),
                enabled = model.enabled,
                variant = model.variant,
                size = model.size,
            ) { NomiText(stringResource(Res.string.design_system_demo_click)) }
            NomiButton(
                onClick = onButtonClick,
                modifier = Modifier.testTag(DesignSystemDemoTags.DisabledButton),
                enabled = false,
                variant = model.variant,
                size = model.size,
            ) { NomiText(stringResource(Res.string.design_system_demo_disabled_button)) }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium),
            verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
        ) {
            NomiToggleButton(
                checked = toggled,
                onCheckedChange = { toggled = it },
                modifier = Modifier.testTag(DesignSystemDemoTags.ToggleButton),
                enabled = model.enabled,
            ) { NomiText(stringResource(Res.string.design_system_demo_toggle_button)) }
            NomiToggleButton(
                checked = iconToggled,
                onCheckedChange = { iconToggled = it },
                modifier = Modifier.testTag(DesignSystemDemoTags.ToggleIconButton),
                enabled = model.enabled,
                iconOnly = true,
            ) {
                NomiIcon(
                    imageVector = IconPack.Settings,
                    contentDescription = stringResource(Res.string.design_system_demo_toggle_icon_button),
                    modifier = Modifier.size(NomiTheme.sizes.icon.medium),
                )
            }
            Box {
                NomiDropDownButton(
                    onClick = { menuExpanded = !menuExpanded },
                    modifier = Modifier.testTag(DesignSystemDemoTags.DropDownButton),
                    enabled = model.enabled,
                ) { NomiText(stringResource(Res.string.design_system_demo_drop_down_button)) }
                NomiMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    NomiMenuItem(
                        onClick = {
                            selectedMenuItem = 1
                            menuExpanded = false
                        },
                        modifier = Modifier.testTag(DesignSystemDemoTags.MenuItem),
                        selected = selectedMenuItem == 1,
                    ) { NomiText(stringResource(Res.string.design_system_demo_menu_item_first)) }
                    NomiDivider(modifier = Modifier.fillMaxWidth())
                    NomiMenuItem(
                        onClick = {
                            selectedMenuItem = 2
                            menuExpanded = false
                        },
                        selected = selectedMenuItem == 2,
                    ) { NomiText(stringResource(Res.string.design_system_demo_menu_item_second)) }
                }
            }
        }
        NomiText(
            text = stringResource(Res.string.design_system_demo_counter, model.clickCount),
            modifier = Modifier.testTag(DesignSystemDemoTags.Counter),
            style = NomiTheme.typography.bodySmall,
        )
        NomiButton(
            onClick = onButtonClick,
            modifier = Modifier.fillMaxWidth().testTag(DesignSystemDemoTags.LongButton),
            enabled = model.enabled,
            variant = model.variant,
            size = model.size,
        ) { NomiText(stringResource(Res.string.design_system_demo_button_long)) }
    }
}

/**
 * 显式按钮矩阵纯预览。
 */
@AppPreview
@Composable
private fun DesignSystemButtonShowcasePreview() {
    SamplePreviewTheme {
        DesignSystemButtonShowcase(
            model = DesignSystemDemoModel(
                size = NomiControlSize.Medium,
                variant = NomiButtonVariant.Primary,
                enabled = true,
                override = false,
                clickCount = 0,
                alternateDesignSystem = null,
            ),
            onButtonClick = {},
        )
    }
}
