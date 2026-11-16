package ciyin.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 首页矢量图标，固有尺寸为 256.dp × 256.dp；首次读取时构建，后续读取复用文件级缓存。
 */
val IconPack.Home: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "Home",
        defaultWidth = 200.dp,
        defaultHeight = 200.dp,
        viewportWidth = 200f,
        viewportHeight = 200f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 14f,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(100f, 21f)
            curveTo(93f, 21f, 88f, 24f, 81f, 29f)
            lineTo(24f, 73f)
            curveTo(18f, 78f, 16f, 84f, 16f, 93f)
            verticalLineTo(146f)
            curveTo(16f, 167f, 32f, 184f, 54f, 184f)
            horizontalLineTo(146f)
            curveTo(168f, 184f, 184f, 167f, 184f, 146f)
            verticalLineTo(93f)
            curveTo(184f, 84f, 182f, 78f, 176f, 73f)
            lineTo(119f, 29f)
            curveTo(112f, 24f, 107f, 21f, 100f, 21f)
            close()
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 14f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(100f, 96f)
            verticalLineTo(151f)
        }
    }.build()
}

