package com.ciyin.app.ui.theme.iconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val IconPack.Logcat: ImageVector
    get() {
        if (_Logcat != null) {
            return _Logcat!!
        }
        _Logcat = ImageVector.Builder(
            name = "Logcat",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF6C707E)),
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(11.5f, 5.5f)
                lineToRelative(2.146f, -2.146f)
                arcToRelative(
                    0.5f,
                    0.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    0.854f,
                    0.353f
                )
                verticalLineTo(9.5f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3f, 3f)
                horizontalLineToRelative(-3f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3f, -3f)
                verticalLineTo(3.707f)
                arcToRelative(
                    0.5f,
                    0.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    0.854f,
                    -0.353f
                )
                lineTo(8.5f, 5.5f)
                horizontalLineToRelative(3f)
                close()
            }
            path(fill = SolidColor(Color(0xFF6C707E))) {
                moveTo(10.189f, 10.282f)
                arcToRelative(
                    0.25f,
                    0.25f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -0.378f,
                    0f
                )
                lineToRelative(-0.752f, -0.868f)
                arcTo(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.25f, 9f)
                horizontalLineToRelative(1.503f)
                arcToRelative(
                    0.25f,
                    0.25f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    0.189f,
                    0.414f
                )
                lineToRelative(-0.752f, 0.868f)
                close()
            }
            path(fill = SolidColor(Color(0xFF6C707E))) {
                moveTo(7.75f, 7.75f)
                moveToRelative(-0.75f, 0f)
                arcToRelative(
                    0.75f,
                    0.75f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    1.5f,
                    0f
                )
                arcToRelative(
                    0.75f,
                    0.75f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    -1.5f,
                    0f
                )
            }
            path(fill = SolidColor(Color(0xFF6C707E))) {
                moveTo(12.25f, 7.75f)
                moveToRelative(-0.75f, 0f)
                arcToRelative(
                    0.75f,
                    0.75f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    1.5f,
                    0f
                )
                arcToRelative(
                    0.75f,
                    0.75f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    -1.5f,
                    0f
                )
            }
            path(
                stroke = SolidColor(Color(0xFF6C707E)),
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(1.5f, 5.5f)
                horizontalLineToRelative(4f)
                moveTo(2.5f, 8.5f)
                horizontalLineToRelative(3f)
                moveTo(2.5f, 11.5f)
                horizontalLineTo(6f)
            }
        }.build()

        return _Logcat!!
    }

@Suppress("ObjectPropertyName")
private var _Logcat: ImageVector? = null


