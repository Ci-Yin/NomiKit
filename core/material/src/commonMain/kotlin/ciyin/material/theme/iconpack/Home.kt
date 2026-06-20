package ciyin.material.theme.iconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 首页图标。
 */
val IconPack.Home: ImageVector
    get() {
        if (_Home != null) {
            return _Home!!
        }
        _Home = ImageVector.Builder(
            name = "Home",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color(0xFF040000))) {
                moveTo(1010.8f, 510.8f)
                lineTo(544.9f, 31.7f)
                curveToRelative(-8.7f, -8.9f, -20.4f, -13.9f, -32.9f, -13.9f)
                curveToRelative(-0.3f, 0f, -0.6f, 0f, -0.9f, 0f)
                curveToRelative(-12.8f, -0.2f, -24.8f, 4.8f, -33.7f, 13.9f)
                lineTo(13.2f, 510.9f)
                curveTo(0.6f, 523.8f, -3.7f, 548.8f, 3.5f, 566.4f)
                curveToRelative(6.9f, 16.8f, 22.7f, 33.7f, 42.6f, 33.7f)
                horizontalLineToRelative(69.7f)
                verticalLineToRelative(349.4f)
                curveToRelative(0f, 24.2f, 26.5f, 56.7f, 53.7f, 56.7f)
                horizontalLineToRelative(204.5f)
                curveToRelative(26.3f, 0f, 42.6f, -28f, 42.6f, -53.9f)
                lineTo(416.6f, 787.7f)
                horizontalLineToRelative(164.4f)
                verticalLineToRelative(161.8f)
                curveToRelative(0f, 17.4f, 12.8f, 32.3f, 18.4f, 37.9f)
                curveToRelative(5.6f, 5.7f, 20.4f, 18.8f, 37.7f, 18.8f)
                horizontalLineToRelative(201.4f)
                curveToRelative(13.4f, 0f, 25.6f, -7.8f, 34.2f, -21.8f)
                curveToRelative(6.3f, -10.4f, 10.1f, -23.4f, 10.1f, -34.9f)
                verticalLineToRelative(-349.4f)
                horizontalLineToRelative(95.2f)
                curveToRelative(19.9f, 0f, 35.7f, -16.9f, 42.6f, -33.7f)
                curveToRelative(7.2f, -17.6f, 2.9f, -42.5f, -9.7f, -55.5f)
                close()
                moveTo(788.9f, 551.2f)
                verticalLineToRelative(361.2f)
                lineTo(674.8f, 912.4f)
                lineTo(674.8f, 731.6f)
                curveToRelative(0f, -9.1f, -1.1f, -20.6f, -9.9f, -28.6f)
                curveToRelative(-7.9f, -7.2f, -18.1f, -8.2f, -27.9f, -8.2f)
                horizontalLineToRelative(-267.3f)
                curveToRelative(-12.4f, 0f, -21.5f, 1.9f, -28.8f, 5.9f)
                curveToRelative(-11f, 6.2f, -17.1f, 17.1f, -17.1f, 30.9f)
                verticalLineToRelative(180.8f)
                horizontalLineToRelative(-115.1f)
                lineTo(208.7f, 543.5f)
                curveToRelative(0f, -37.2f, -29.5f, -37.2f, -39.2f, -37.2f)
                horizontalLineToRelative(-13.6f)
                lineToRelative(355.4f, -366.8f)
                lineToRelative(356.6f, 366.8f)
                horizontalLineToRelative(-24.3f)
                curveToRelative(-25.9f, 0f, -54.7f, 18.4f, -54.7f, 44.9f)
                close()
            }
        }.build()

        return _Home!!
    }

/** 缓存已构建的首页图标。 */
@Suppress("ObjectPropertyName")
private var _Home: ImageVector? = null
