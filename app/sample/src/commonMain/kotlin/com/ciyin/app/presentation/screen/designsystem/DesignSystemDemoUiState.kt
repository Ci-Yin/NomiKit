package com.ciyin.app.presentation.screen.designsystem

import androidx.compose.runtime.Immutable
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.theme.DarkMode

/**
 * 展示页可比较的配置状态；不持有渲染器、主题实例或 Compose 滚动对象。
 *
 * @property designSystemId 所选宿主候选的稳定标识，默认指向 Nomi。
 * @property darkMode 用户请求的外观模式，默认跟随系统。
 * @property size 按钮尺寸覆盖；null 保留当前设计系统的原生默认。
 * @property variant 按钮视觉语义覆盖；null 保留当前设计系统的原生默认。
 * @property enabled 展示按钮是否可用；控制面板始终可用。
 * @property customTokens 是否只覆盖主题强调色，默认关闭。
 * @property override 是否启用局部设计系统与强调色覆盖示例，默认关闭。
 * @property fontScale 展示页的字体缩放倍数，默认 1 倍，由动作边界限制为公开档位。
 * @property clickCount 可用展示按钮的有效点击总数，默认从零开始。
 */
@Immutable
internal data class DesignSystemDemoUiState(
    val designSystemId: NomiDesignSystemId = NomiDesignSystem.Default.id,
    val darkMode: DarkMode = DarkMode.System,
    val size: NomiControlSize? = null,
    val variant: NomiButtonVariant? = null,
    val enabled: Boolean = true,
    val customTokens: Boolean = false,
    val override: Boolean = false,
    val fontScale: Float = 1f,
    val clickCount: Int = 0,
)
