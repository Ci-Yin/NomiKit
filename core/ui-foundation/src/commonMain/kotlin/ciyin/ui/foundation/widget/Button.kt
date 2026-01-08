package ciyin.ui.foundation.widget


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ciyin.ui.foundation.extension.clickableNoRipple
import ciyin.ui.foundation.extension.thenIf
import org.jetbrains.compose.ui.tooling.preview.Preview


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/9/4 20:21
 * @version: 1.0
 */


val ButtonColors: ButtonColors
    @Composable
    get() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        //disabledContainerColor = MaterialTheme.colorScheme.disabled,
        disabledContentColor = MaterialTheme.colorScheme.onPrimary
    )

val ButtonContentPadding = PaddingValues(vertical = 10.dp, horizontal = 25.dp)
val SmallButtonContentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp)

@Preview
@Composable
private fun ButtonsPreview() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        Button(
            text = "Button",
            onClick = {}
        )

        Button(
            text = "Button",
            enabled = false,
            onClick = {}
        )
        SmallButton(
            content = { Text("Button") },
            onClick = {}
        )
        SmallButton(
            content = { Text("Button") },
            enabled = false,
            onClick = {}
        )
        OutLineTextButton(
            text = "Button",
            onClick = {}
        )
        OutLineTextButton(
            text = "Button",
            enabled = false,
            onClick = {}
        )
    }
}

/**
 * 通用按钮组件，可用于构建具有自定义样式和行为的按钮。
 *
 * @param modifier [Modifier]，用于修饰按钮的布局和行为，默认为[Modifier]。
 * @param enabled `true`表示按钮可用，`false`表示按钮不可用，默认为`true`。
 * @param contentPadding [PaddingValues]，定义按钮内容的内边距，默认为[ButtonContentPadding]。
 * @param shape [Shape]，定义按钮的形状，默认为[MaterialTheme.shapes.mediumSmall]。
 * @param colors [ButtonColors]，定义按钮的颜色样式，默认为[ButtonColors]。
 * @param brush [Brush]?，可选，用于定义按钮的渐变背景。如果设置了`brush`且`enabled`为`true`，将使用`brush`作为背景。
 * @param border [BorderStroke]?，可选，用于定义按钮的边框样式。如果设置了`border`，将使用`border`作为边框样式。
 * @param onClick 按钮点击时触发的回调函数。
 * @param content 按钮的内容，通过[Composable]函数定义。
 */
@Composable
fun Button(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonContentPadding,
    shape: Shape = MaterialTheme.shapes.small,
    colors: ButtonColors = ButtonColors,
    brush: Brush? = null,
    border: BorderStroke? = null,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val color = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    Row(
        modifier = modifier
            .sizeIn(minWidth = 60.dp, minHeight = 24.dp)
            .run {
                if (brush != null && enabled) {
                    background(brush = brush, shape = shape)
                } else {
                    background(color = color, shape = shape)
                }
            }
            .thenIf(border != null) { border(border!!, shape) }
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.titleSmall.copy(color = contentColor),
            LocalContentColor provides contentColor
        ) {
            content()
        }
    }

}

/**
 * 通用按钮组件，提供带有默认样式的按钮。
 *
 * @param text 按钮显示的文本内容。
 * @param modifier [Modifier]，用于修饰按钮的布局和行为，默认为[Modifier]。
 * @param enabled `true`表示按钮可用，`false`表示按钮不可用，默认为`true`。
 * @param contentPadding [PaddingValues]，定义按钮内容的内边距，默认为[ButtonContentPadding]。
 * @param shape [Shape]，定义按钮的形状，默认为[MaterialTheme.shapes.mediumSmall]。
 * @param colors [ButtonColors]，定义按钮的颜色样式，默认为[ButtonColors]。
 * @param brush [Brush]?，可选，用于定义按钮的渐变背景。如果设置了`brush`且`enabled`为`true`，将使用`brush`作为背景。
 * @param border [BorderStroke]?，可选，用于定义按钮的边框样式。如果设置了`border`，将使用`border`作为边框样式。
 * @param onClick 按钮点击时触发的回调函数。
 */
@Composable
fun Button(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonContentPadding,
    shape: Shape = MaterialTheme.shapes.small,
    colors: ButtonColors = ButtonColors,
    brush: Brush? = null,
    border: BorderStroke? = null,
    onClick: () -> Unit,
) = Button(
    modifier = modifier,
    enabled = enabled,
    contentPadding = contentPadding,
    shape = shape,
    colors = colors,
    brush = brush,
    border = border,
    onClick = onClick,
    content = { SingleText(text = text) }
)

/**
 * 小型按钮组件，是对[Button]的封装，用于构建具有自定义样式和行为的小型按钮。
 *
 * @param modifier [Modifier]，用于修饰按钮的布局和行为，默认为[Modifier]。
 * @param shape [Shape]，定义按钮的形状，默认为[CircleShape]。
 * @param enabled 按钮是否可用，`true`表示按钮可用，`false`表示按钮不可用，默认为`true`。
 * @param contentPadding [PaddingValues]，定义按钮内容的内边距，默认为[SmallButtonContentPadding]。
 * @param colors [ButtonColors]，定义按钮的颜色样式，默认为[ButtonColors]。
 * @param brush [Brush]?，可选，用于定义按钮的渐变背景。如果设置了`brush`且`enabled`为`true`，将使用`brush`作为背景。
 * @param onClick 按钮点击时触发的回调函数。
 * @param content 按钮的内容，通过[Composable]函数定义。
 */
@Composable
fun SmallButton(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    contentPadding: PaddingValues = SmallButtonContentPadding,
    colors: ButtonColors = ButtonColors,
    brush: Brush? = null,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) = Button(
    content = content,
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    contentPadding = contentPadding,
    shape = shape,
    colors = colors,
    brush = brush
)

/**
 * 小型按钮组件，是对[SmallButton]的封装，通过文本和点击事件快速定义常用的小型按钮。
 *
 * @param text 按钮上显示的文本内容，由[String]表示。
 * @param modifier [Modifier]，用于修饰按钮的布局和行为，默认为[Modifier]。
 * @param enabled 按钮是否可用，`true`表示按钮可用，`false`表示按钮不可用，默认为`true`。
 * @param contentPadding [PaddingValues]，定义按钮内容的内边距，默认为[SmallButtonContentPadding]。
 * @param shape [Shape]，定义按钮的形状，默认为[MaterialTheme.shapes.mediumSmall]。
 * @param colors [ButtonColors]，定义按钮的颜色样式，默认为[ButtonColors]。
 * @param onClick 按钮点击时触发的回调函数。
 */
@Composable
fun SmallButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = SmallButtonContentPadding,
    shape: Shape = MaterialTheme.shapes.small,
    colors: ButtonColors = ButtonColors,
    onClick: () -> Unit,
) = SmallButton(
    content = {
        SingleText(text = text)
    },
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    contentPadding = contentPadding,
    shape = shape,
    colors = colors
)

@Composable
fun OutLineButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    borderWith: Dp = 1.dp,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clickableNoRipple(enabled = enabled) { onClick() }
            .border(borderWith, color, shape)
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
fun OutLineTextButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    borderWith: Dp = 1.dp,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    style: TextStyle = LocalTextStyle.current,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
) {
    OutLineButton(
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        borderWith = borderWith,
        contentPadding = contentPadding,
        color = color,
        onClick = onClick
    ) {
        SingleText(
            text = text,
            color = color,
            style = style
        )
    }
}