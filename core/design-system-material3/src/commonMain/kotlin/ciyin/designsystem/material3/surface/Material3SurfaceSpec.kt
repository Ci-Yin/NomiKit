package ciyin.designsystem.material3.surface

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import ciyin.designsystem.component.surface.NomiSurfaceSpec

/**
 * Material 3 表面未进入公共 API 的专属参数，实现表面族的契约 [NomiSurfaceSpec]。
 *
 * @property tonalElevation 原生色调阴影；null 保留原生 0.dp。
 * @property shadowElevation 原生投影；null 保留原生 0.dp。
 */
@Immutable
data class Material3SurfaceSpec(
    val tonalElevation: Dp? = null,
    val shadowElevation: Dp? = null,
) : NomiSurfaceSpec
