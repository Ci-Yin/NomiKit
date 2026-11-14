package com.ciyin.app.presentation.screen.designsystem

import androidx.compose.runtime.Immutable
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.NomiComponentCapabilities
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.runtime.NomiDesignSystem

/**
 * 展示页支持的字体缩放档位；替换当前字体比例，不改变设备像素密度。
 */
internal val DesignSystemDemoFontScales: List<Float> = listOf(1f, 1.3f, 2f)

/**
 * 独立组件展示区域的输入；组件只接收此模型和局部回调，不依赖页面 Action。
 *
 * @property size 当前按钮尺寸覆盖；null 表示原生默认。
 * @property variant 当前按钮样式覆盖；null 表示原生默认。
 * @property enabled 展示按钮是否可用，不影响页面控制面板。
 * @property override 是否显示局部主题覆盖效果。
 * @property clickCount 当前有效展示点击次数。
 * @property capabilities 当前设计系统声明的覆盖能力，用于决定可展示的参数与限制提示。
 * @property alternateDesignSystem 可用于局部覆盖示例的另一设计系统；null 表示沿用当前系统。
 */
@Immutable
internal data class DesignSystemDemoModel(
    val size: NomiControlSize?,
    val variant: NomiButtonVariant?,
    val enabled: Boolean,
    val override: Boolean,
    val clickCount: Int,
    val capabilities: NomiComponentCapabilities,
    val alternateDesignSystem: NomiDesignSystem?,
)
