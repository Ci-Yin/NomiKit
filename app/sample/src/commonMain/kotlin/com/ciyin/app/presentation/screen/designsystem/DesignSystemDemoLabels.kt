package com.ciyin.app.presentation.screen.designsystem

import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.theme.DarkMode
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_size_small
import com.ciyin.app.sample.design_system_demo_size_medium
import com.ciyin.app.sample.design_system_demo_size_large
import com.ciyin.app.sample.design_system_demo_variant_primary
import com.ciyin.app.sample.design_system_demo_variant_default
import com.ciyin.app.sample.design_system_demo_variant_subtle
import com.ciyin.app.sample.design_system_demo_variant_text
import com.ciyin.app.sample.design_system_demo_variant_destructive
import com.ciyin.app.sample.design_system_demo_mode_system
import com.ciyin.app.sample.design_system_demo_mode_light
import com.ciyin.app.sample.design_system_demo_mode_dark
import com.ciyin.app.sample.design_system_demo_scale_normal
import com.ciyin.app.sample.design_system_demo_scale_large
import com.ciyin.app.sample.design_system_demo_scale_extra_large
import org.jetbrains.compose.resources.StringResource

/**
 * 返回按钮尺寸的资源标识，不把文案存入状态机。
 *
 * @param size 要显示名称的显式按钮尺寸档位。
 * @return 对应尺寸名称的字符串资源，尚未按语言解析。
 */
internal fun designSystemSizeLabel(size: NomiControlSize): StringResource = when (size) {
    NomiControlSize.Small -> Res.string.design_system_demo_size_small
    NomiControlSize.Medium -> Res.string.design_system_demo_size_medium
    NomiControlSize.Large -> Res.string.design_system_demo_size_large
}

/**
 * 返回中立按钮视觉语义的资源标识。
 *
 * @param variant 要显示名称的显式按钮视觉语义。
 * @return 对应按钮样式名称的字符串资源。
 */
internal fun designSystemVariantLabel(variant: NomiButtonVariant): StringResource = when (variant) {
    NomiButtonVariant.Primary -> Res.string.design_system_demo_variant_primary
    NomiButtonVariant.Default -> Res.string.design_system_demo_variant_default
    NomiButtonVariant.Subtle -> Res.string.design_system_demo_variant_subtle
    NomiButtonVariant.Text -> Res.string.design_system_demo_variant_text
    NomiButtonVariant.Destructive -> Res.string.design_system_demo_variant_destructive
}

/**
 * 返回外观模式的资源标识。
 *
 * @param mode 用户请求的模式，System 作为独立选项显示，不在此解析为明暗模式。
 * @return 跟随系统、浅色或深色选项的字符串资源。
 */
internal fun designSystemDarkModeLabel(mode: DarkMode): StringResource = when (mode) {
    DarkMode.System -> Res.string.design_system_demo_mode_system
    DarkMode.Light -> Res.string.design_system_demo_mode_light
    DarkMode.Dark -> Res.string.design_system_demo_mode_dark
}

/**
 * 返回固定字体档位的资源标识。
 *
 * @param scale 字体缩放倍数，仅支持 1、1.3 和 2。
 * @return 对应普通、大号或超大字体档位的字符串资源。
 * @throws IllegalStateException scale 不在支持档位中时抛出。
 */
internal fun designSystemFontScaleLabel(scale: Float): StringResource = when (scale) {
    1f -> Res.string.design_system_demo_scale_normal
    1.3f -> Res.string.design_system_demo_scale_large
    2f -> Res.string.design_system_demo_scale_extra_large
    else -> error("不支持的展示字体缩放：$scale")
}
