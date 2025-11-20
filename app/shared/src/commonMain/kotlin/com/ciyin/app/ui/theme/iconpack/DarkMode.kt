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

val IconPack.DarkMode: ImageVector
    get() {
        if (_DarkMode2 != null) {
            return _DarkMode2!!
        }
        _DarkMode2 = ImageVector.Builder(
            name = "DarkMode2",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(428f, 181.1f)
                quadTo(426.7f, 197.1f, 426.7f, 213.3f)
                arcToRelative(
                    384.4f,
                    384.4f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    384f,
                    384f
                )
                quadToRelative(16.2f, 0f, 32.2f, -1.3f)
                arcToRelative(
                    341.3f,
                    341.3f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    -414.9f,
                    -414.9f
                )
                moveTo(512f, 85.3f)
                arcToRelative(
                    426.7f,
                    426.7f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    426.7f,
                    426.7f
                )
                curveToRelative(0f, -9.5f, -0.4f, -18.9f, -1f, -28.3f)
                arcToRelative(
                    298.7f,
                    298.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -397.3f,
                    -397.3f
                )
                arcTo(
                    445.4f,
                    445.4f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    512f,
                    85.3f
                )
                close()
            }
        }.build()

        return _DarkMode2!!
    }

@Suppress("ObjectPropertyName")
private var _DarkMode2: ImageVector? = null

@Preview
@Composable
private fun DarkMode2Preview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.DarkMode, contentDescription = null)
    }
}
