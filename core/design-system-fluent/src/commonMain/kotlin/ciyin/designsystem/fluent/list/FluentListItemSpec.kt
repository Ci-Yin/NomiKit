package ciyin.designsystem.fluent.list

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import ciyin.designsystem.component.list.NomiListItemSpec
import io.github.composefluent.scheme.VisualStateScheme
import io.github.composefluent.surface.CardColor

/**
 * Fluent 列表项未进入公共 API 的专属参数，实现列表项的族契约 [NomiListItemSpec]。
 *
 * 每个字段的默认值都等于 `CardExpanderItem` 的原生默认值，因此传空 spec 不改变外观；
 * 由调用方经 NomiListItem 的 nativeSpec 参数传入，只被 Fluent 列表项渲染器消费。
 *
 * 静态行（无点击回调）没有 enabled、colors 与 interactionSource 的原生入口，这些字段会在该分支被
 * 忽略并记录一次警告。
 *
 * @property dropdown 原生尾部展开槽，渲染在 trailing 之后；null 表示不占位。
 * @property shape 卡片轮廓；null 保留 `FluentTheme.shapes.control`。
 * @property enabled 是否接受交互；仅点击行生效，默认 true 与原生一致。
 * @property colors 卡片四态配色；null 保留 `ExpanderDefaults.cardExpanderItemColors()`。
 * @property interactionSource 可选交互源；null 由原生组件自行创建。
 */
@Immutable
data class FluentListItemSpec(
    val dropdown: (@Composable () -> Unit)? = null,
    val shape: Shape? = null,
    val enabled: Boolean = true,
    val colors: VisualStateScheme<CardColor>? = null,
    val interactionSource: MutableInteractionSource? = null,
) : NomiListItemSpec
