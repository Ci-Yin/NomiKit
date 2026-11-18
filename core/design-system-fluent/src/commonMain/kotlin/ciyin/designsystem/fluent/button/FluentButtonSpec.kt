package ciyin.designsystem.fluent.button

import androidx.compose.runtime.Immutable
import ciyin.designsystem.component.button.NomiButtonSpec
import io.github.composefluent.component.ButtonColorScheme

/**
 * Fluent 按钮未进入公共 API 的专属参数，实现按钮族的契约 [NomiButtonSpec]。
 *
 * @property colors 原生五态色板；null 表示沿用按 [ciyin.designsystem.component.button.NomiButtonColors]
 *   推导出的原生色板。用于覆盖中立四字段表达不了的悬停、按下、聚焦态与描边。
 */
@Immutable
data class FluentButtonSpec(
    val colors: ButtonColorScheme? = null,
) : NomiButtonSpec
