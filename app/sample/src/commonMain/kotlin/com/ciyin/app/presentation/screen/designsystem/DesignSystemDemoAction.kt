package com.ciyin.app.presentation.screen.designsystem

import androidx.compose.runtime.Immutable
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.theme.DarkMode

/**
 * 设计系统展示页的同步用户意图。
 */
@Immutable
internal sealed interface DesignSystemDemoAction {
    /**
     * 选择宿主目录中的设计系统，并在动作边界清理其不支持的覆盖。
     *
     * @property id 宿主候选的稳定标识；不在候选目录中的标识会被忽略。
     */
    data class SelectDesignSystem(val id: NomiDesignSystemId) : DesignSystemDemoAction

    /**
     * 选择外观模式。
     *
     * @property value 用户请求的外观模式；System 由主题边界解析为当前系统模式。
     */
    data class SelectDarkMode(val value: DarkMode) : DesignSystemDemoAction

    /**
     * 选择按钮尺寸覆盖，null 表示恢复原生默认。
     *
     * @property value 控件尺寸覆盖；null 恢复原生默认，当前系统不支持的非空尺寸会被忽略。
     */
    data class SelectSize(val value: NomiControlSize?) : DesignSystemDemoAction

    /**
     * 选择按钮样式覆盖，null 表示恢复原生默认。
     *
     * @property value 按钮视觉语义覆盖；null 恢复当前系统的原生样式。
     */
    data class SelectVariant(val value: NomiButtonVariant?) : DesignSystemDemoAction

    /**
     * 选择受支持的字体缩放倍数。
     *
     * @property value 目标字体缩放倍数；仅接受 [DesignSystemDemoFontScales] 中的档位。
     */
    data class SelectFontScale(val value: Float) : DesignSystemDemoAction

    /**
     * 切换展示按钮是否可用。
     */
    data object ToggleEnabled : DesignSystemDemoAction

    /**
     * 切换局部主题覆盖。
     */
    data object ToggleOverride : DesignSystemDemoAction

    /**
     * 对一个有效展示点击计数。
     */
    data object ButtonClick : DesignSystemDemoAction

    /**
     * 将全部配置和点击计数恢复初始状态。
     */
    data object Reset : DesignSystemDemoAction

    /**
     * 请求返回样例列表。
     */
    data object BackClick : DesignSystemDemoAction
}
