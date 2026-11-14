package ciyin.designsystem.icon

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 无绘制路径的空图标占位，固有尺寸为 1.dp × 1.dp；通过 lazy 首次构建并复用实例。
 */
val IconPack.Null: ImageVector by lazy {
    ImageVector.Builder(
        name = "Null",
        defaultWidth = 1.dp,
        defaultHeight = 1.dp,
        viewportWidth = 1f,
        viewportHeight = 1f
    ).build()
}
