package ciyin.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 亮色模式矢量图标，固有尺寸为 256.dp × 256.dp；首次读取时构建，后续读取复用文件级缓存。
 */
val IconPack.LightMode: ImageVector
    get() {
        if (_LightMode2 != null) {
            return _LightMode2!!
        }
        _LightMode2 = ImageVector.Builder(
            name = "LightMode",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(554.7f, 42.7f)
                verticalLineToRelative(85.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -85.3f,
                    0f
                )
                lineTo(469.3f, 42.7f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    85.3f,
                    0f
                )
                close()
                moveTo(512f, 853.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -42.7f,
                    42.7f
                )
                verticalLineToRelative(85.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    85.3f,
                    0f
                )
                verticalLineToRelative(-85.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -42.7f,
                    -42.7f
                )
                close()
                moveTo(150f, 150f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    60.3f
                )
                lineToRelative(60.3f, 60.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    60.3f,
                    -60.3f
                )
                lineTo(210.3f, 150f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -60.3f,
                    0f
                )
                close()
                moveTo(753.4f, 753.4f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    60.3f
                )
                lineToRelative(60.3f, 60.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    60.3f,
                    -60.3f
                )
                lineToRelative(-60.3f, -60.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -60.3f,
                    0f
                )
                close()
                moveTo(0f, 512f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    42.7f,
                    42.7f
                )
                horizontalLineToRelative(85.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    -85.3f
                )
                lineTo(42.7f, 469.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -42.7f,
                    42.7f
                )
                close()
                moveTo(853.3f, 512f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    42.7f,
                    42.7f
                )
                horizontalLineToRelative(85.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    -85.3f
                )
                horizontalLineToRelative(-85.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -42.7f,
                    42.7f
                )
                close()
                moveTo(150f, 874f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    60.3f,
                    0f
                )
                lineToRelative(60.3f, -60.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -60.3f,
                    -60.3f
                )
                lineToRelative(-60.3f, 60.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    60.3f
                )
                close()
                moveTo(753.4f, 270.6f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    60.3f,
                    0f
                )
                lineToRelative(60.3f, -60.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -60.3f,
                    -60.3f
                )
                lineToRelative(-60.3f, 60.3f)
                arcToRelative(
                    42.7f,
                    42.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    60.3f
                )
                close()
                moveTo(768f, 512f)
                arcToRelative(
                    256f,
                    256f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    -256f,
                    -256f
                )
                arcToRelative(
                    256f,
                    256f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    256f,
                    256f
                )
                close()
                moveTo(682.7f, 512f)
                arcToRelative(
                    170.7f,
                    170.7f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -170.7f,
                    170.7f
                )
                arcToRelative(
                    170.7f,
                    170.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    170.7f,
                    -170.7f
                )
                close()
            }
        }.build()

        return _LightMode2!!
    }

/**
 * 文件级亮色模式矢量缓存；null 表示尚未构建，由图标属性首次读取时填充并供后续读取复用。
 */
@Suppress("ObjectPropertyName")
private var _LightMode2: ImageVector? = null
