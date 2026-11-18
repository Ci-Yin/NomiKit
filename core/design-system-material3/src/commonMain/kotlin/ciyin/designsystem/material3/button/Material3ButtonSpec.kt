package ciyin.designsystem.material3.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Immutable
import ciyin.designsystem.component.button.NomiButtonSpec

/**
 * Material 3 按钮未进入公共 API 的专属参数，实现按钮族的契约 [NomiButtonSpec]。
 *
 * 颜色已由中立的 `NomiButtonColors` 覆盖，因此这里只提供原生独有的阴影、描边与交互源。
 *
 * @property elevation 原生 elevation 对象；null 保留所选变体的原生 elevation。
 * @property border 原生描边；null 表示不描边（与原生默认一致）。
 * @property interactionSource 可选交互源；null 由原生按钮自行创建。
 */
@Immutable
data class Material3ButtonSpec(
    val elevation: ButtonElevation? = null,
    val border: BorderStroke? = null,
    val interactionSource: MutableInteractionSource? = null,
) : NomiButtonSpec
