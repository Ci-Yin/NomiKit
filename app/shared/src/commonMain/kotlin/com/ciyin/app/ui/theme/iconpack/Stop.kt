package com.ciyin.app.ui.theme.iconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val IconPack.Stop: ImageVector
    get() {
        if (_Stop != null) {
            return _Stop!!
        }
        _Stop = ImageVector.Builder(
            name = "Stop",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(258.9f, 174.5f)
                horizontalLineToRelative(506.3f)
                curveToRelative(46.6f, 0f, 84.4f, 37.8f, 84.4f, 84.4f)
                verticalLineToRelative(506.3f)
                curveToRelative(0f, 46.6f, -37.8f, 84.4f, -84.4f, 84.4f)
                horizontalLineTo(258.9f)
                curveToRelative(-46.6f, 0f, -84.4f, -37.8f, -84.4f, -84.4f)
                verticalLineTo(258.9f)
                curveToRelative(0f, -46.6f, 37.8f, -84.4f, 84.4f, -84.4f)
                close()
            }
        }.build()

        return _Stop!!
    }

@Suppress("ObjectPropertyName")
private var _Stop: ImageVector? = null
