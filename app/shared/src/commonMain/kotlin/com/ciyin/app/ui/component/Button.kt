package com.ciyin.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ciyin.app.ui.theme.ScripStop
import com.ciyin.app.ui.theme.border
import com.ciyin.app.ui.theme.iconpack.IconPack
import com.ciyin.app.ui.theme.iconpack.Project


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/21 上午2:57
 */

@Composable
fun ProjectImageButton(
    icon: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) = Box(
    modifier = modifier
        .clip(RoundedCornerShape(15))
        .clickable(enabled, onClick = onClick),
) {
    if (icon.isEmpty()) {
        Image(
            modifier = modifier.padding(7.dp),
            painter = rememberVectorPainter(IconPack.Project),
            contentDescription = null
        )
    } else {
        AsyncImage(
            modifier = modifier.padding(7.dp),
            model = icon,
            contentDescription = null
        )
    }
}


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

@Composable
fun FilledTextButton(
    text: String,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    textStyle: TextStyle = LocalTextStyle.current,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
    ),
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = Button(
    modifier = modifier.sizeIn(minWidth = 110.dp, minHeight = 32.dp),
    shape = MaterialTheme.shapes.border,
    contentPadding = PaddingValues(horizontal = 10.dp),
    colors = colors,
    content = {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            style = textStyle,
        )
    },
    onClick = onClick
)

@Composable
fun StateTextButton(
    data: FilledTextButtonData,
    state: Boolean = true,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    textStyle: TextStyle = LocalTextStyle.current,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = FilledTextButton(
    modifier = modifier,
    colors = ButtonDefaults.buttonColors(
        containerColor = if (state) data.containerColor else data.disabledContainerColor,
        contentColor = if (state) data.textColor else data.disabledTextColor
    ),
    text = if (state) data.text else data.disabledText,
    fontSize = fontSize,
    fontWeight = fontWeight,
    textStyle = textStyle,
    onClick = onClick
)

data class FilledTextButtonData(
    val text: String,
    val disabledText: String,
    val textColor: Color,
    val disabledTextColor: Color,
    val containerColor: Color,
    val disabledContainerColor: Color,
) {
    companion object {

        @Composable
        fun data(
            text: String,
            disabledText: String,
            textColor: Color = Color.Unspecified,
            disabledTextColor: Color = Color.Unspecified,
            containerColor: Color = MaterialTheme.colorScheme.primary,
            disabledContainerColor: Color = ScripStop,
        ) = FilledTextButtonData(
            text = text,
            disabledText = disabledText,
            textColor = textColor,
            disabledTextColor = disabledTextColor,
            containerColor = containerColor,
            disabledContainerColor = disabledContainerColor,
        )
    }
}