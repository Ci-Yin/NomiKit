package com.ciyin.app.presentation.screen.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.NomiThemeOverride
import ciyin.designsystem.theme.NomiThemeOverrides
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoTags
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_inherited_content
import com.ciyin.app.sample.design_system_demo_outer_after
import com.ciyin.app.sample.design_system_demo_outer_before
import com.ciyin.app.sample.design_system_demo_override_inner
import com.ciyin.app.sample.design_system_demo_override_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 展示局部设计系统与 accent 稀疏覆盖，外层主题不复制完整 tokens。
 *
 * @param enabled 是否启用中间样例的局部覆盖；仅在备用系统非空时生效。
 * @param alternateDesignSystem 中间样例切换到的设计系统；null 时不创建覆盖边界，沿用外层主题。
 */
@Composable
internal fun DesignSystemOverrideShowcase(
    enabled: Boolean,
    alternateDesignSystem: NomiDesignSystem?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium)) {
        NomiText(
            text = stringResource(Res.string.design_system_demo_override_title),
            style = NomiTheme.typography.titleLarge
        )
        OverrideSample(
            text = stringResource(Res.string.design_system_demo_outer_before),
            tag = DesignSystemDemoTags.OuterBefore
        )
        if (enabled && alternateDesignSystem != null) {
            NomiThemeOverride(
                designSystem = alternateDesignSystem,
                overrides = NomiThemeOverrides(accentColor = NomiTheme.colorScheme.info),
            ) {
                OverrideSample(
                    text = stringResource(Res.string.design_system_demo_override_inner),
                    tag = DesignSystemDemoTags.InnerOverride
                )
            }
        } else {
            OverrideSample(
                text = stringResource(Res.string.design_system_demo_override_inner),
                tag = DesignSystemDemoTags.InnerOverride
            )
        }
        OverrideSample(
            text = stringResource(Res.string.design_system_demo_outer_after),
            tag = DesignSystemDemoTags.OuterAfter
        )
    }
}

/**
 * 用显式表面强调色验证局部作用域前后仍可恢复。
 *
 * @param text 显示在表面内的作用域说明，继承当前表面内容样式。
 * @param tag 标记覆盖前、覆盖内或覆盖后表面的测试标签。
 */
@Composable
private fun OverrideSample(text: String, tag: String) {
    NomiSurface(
        modifier = Modifier.fillMaxWidth().testTag(tag),
        colors = NomiSurfaceColors(containerColor = NomiTheme.colorScheme.primary),
    ) {
        NomiText(text = text, modifier = Modifier.padding(NomiTheme.spacings.medium))
    }
}

/**
 * 覆盖区域纯内容预览。
 */
@AppPreview
@Composable
private fun DesignSystemOverrideShowcasePreview() {
    SamplePreviewTheme {
        DesignSystemOverrideShowcase(
            enabled = true,
            alternateDesignSystem = null
        )
    }
}
