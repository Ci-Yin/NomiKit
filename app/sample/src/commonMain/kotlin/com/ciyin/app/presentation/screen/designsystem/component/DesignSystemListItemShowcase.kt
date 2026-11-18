package com.ciyin.app.presentation.screen.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.fluent.list.FluentListItemSpec
import ciyin.designsystem.icon.ArrowRight
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.icon.Settings
import ciyin.designsystem.material3.list.Material3ListItemSpec
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoTags
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_list_item_action
import com.ciyin.app.sample.design_system_demo_list_item_static_subtitle
import com.ciyin.app.sample.design_system_demo_list_item_static_title
import com.ciyin.app.sample.design_system_demo_list_item_subtitle
import com.ciyin.app.sample.design_system_demo_list_item_title
import com.ciyin.app.sample.design_system_demo_list_item_value
import com.ciyin.app.sample.design_system_demo_list_items_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 列表项展示分区：可点击行与静态行，槽位内容由调用方组合。
 *
 * 图标使用设计系统中立 IconPack 并显式约束尺寸与颜色，不把外层 Material 3 内容色带进
 * 其它设计系统的演示行。行本身不传任何颜色、形状或内边距参数。
 *
 * @param modifier 应用于展示分区根 Column 的布局与语义修饰符。
 */
@Composable
internal fun DesignSystemListItemShowcase(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().testTag(DesignSystemDemoTags.ListItems),
        verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium),
    ) {
        NomiText(
            text = stringResource(Res.string.design_system_demo_list_items_title),
            style = nativeTextStyle(ShowcaseTextRole.TitleLarge),
        )
        NomiListItem(
            title = { NomiText(stringResource(Res.string.design_system_demo_list_item_title)) },
            modifier = Modifier.testTag(DesignSystemDemoTags.ListItemClickable),
            onClick = {},
            leading = {
                Icon(
                    imageVector = IconPack.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(NomiTheme.sizes.icon.large),
                    tint = nativeSecondaryTextColor(),
                )
            },
            subtitle = { NomiText(stringResource(Res.string.design_system_demo_list_item_subtitle)) },
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
                    NomiText(
                        text = stringResource(Res.string.design_system_demo_list_item_value),
                        modifier = Modifier.testTag(DesignSystemDemoTags.ListItemValue),
                    )
                    Icon(
                        imageVector = IconPack.ArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(NomiTheme.sizes.icon.small),
                        tint = nativeSecondaryTextColor(),
                    )
                }
            },
        )
        NomiListItem(
            title = { NomiText(stringResource(Res.string.design_system_demo_list_item_static_title)) },
            modifier = Modifier.testTag(DesignSystemDemoTags.ListItemStatic),
            subtitle = {
                NomiText(stringResource(Res.string.design_system_demo_list_item_static_subtitle))
            },
            trailing = {
                NomiButton(
                    onClick = {},
                    modifier = Modifier.testTag(DesignSystemDemoTags.ListItemAction),
                ) { NomiText(stringResource(Res.string.design_system_demo_list_item_action)) }
            },
            nativeSpec = Material3ListItemSpec() + FluentListItemSpec()
        )
    }
}

/**
 * 列表项分区纯内容预览。
 */
@AppPreview
@Composable
private fun DesignSystemListItemShowcasePreview() {
    SamplePreviewTheme {
        DesignSystemListItemShowcase(modifier = Modifier.padding(NomiTheme.spacings.large))
    }
}