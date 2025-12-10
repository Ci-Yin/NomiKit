package com.ciyin.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun IconButton2(
    image: Painter,
    modifier: Modifier = Modifier,
    fraction: Float = 1f,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors().copy(),
    onClick: () -> Unit,
) = IconButton(
    image = image,
    onClick = onClick,
    modifier = modifier,
    fraction = fraction,
    enabled = enabled,
    colors = colors
)


@Composable
fun IconButton2(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    fraction: Float = 1f,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    onClick: () -> Unit,
) = IconButton(
    icon = icon,
    onClick = onClick,
    modifier = modifier,
    fraction = fraction,
    enabled = enabled,
    colors = colors
)

@Composable
private fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: Painter? = null,
    icon: ImageVector? = null,
    fraction: Float = 1f,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
) = IconButton(
    modifier = modifier.size(35.dp * fraction),
    colors = colors,
    enabled = enabled,
    onClick = onClick,
) {
    if (image != null) {
        Image(
            image, null, Modifier.size(24.dp * fraction),
            colorFilter = ColorFilter.tint(colors.contentColor)
        )
    }
    if (icon != null) {
        Icon(icon, null, Modifier.size(24.dp * fraction))
    }
}

@Composable
fun OutlinedIconButton2(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: Painter? = null,
    icon: ImageVector? = null,
    fraction: Float = 1f,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
) = OutlinedIconButton(
    modifier = modifier.size(35.dp * fraction),
    colors = colors,
    enabled = enabled,
    onClick = onClick,
) {
    if (image != null) {
        Image(
            image, null, Modifier.size(24.dp * fraction),
            colorFilter = ColorFilter.tint(colors.contentColor)
        )
    }
    if (icon != null) {
        Icon(icon, null, Modifier.size(24.dp * fraction))
    }
}

@Composable
fun Button2(text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier.padding(10.dp),
        onClick = onClick,
        content = {
            Text(text = text)
        }
    )
}

@Composable
fun TextButton2(text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier.padding(horizontal = 5.dp),
        elevation = null,
        contentPadding = PaddingValues(horizontal = 10.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            containerColor = Color.Transparent
        ),
        onClick = onClick,
        content = {
            Text(text = text)
        }
    )
}

