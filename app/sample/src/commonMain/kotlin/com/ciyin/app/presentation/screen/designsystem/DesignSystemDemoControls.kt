package com.ciyin.app.presentation.screen.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.DarkMode
import com.ciyin.app.presentation.screen.designsystem.component.SamplePreviewTheme
import com.ciyin.app.presentation.screen.sample.DefaultSampleDesignSystems
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_controls
import com.ciyin.app.sample.design_system_demo_system
import com.ciyin.app.sample.design_system_demo_dark_mode
import com.ciyin.app.sample.design_system_demo_font_scale
import com.ciyin.app.sample.design_system_demo_size
import com.ciyin.app.sample.design_system_demo_variant
import com.ciyin.app.sample.design_system_demo_enabled
import com.ciyin.app.sample.design_system_demo_disabled
import com.ciyin.app.sample.design_system_demo_override_on
import com.ciyin.app.sample.design_system_demo_override_off
import com.ciyin.app.sample.design_system_demo_reset
import com.ciyin.app.sample.design_system_demo_nomi
import com.ciyin.app.sample.design_system_demo_material3
import com.ciyin.app.sample.design_system_demo_native_default
import com.ciyin.app.sample.design_system_demo_fluent
import com.ciyin.app.sample.design_system_demo_host_system
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 页面级设置面板，直接把选项转换为 Action；所有控制仍使用中立基础组件。
 *
 * @param state 当前配置快照；仅用于读取选中态，面板不会直接修改它。
 * @param systems 宿主候选目录，须恰好包含一个与 state.designSystemId 匹配的实现。
 * @param onAction 用户选择、切换或重置时接收意图，由调用方更新状态；控制面板不随展示按钮禁用。
 * @param modifier 应用于面板卡片的布局与语义修饰符。
 * @throws NoSuchElementException 候选目录中没有与状态标识匹配的实现时抛出。
 * @throws IllegalArgumentException 候选目录中有多个与状态标识匹配的实现时抛出。
 */
@Composable
internal fun DesignSystemDemoControls(
    state: DesignSystemDemoUiState,
    systems: List<NomiDesignSystem>,
    onAction: (DesignSystemDemoAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    NomiCard(
        modifier = modifier.fillMaxWidth().testTag(DesignSystemDemoTags.Controls),
        contentPadding = PaddingValues(NomiTheme.spacings.large),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium),
        ) {
            NomiText(
                text = stringResource(Res.string.design_system_demo_controls),
                style = NomiTheme.typography.titleLarge,
            )
            DemoControlGroup(title = stringResource(Res.string.design_system_demo_system)) {
                systems.forEach { system ->
                    DemoChoice(
                        label = designSystemName(system.id),
                        selected = state.designSystemId == system.id,
                        tag = "design-system-demo-system-${system.id.value}",
                        onClick = { onAction(DesignSystemDemoAction.SelectDesignSystem(system.id)) },
                    )
                }
            }
            DemoControlGroup(title = stringResource(Res.string.design_system_demo_dark_mode)) {
                listOf(DarkMode.System, DarkMode.Light, DarkMode.Dark).forEach { mode ->
                    DemoChoice(
                        label = stringResource(designSystemDarkModeLabel(mode)),
                        selected = state.darkMode == mode,
                        tag = "design-system-demo-mode-$mode",
                        onClick = { onAction(DesignSystemDemoAction.SelectDarkMode(mode)) },
                    )
                }
            }
            DemoControlGroup(title = stringResource(Res.string.design_system_demo_font_scale)) {
                DesignSystemDemoFontScales.forEach { scale ->
                    DemoChoice(
                        label = stringResource(designSystemFontScaleLabel(scale)),
                        selected = state.fontScale == scale,
                        tag = "design-system-demo-font-$scale",
                        onClick = { onAction(DesignSystemDemoAction.SelectFontScale(scale)) },
                    )
                }
            }
            DemoControlGroup(title = stringResource(Res.string.design_system_demo_size)) {
                DemoChoice(
                    label = stringResource(Res.string.design_system_demo_native_default),
                    selected = state.size == null,
                    tag = "design-system-demo-size-default",
                    onClick = { onAction(DesignSystemDemoAction.SelectSize(null)) },
                )
                NomiControlSize.entries.forEach { size ->
                    DemoChoice(
                        label = stringResource(designSystemSizeLabel(size)),
                        selected = state.size == size,
                        tag = "design-system-demo-size-$size",
                        onClick = { onAction(DesignSystemDemoAction.SelectSize(size)) },
                    )
                }
            }
            DemoControlGroup(title = stringResource(Res.string.design_system_demo_variant)) {
                DemoChoice(
                    label = stringResource(Res.string.design_system_demo_native_default),
                    selected = state.variant == null,
                    tag = "design-system-demo-variant-default",
                    onClick = { onAction(DesignSystemDemoAction.SelectVariant(null)) },
                )
                NomiButtonVariant.entries.forEach { variant ->
                    DemoChoice(
                        label = stringResource(designSystemVariantLabel(variant)),
                        selected = state.variant == variant,
                        tag = "design-system-demo-variant-$variant",
                        onClick = { onAction(DesignSystemDemoAction.SelectVariant(variant)) },
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
                verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
            ) {
                NomiButton(
                    onClick = { onAction(DesignSystemDemoAction.ToggleEnabled) },
                    modifier = Modifier.testTag(DesignSystemDemoTags.Enabled),
                ) {
                    val label = if (state.enabled) {
                        Res.string.design_system_demo_enabled
                    } else Res.string.design_system_demo_disabled
                    NomiText(stringResource(label))
                }
                NomiButton(
                    onClick = { onAction(DesignSystemDemoAction.ToggleOverride) },
                    modifier = Modifier.testTag(DesignSystemDemoTags.Override),
                    variant = NomiButtonVariant.Default,
                ) {
                    val label = if (state.override) {
                        Res.string.design_system_demo_override_on
                    } else Res.string.design_system_demo_override_off
                    NomiText(stringResource(label))
                }
                NomiButton(
                    onClick = { onAction(DesignSystemDemoAction.Reset) },
                    modifier = Modifier.testTag(DesignSystemDemoTags.Reset),
                ) { NomiText(stringResource(Res.string.design_system_demo_reset)) }
            }
        }
    }
}

/**
 * 标题与可自然换行的选项组，不使用固定高度约束按钮文字。
 *
 * @param title 选项组上方显示的标题。
 * @param content 在 FlowRow 内组合的选项内容，无接收者作用域，间距由组容器提供。
 */
@Composable
private fun DemoControlGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
        NomiText(text = title, style = NomiTheme.typography.titleSmall)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
            verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
        ) { content() }
    }
}

/**
 * 使用普通按钮表达一个互斥选项，并提供稳定选中语义。
 *
 * @param label 选项按钮的显示文字。
 * @param selected 是否选中，决定主按钮样式与 selected 语义。
 * @param tag 用于测试定位的稳定按钮标签。
 * @param onClick 点击选项时触发；选中状态由调用方更新。
 */
@Composable
private fun DemoChoice(label: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    NomiButton(
        onClick = onClick,
        modifier = Modifier.testTag(tag).semantics { this.selected = selected },
        variant = if (selected) NomiButtonVariant.Primary else NomiButtonVariant.Default,
    ) {
        NomiText(label)
    }
}

/**
 * 将已知标识转换为资源名称，未知宿主候选仍通过资源格式化显示稳定身份。
 *
 * @param id 待显示的设计系统标识。
 * @return 当前语言下的候选名称；未知标识使用宿主名称模板包含其原始值。
 */
@Composable
private fun designSystemName(id: NomiDesignSystemId): String = when (id.value) {
    "nomi" -> stringResource(Res.string.design_system_demo_nomi)
    "material3" -> stringResource(Res.string.design_system_demo_material3)
    "fluent" -> stringResource(Res.string.design_system_demo_fluent)
    else -> stringResource(Res.string.design_system_demo_host_system, id.value)
}

/**
 * 不启动状态机的设置面板预览。
 */
@AppPreview
@Composable
private fun DesignSystemDemoControlsPreview() {
    SamplePreviewTheme {
        DesignSystemDemoControls(
            state = DesignSystemDemoUiState(),
            systems = DefaultSampleDesignSystems,
            onAction = {},
        )
    }
}
