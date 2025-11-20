package com.ciyin.app.ui.theme.iconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val IconPack.Copy: ImageVector
    get() {
        if (_Copy != null) {
            return _Copy!!
        }
        _Copy = ImageVector.Builder(
            name = "Copy",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color(0xFF0C0C0C))) {
                moveTo(602.6f, 764.5f)
                curveToRelative(92.9f, 0f, 168.5f, -75.6f, 168.5f, -168.5f)
                verticalLineTo(286.7f)
                curveToRelative(0f, -92.9f, -75.6f, -168.5f, -168.5f, -168.5f)
                horizontalLineToRelative(-321f)
                curveToRelative(-92.9f, 0f, -168.5f, 75.6f, -168.5f, 168.5f)
                verticalLineTo(596f)
                curveToRelative(0f, 92.9f, 75.6f, 168.5f, 168.5f, 168.5f)
                horizontalLineToRelative(321f)
                close()
                moveTo(209.2f, 596f)
                verticalLineTo(286.7f)
                curveToRelative(0f, -39.9f, 32.5f, -72.4f, 72.4f, -72.4f)
                horizontalLineToRelative(321f)
                curveToRelative(39.9f, 0f, 72.4f, 32.5f, 72.4f, 72.4f)
                verticalLineTo(596f)
                curveToRelative(0f, 39.9f, -32.5f, 72.4f, -72.4f, 72.4f)
                horizontalLineToRelative(-321f)
                curveToRelative(-39.9f, 0f, -72.4f, -32.5f, -72.4f, -72.4f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0C0C0C))) {
                moveTo(573f, 592.5f)
                curveToRelative(32.5f, 0f, 58.8f, -26.3f, 58.8f, -58.8f)
                reflectiveCurveToRelative(-26.3f, -58.8f, -58.8f, -58.8f)
                horizontalLineTo(305.9f)
                curveToRelative(-32.5f, 0f, -58.8f, 26.3f, -58.8f, 58.8f)
                reflectiveCurveToRelative(26.3f, 58.8f, 58.8f, 58.8f)
                horizontalLineTo(573f)
                close()
                moveTo(305.9f, 408.2f)
                horizontalLineTo(573f)
                curveToRelative(32.5f, 0f, 58.8f, -26.3f, 58.8f, -58.8f)
                reflectiveCurveToRelative(-26.3f, -58.8f, -58.8f, -58.8f)
                horizontalLineTo(305.9f)
                curveToRelative(-32.5f, 0f, -58.8f, 26.3f, -58.8f, 58.8f)
                reflectiveCurveToRelative(26.3f, 58.8f, 58.8f, 58.8f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0C0C0C))) {
                moveTo(818.8f, 278.2f)
                verticalLineTo(739.7f)
                curveToRelative(0f, 39.9f, -32.5f, 72.4f, -72.4f, 72.4f)
                horizontalLineTo(273.3f)
                curveToRelative(27.1f, 56.8f, 85.1f, 96.2f, 152.2f, 96.2f)
                horizontalLineToRelative(321f)
                curveToRelative(92.9f, 0f, 168.5f, -75.6f, 168.5f, -168.5f)
                verticalLineTo(430.4f)
                curveToRelative(0f, -67.1f, -39.4f, -125.1f, -96.2f, -152.2f)
                close()
            }
        }.build()

        return _Copy!!
    }

@Suppress("ObjectPropertyName")
private var _Copy: ImageVector? = null
