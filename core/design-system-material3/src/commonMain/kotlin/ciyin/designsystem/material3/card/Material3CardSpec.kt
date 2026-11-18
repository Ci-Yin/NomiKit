package ciyin.designsystem.material3.card

import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Immutable
import ciyin.designsystem.component.card.NomiCardSpec

/**
 * Material 3 卡片未进入公共 API 的专属参数，实现卡片族的契约 [NomiCardSpec]。
 *
 * @property elevation 原生 elevation 对象；null 保留 `CardDefaults.cardElevation()`。
 */
@Immutable
data class Material3CardSpec(
    val elevation: CardElevation? = null,
) : NomiCardSpec
