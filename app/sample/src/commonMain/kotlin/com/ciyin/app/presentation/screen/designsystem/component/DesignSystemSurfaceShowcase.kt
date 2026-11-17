package com.ciyin.app.presentation.screen.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import ciyin.ui.foundation.layout.grid.SimpleGridCells
import ciyin.ui.foundation.layout.grid.VerticalGrid
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoTags
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_inherited_content
import com.ciyin.app.sample.design_system_demo_surface_default
import com.ciyin.app.sample.design_system_demo_surface_high
import com.ciyin.app.sample.design_system_demo_surface_higher
import com.ciyin.app.sample.design_system_demo_surface_low
import com.ciyin.app.sample.design_system_demo_surface_lower
import com.ciyin.app.sample.design_system_demo_surfaces_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 展示五级表面覆盖；内容颜色保持未指定，验证 slot 原生继承。
 */
@Composable
internal fun DesignSystemSurfaceShowcase() {
    val levels = listOf(
        Res.string.design_system_demo_surface_lower to nativeSurfaceColor(ShowcaseSurfaceLevel.Lower),
        Res.string.design_system_demo_surface_low to nativeSurfaceColor(ShowcaseSurfaceLevel.Low),
        Res.string.design_system_demo_surface_default to nativeSurfaceColor(ShowcaseSurfaceLevel.Default),
        Res.string.design_system_demo_surface_high to nativeSurfaceColor(ShowcaseSurfaceLevel.High),
        Res.string.design_system_demo_surface_higher to nativeSurfaceColor(ShowcaseSurfaceLevel.Higher),
    )
    Column(verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
        NomiText(
            text = stringResource(Res.string.design_system_demo_surfaces_title),
            style = nativeTextStyle(ShowcaseTextRole.TitleLarge)
        )
        VerticalGrid(
            columns = SimpleGridCells.Adaptive(NomiTheme.sizes.layoutConstraints.cardMinWidth * LocalDensity.current.fontScale),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
            verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
        ) {
            levels.forEachIndexed { index, (label, color) ->
                NomiSurface(
                    modifier = Modifier.fillMaxWidth().testTag("design-system-demo-surface-$index"),
                    colors = NomiSurfaceColors(containerColor = color),
                ) {
                    Column(
                        modifier = Modifier.padding(NomiTheme.spacings.medium),
                        verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
                    ) {
                        NomiText(
                            text = stringResource(label),
                            style = NomiTheme.typography.titleSmall
                        )
                        NomiText(stringResource(Res.string.design_system_demo_inherited_content))
                    }
                }
            }
        }
    }
}

/**
 * 表面展示纯内容预览。
 */
@AppPreview
@Composable
private fun DesignSystemSurfaceShowcasePreview() {
    SamplePreviewTheme { DesignSystemSurfaceShowcase() }
}
