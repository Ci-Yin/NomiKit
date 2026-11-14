package com.ciyin.app.presentation.screen.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalDensity
import ciyin.ui.foundation.layout.grid.VerticalGrid
import ciyin.ui.foundation.layout.grid.SimpleGridCells
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.NomiComponentCapabilities
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoModel
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoTags
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_native_default
import com.ciyin.app.sample.design_system_demo_native_defaults_title
import com.ciyin.app.sample.design_system_demo_native_description
import com.ciyin.app.sample.design_system_demo_sample_text
import com.ciyin.app.sample.design_system_demo_sample_button
import com.ciyin.app.sample.design_system_demo_sample_surface
import com.ciyin.app.sample.design_system_demo_sample_card
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 首期四组件及限制与覆盖区域的纯展示编排，不持有页面 Action 或状态机。
 *
 * @param model 各展示分区共用的配置、能力、计数和局部覆盖输入。
 * @param onButtonClick 可用展示按钮被点击时调用，计数与状态更新由调用方负责。
 * @param modifier 应用于展示区域根 Column 的布局与语义修饰符。
 */
@Composable
internal fun DesignSystemShowcase(
    model: DesignSystemDemoModel,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().testTag(DesignSystemDemoTags.Showcase),
        verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.large),
    ) {
        DesignSystemSection {
            NativeDefaultsShowcase(
                enabled = model.enabled,
                onButtonClick = onButtonClick
            )
        }
        DesignSystemSection {
            DesignSystemButtonShowcase(
                model = model,
                onButtonClick = onButtonClick
            )
        }
        DesignSystemRestrictionShowcase(model)
        DesignSystemSection { DesignSystemTextShowcase() }
        DesignSystemSection { DesignSystemSurfaceShowcase() }
        DesignSystemSection {
            DesignSystemCardShowcase(
                enabled = model.enabled,
                onButtonClick = onButtonClick
            )
        }
        DesignSystemSection {
            DesignSystemOverrideShowcase(
                model.override,
                model.alternateDesignSystem
            )
        }
    }
}

/**
 * 原生默认区的四个样例组件均不传 shape、colors、padding 或 style；标题与说明独立使用主题排版。
 *
 * @param enabled 原生按钮样例是否可用，其他静态样例不受影响。
 * @param onButtonClick 原生按钮样例可用且被点击时调用。
 */
@Composable
private fun NativeDefaultsShowcase(enabled: Boolean, onButtonClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("design-system-demo-native-defaults"),
        verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium),
    ) {
        NomiText(
            text = stringResource(Res.string.design_system_demo_native_defaults_title),
            style = NomiTheme.typography.titleLarge,
        )
        NomiText(
            text = stringResource(Res.string.design_system_demo_native_description),
            style = NomiTheme.typography.bodySmall,
        )
        VerticalGrid(
            columns = SimpleGridCells.Adaptive(NomiTheme.sizes.layoutConstraints.cardMinWidth / 2 * LocalDensity.current.fontScale),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.large),
            verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.large),
        ) {
            NativeExample(label = stringResource(Res.string.design_system_demo_sample_text)) {
                NomiText(
                    text = stringResource(Res.string.design_system_demo_native_default),
                    modifier = Modifier.testTag("design-system-demo-native-text"),
                )
            }
            NativeExample(label = stringResource(Res.string.design_system_demo_sample_button)) {
                NomiButton(
                    onClick = onButtonClick,
                    modifier = Modifier.testTag("design-system-demo-native-button"),
                    enabled = enabled,
                ) { NomiText(stringResource(Res.string.design_system_demo_native_default)) }
            }
            NativeExample(label = stringResource(Res.string.design_system_demo_sample_surface)) {
                NomiSurface(modifier = Modifier.testTag("design-system-demo-native-surface")) {
                    NomiText(stringResource(Res.string.design_system_demo_native_default))
                }
            }
            NativeExample(label = stringResource(Res.string.design_system_demo_sample_card)) {
                NomiCard(modifier = Modifier.testTag("design-system-demo-native-card")) {
                    NomiText(stringResource(Res.string.design_system_demo_native_default))
                }
            }
        }
    }
}

/**
 * 标签与原生样例分离；padding 属于展示单元，不改变样例本身的内部度量。
 *
 * @param label 样例上方的组件名称标签。
 * @param content 在独立 Box 内组合的原生样例，无接收者作用域，不继承标签指定的文本样式。
 */
@Composable
private fun NativeExample(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
        NomiText(text = label, style = NomiTheme.typography.labelMedium)
        Box(modifier = Modifier.padding(vertical = NomiTheme.spacings.small)) { content() }
    }
}

/**
 * 展示编排纯预览。
 */
@AppPreview
@Composable
private fun DesignSystemShowcasePreview() {
    SamplePreviewTheme {
        DesignSystemShowcase(
            model = DesignSystemDemoModel(
                size = null,
                variant = null,
                enabled = true,
                override = true,
                clickCount = 3,
                capabilities = NomiComponentCapabilities(),
                alternateDesignSystem = null,
            ),
            onButtonClick = {},
        )
    }
}
