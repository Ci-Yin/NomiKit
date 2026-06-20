package ciyin.material.theme.iconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 向右箭头图标。
 */
val IconPack.ArrowRight: ImageVector
    get() {
        if (_ArrowRight != null) {
            return _ArrowRight!!
        }
        _ArrowRight = ImageVector.Builder(
            name = "ArrowRight",
            defaultWidth = 200.dp,
            defaultHeight = 200.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(741.3f, 512.3f)
                curveToRelative(-0.2f, -10.7f, -4.6f, -20.8f, -12.1f, -28.3f)
                lineTo(355.6f, 110.4f)
                curveToRelative(-16.2f, -16.2f, -42.5f, -16.2f, -58.7f, 0f)
                curveToRelative(-16.2f, 16.2f, -16.2f, 42.5f, 0f, 58.7f)
                lineToRelative(344.2f, 344.2f)
                lineToRelative(-197.3f, 197.3f)
                curveToRelative(-16.2f, 16.2f, -16.2f, 42.5f, 0f, 58.7f)
                curveToRelative(16.2f, 16.2f, 42.5f, 16.2f, 58.7f, 0f)
                lineToRelative(226.7f, -226.7f)
                curveToRelative(2.5f, -2.5f, 4.6f, -5.2f, 6.4f, -8.3f)
                curveToRelative(0.3f, -0.6f, 0.7f, -1.2f, 1f, -1.8f)
                curveToRelative(0.8f, -1.5f, 1.5f, -3f, 2.1f, -4.6f)
                curveToRelative(0.5f, -1.3f, 0.9f, -2.5f, 1.2f, -3.8f)
                curveToRelative(0.9f, -3.2f, 1.3f, -6.5f, 1.4f, -9.9f)
                verticalLineToRelative(-1.9f)
                close()
                moveTo(385.2f, 828f)
                curveToRelative(-16.2f, -16.2f, -42.5f, -16.2f, -58.7f, 0f)
                lineToRelative(-29.3f, 29.3f)
                curveToRelative(-16.2f, 16.2f, -16.2f, 42.5f, 0f, 58.7f)
                curveToRelative(16.2f, 16.2f, 42.5f, 16.2f, 58.7f, 0f)
                lineToRelative(29.3f, -29.3f)
                curveToRelative(16.2f, -16.2f, 16.2f, -42.5f, 0f, -58.7f)
                close()
            }
        }.build()

        return _ArrowRight!!
    }

/** 缓存已构建的向右箭头图标。 */
@Suppress("ObjectPropertyName")
private var _ArrowRight: ImageVector? = null
