package com.ciyin.app.ui.theme.iconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val IconPack.Edit: ImageVector
    get() {
        if (_Edit != null) {
            return _Edit!!
        }
        _Edit = ImageVector.Builder(
            name = "Edit",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color(0xFF222222))) {
                moveTo(56.9f, 967.1f)
                horizontalLineToRelative(910.2f)
                verticalLineToRelative(-75.8f)
                lineTo(56.9f, 891.3f)
                lineTo(56.9f, 967.1f)
                close()
                moveTo(208.6f, 739.6f)
                lineTo(208.6f, 565.6f)
                lineToRelative(265.4f, -265.5f)
                lineToRelative(174f, 174f)
                lineTo(382.5f, 739.6f)
                lineTo(208.6f, 739.6f)
                close()
                moveTo(625.8f, 148.5f)
                lineToRelative(173.9f, 173.9f)
                lineToRelative(-98.1f, 98.1f)
                lineToRelative(-173.9f, -173.9f)
                lineTo(625.8f, 148.5f)
                close()
                moveTo(880.1f, 349.2f)
                horizontalLineToRelative(0.1f)
                arcToRelative(
                    37.9f,
                    37.9f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    -53.6f
                )
                lineToRelative(-227.6f, -227.6f)
                arcToRelative(
                    37.9f,
                    37.9f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -53.6f,
                    0f
                )
                horizontalLineToRelative(-0.1f)
                lineTo(132.7f, 534.2f)
                verticalLineToRelative(281.1f)
                horizontalLineToRelative(281.2f)
                lineToRelative(466.2f, -466.1f)
                close()
            }
        }.build()

        return _Edit!!
    }

@Suppress("ObjectPropertyName")
private var _Edit: ImageVector? = null
