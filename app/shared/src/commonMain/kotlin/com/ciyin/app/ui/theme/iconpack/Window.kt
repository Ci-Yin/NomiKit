package com.ciyin.app.ui.theme.iconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val IconPack.Window: ImageVector
    get() {
        if (_Window != null) {
            return _Window!!
        }
        _Window = ImageVector.Builder(
            name = "Window",
            defaultWidth = 200.dp,
            defaultHeight = 200.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(170.7f, 896f)
                horizontalLineToRelative(682.7f)
                curveToRelative(47.1f, 0f, 85.3f, -38.3f, 85.3f, -85.3f)
                lineTo(938.7f, 213.3f)
                curveToRelative(0f, -47.1f, -38.3f, -85.3f, -85.3f, -85.3f)
                lineTo(170.7f, 128f)
                curveToRelative(-47.1f, 0f, -85.3f, 38.3f, -85.3f, 85.3f)
                verticalLineToRelative(597.3f)
                curveToRelative(0f, 47.1f, 38.3f, 85.3f, 85.3f, 85.3f)
                close()
                moveTo(170.7f, 810.7f)
                lineTo(170.7f, 298.7f)
                horizontalLineToRelative(682.7f)
                lineToRelative(0f, 512f)
                lineTo(170.7f, 810.7f)
                close()
            }
        }.build()

        return _Window!!
    }

@Suppress("ObjectPropertyName")
private var _Window: ImageVector? = null
