package com.ciyin.app.ui.theme.iconpack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

val IconPack.Settings: ImageVector
    get() {
        if (_Settings2 != null) {
            return _Settings2!!
        }
        _Settings2 = ImageVector.Builder(
            name = "Settings",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(512f, 346.7f)
                arcToRelative(
                    165.3f,
                    165.3f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    0f,
                    330.7f
                )
                arcToRelative(
                    165.3f,
                    165.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    -330.7f
                )
                close()
                moveTo(512f, 421.3f)
                arcToRelative(
                    90.7f,
                    90.7f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    0f,
                    181.3f
                )
                arcToRelative(
                    90.7f,
                    90.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    0f,
                    -181.3f
                )
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(517.9f, 48f)
                curveToRelative(-39.6f, 0f, -78.5f, 4.9f, -116.1f, 14.7f)
                arcToRelative(
                    37.3f,
                    37.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -27.9f,
                    38.1f
                )
                arcToRelative(
                    90.7f,
                    90.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -139.9f,
                    80.9f
                )
                arcToRelative(
                    37.3f,
                    37.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -46.9f,
                    5.2f
                )
                arcToRelative(
                    463.3f,
                    463.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -116.3f,
                    201.2f
                )
                arcToRelative(
                    37.3f,
                    37.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    19f,
                    43.2f
                )
                arcTo(90.7f, 90.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 139.2f, 512f)
                arcToRelative(
                    90.7f,
                    90.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -49.5f,
                    80.9f
                )
                arcToRelative(
                    37.3f,
                    37.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -19f,
                    43.2f
                )
                arcToRelative(
                    463.4f,
                    463.4f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    116.3f,
                    201.2f
                )
                arcToRelative(
                    37.3f,
                    37.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    46.9f,
                    5.1f
                )
                arcToRelative(
                    90.7f,
                    90.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    94.7f,
                    -2.4f
                )
                arcToRelative(
                    90.6f,
                    90.6f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    45.4f,
                    78.7f
                )
                lineToRelative(-0.1f, 4.6f)
                arcToRelative(
                    37.3f,
                    37.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    27.9f,
                    38.1f
                )
                curveToRelative(37.7f, 9.7f, 76.6f, 14.7f, 116.2f, 14.7f)
                curveToRelative(39.6f, 0f, 78.5f, -4.9f, 116.1f, -14.7f)
                arcToRelative(
                    37.3f,
                    37.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    27.9f,
                    -38.1f
                )
                arcToRelative(
                    90.7f,
                    90.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    139.9f,
                    -80.9f
                )
                arcToRelative(
                    37.3f,
                    37.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    46.9f,
                    -5.2f
                )
                arcToRelative(
                    463.3f,
                    463.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    116.3f,
                    -201.2f
                )
                arcToRelative(
                    37.3f,
                    37.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -19f,
                    -43.2f
                )
                arcTo(90.7f, 90.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 896.6f, 512f)
                arcToRelative(
                    90.7f,
                    90.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    49.5f,
                    -80.8f
                )
                arcToRelative(
                    37.3f,
                    37.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    19f,
                    -43.2f
                )
                arcToRelative(
                    463.4f,
                    463.4f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -116.3f,
                    -201.2f
                )
                arcToRelative(
                    37.3f,
                    37.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -46.9f,
                    -5.1f
                )
                arcToRelative(
                    90.7f,
                    90.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -94.7f,
                    2.4f
                )
                arcToRelative(
                    90.6f,
                    90.6f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -45.2f,
                    -83.3f
                )
                arcToRelative(
                    37.3f,
                    37.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -27.9f,
                    -38.1f
                )
                arcToRelative(
                    464.8f,
                    464.8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -116.2f,
                    -14.7f
                )
                close()
                moveTo(529f, 122.8f)
                arcToRelative(
                    391.3f,
                    391.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    54.5f,
                    5.3f
                )
                lineToRelative(5.4f, 1f)
                lineToRelative(0.4f, 3f)
                arcToRelative(
                    165.2f,
                    165.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    80.5f,
                    116.6f
                )
                lineToRelative(5.7f, 3.1f)
                arcToRelative(
                    165.2f,
                    165.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    133.2f,
                    9.2f
                )
                lineToRelative(5.1f, -2f)
                lineToRelative(1.7f, 1.9f)
                arcToRelative(
                    388.7f,
                    388.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    68.6f,
                    118.6f
                )
                lineToRelative(0.9f, 2.5f)
                lineToRelative(-2.4f, 1.9f)
                arcTo(
                    165.2f,
                    165.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    821.9f,
                    512f
                )
                lineToRelative(0.1f, 6.1f)
                arcTo(
                    165.2f,
                    165.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    882.6f,
                    640f
                )
                lineToRelative(2.4f, 1.9f)
                lineToRelative(-0.9f, 2.5f)
                arcToRelative(
                    388.7f,
                    388.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -68.6f,
                    118.6f
                )
                lineToRelative(-1.7f, 1.9f)
                lineToRelative(-5.1f, -1.9f)
                arcToRelative(
                    165.1f,
                    165.1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -138.8f,
                    12.3f
                )
                lineToRelative(-5.2f, 3.1f)
                arcToRelative(
                    165.2f,
                    165.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -75.3f,
                    113.5f
                )
                lineToRelative(-0.4f, 3f)
                lineToRelative(-5.4f, 1f)
                curveToRelative(-21.5f, 3.6f, -43.4f, 5.5f, -65.6f, 5.5f)
                lineToRelative(-11.1f, -0.1f)
                arcToRelative(
                    391.3f,
                    391.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -54.5f,
                    -5.3f
                )
                lineToRelative(-5.5f, -1f)
                lineToRelative(-0.4f, -3f)
                arcToRelative(
                    165.2f,
                    165.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -80.5f,
                    -116.6f
                )
                lineToRelative(-5.7f, -3.1f)
                arcToRelative(
                    165.2f,
                    165.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -133.2f,
                    -9.2f
                )
                lineToRelative(-5.2f, 2f)
                lineToRelative(-1.6f, -1.9f)
                arcToRelative(
                    388.7f,
                    388.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -68.6f,
                    -118.6f
                )
                lineToRelative(-0.9f, -2.5f)
                lineToRelative(2.4f, -1.9f)
                arcTo(
                    165.2f,
                    165.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    213.9f,
                    512f
                )
                lineToRelative(-0.1f, -6.1f)
                arcTo(
                    165.2f,
                    165.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    153.2f,
                    384f
                )
                lineToRelative(-2.4f, -1.9f)
                lineToRelative(0.9f, -2.5f)
                arcToRelative(
                    388.6f,
                    388.6f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    68.6f,
                    -118.6f
                )
                lineToRelative(1.7f, -1.9f)
                lineToRelative(5.1f, 2f)
                arcToRelative(
                    165.1f,
                    165.1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    138.9f,
                    -12.3f
                )
                lineToRelative(5.2f, -3.1f)
                arcToRelative(
                    165.2f,
                    165.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    75.3f,
                    -113.5f
                )
                lineToRelative(0.4f, -3f)
                lineToRelative(5.5f, -1f)
                arcToRelative(
                    392.4f,
                    392.4f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    65.6f,
                    -5.5f
                )
                lineToRelative(11.1f, 0.1f)
                close()
            }
        }.build()

        return _Settings2!!
    }

@Suppress("ObjectPropertyName")
private var _Settings2: ImageVector? = null

@Preview
@Composable
private fun Settings2Preview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Settings, contentDescription = null)
    }
}
