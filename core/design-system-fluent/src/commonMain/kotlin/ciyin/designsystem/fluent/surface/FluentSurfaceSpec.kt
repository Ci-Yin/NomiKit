package ciyin.designsystem.fluent.surface

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import ciyin.designsystem.component.surface.NomiSurfaceSpec

/**
 * Fluent 表面未进入公共 API 的专属参数，实现表面族的契约 [NomiSurfaceSpec]。
 *
 * @property elevation 原生 `Layer` 的投影高度；null 保留原生 0.dp。
 */
@Immutable
data class FluentSurfaceSpec(
    val elevation: Dp? = null,
) : NomiSurfaceSpec
