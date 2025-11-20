package com.ciyin.app.ui.theme.iconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val IconPack.Paly: ImageVector
    get() {
        if (_Paly != null) {
            return _Paly!!
        }
        _Paly = ImageVector.Builder(
            name = "Paly",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(847.6f, 381.2f)
                lineTo(267.4f, 17.1f)
                curveTo(193.4f, -28.4f, 102.4f, 22.8f, 102.4f, 108.1f)
                verticalLineToRelative(807.8f)
                curveToRelative(0f, 85.3f, 91f, 136.5f, 165f, 91f)
                lineToRelative(580.3f, -364.1f)
                curveToRelative(96.7f, -62.6f, 96.7f, -204.8f, 0f, -261.7f)
                close()
            }
        }.build()

        return _Paly!!
    }

@Suppress("ObjectPropertyName")
private var _Paly: ImageVector? = null
