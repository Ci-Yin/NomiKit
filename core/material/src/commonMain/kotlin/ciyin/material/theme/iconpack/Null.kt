package ciyin.material.theme.iconpack

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 空图标占位。
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
