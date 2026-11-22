package ciyin.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 勾选矢量图标，固有尺寸为 24.dp × 24.dp；首次读取时构建，后续读取复用文件级缓存。
 *
 * 用于菜单项的选中指示；着色由图标入口的 tint 决定，矢量本身不携带语义色。
 */
val IconPack.Check: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "Check",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 12.5f)
            lineTo(10f, 17.5f)
            lineTo(19f, 7f)
        }
    }.build()
}
