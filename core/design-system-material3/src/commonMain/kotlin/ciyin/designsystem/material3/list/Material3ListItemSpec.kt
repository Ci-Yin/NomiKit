package ciyin.designsystem.material3.list

import androidx.compose.material3.ListItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import ciyin.designsystem.component.list.NomiListItemSpec

/**
 * Material 3 列表项未进入公共 API 的专属参数，实现列表项的族契约 [NomiListItemSpec]。
 *
 * 每个字段的 null 都表示"未指定"并回落原生默认；同时传入其它设计系统的列表项参数不会互相影响。
 * 由调用方经 NomiListItem 的 nativeSpec 参数传入，只被 Material 3 列表项渲染器消费。
 *
 * @property overlineContent 原生 overline 槽；null 表示不占位，不影响行高推断以外的原生行为。
 * @property colors 原生颜色方案；null 保留 `ListItemDefaults.colors()`。
 * @property tonalElevation 原生色调阴影；null 保留 `ListItemDefaults.Elevation`。
 * @property shadowElevation 原生投影；null 保留 `ListItemDefaults.Elevation`。
 */
@Immutable
data class Material3ListItemSpec(
    val overlineContent: (@Composable () -> Unit)? = null,
    val colors: ListItemColors? = null,
    val tonalElevation: Dp? = null,
    val shadowElevation: Dp? = null,
) : NomiListItemSpec
