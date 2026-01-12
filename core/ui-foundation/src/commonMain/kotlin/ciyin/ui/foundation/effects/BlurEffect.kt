package ciyin.ui.foundation.effects

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.Dp

/**
 * 在 Android 11 (api 30) 以下使用纯色背景代替 [Modifier.blur] 的效果
 */
@Composable
expect fun Modifier.blurEffect(
    radius: Dp,
    edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle
): Modifier