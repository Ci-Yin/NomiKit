package ciyin.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Immutable

/**
 * 区分原生描边、明确无描边和指定描边，避免 null 吞掉原生默认。
 */
@Immutable
sealed interface NomiBorder {
    /**
     * 使用原生组件的描边默认值。
     */
    data object Default : NomiBorder

    /**
     * 明确移除描边。
     */
    data object None : NomiBorder

    /**
     * 明确指定描边。
     *
     * @property value 应用于组件边缘的描边宽度和画刷。
     */
    data class Stroke(val value: BorderStroke) : NomiBorder
}
