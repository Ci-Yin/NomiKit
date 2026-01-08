package ciyin.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.ui.foundation.extension.thenIf
import com.ciyin.app.ui.theme.Content
import com.ciyin.app.ui.theme.iconpack.ArrowRight
import com.ciyin.app.ui.theme.iconpack.IconPack
import org.jetbrains.compose.ui.tooling.preview.AppPreview


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/7/28 下午6:57
 */

val SettingItemBackgroundColor = Color.Transparent
val SettingItemContentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)

@AppPreview
@Composable
private fun SettingPreview2() {
    MaterialTheme {
        Column {
            Text(
                text = "SettingItem",
                //fontSize = 10.sp,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 10.sp)
            )
            SettingItem(
                icon = rememberVectorPainter(Icons.Outlined.Edit),
                title = "标题",
                subTitle = "副标题",
                content = "SettingItemContentPaddingSettingItemContentPadding",
                trailingIcon = rememberVectorPainter(IconPack.ArrowRight),
            )
            SettingScrollChoose(
                title = "标题",
                subTitle = "副标题",
                content = "内容",
                style = SettingDefaults.settingStyle(
                    backgroundColor = Color.Red
                ),
                labels = arrayOf("100", "200", "300", "400"),
                selection = 0,
                onSelectionChange = {}
            )
            SettingSwitch(
                title = "标题",
                subTitle = "副标题",
                content = "内容",
                checked = true,
                onCheckedChange = {}
            )
            SettingRadio(
                title = "标题",
                subTitle = "副标题",
                content = "内容",
                checked = true,
                onCheckedChange = {}
            )
            SettingIconButton(
                title = "标题",
                subTitle = "副标题",
                content = "内容",
                iconButton = Icons.Outlined.Edit,
                //iconButton = Icons.Outlined.Palette,
                onClick = { /*TODO*/ },
                onIconClick = { /*TODO*/ }
            )

        }
    }

}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    title: String = "",
    subTitle: String = "",
    content: String = "",
    trailingIcon: Painter? = null,
    enabled: Boolean = true,
    isWeight: Boolean = true,
    useImageCompose: Boolean = false,
    onClick: (() -> Unit)? = null,
    style: SettingStyle = SettingDefaults.settingStyle(),
    contentPadding: PaddingValues = style.padding,
    trailing: @Composable () -> Unit = {
        if (trailingIcon != null) {
            Icon(
                modifier = Modifier
                    .padding(style.trailingIconPadding)
                    .size(style.trailingIconSize),
                painter = trailingIcon,
                tint = style.trailingIconColor,
                contentDescription = null,
            )
        }
    },
) = Row(
    //horizontalArrangement = Arrangement.Absolute.Center,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
        .thenIf(onClick != null) {
            clickable(enabled = enabled, onClick = onClick!!)
        }
        .background(style.backgroundColor)
        .padding(contentPadding)
) {

    if (icon != null) {
        val iconModifier = Modifier
            .padding(style.iconPadding)
            .size(style.iconSize)
        if (useImageCompose) {
            Image(
                modifier = iconModifier,
                painter = icon,
                contentDescription = null,
            )
        } else {
            Icon(
                modifier = iconModifier,
                painter = icon,
                tint = style.iconColor,
                contentDescription = null,
            )
        }
    }

    if (title.isNotEmpty()) {
        Column {

            Text(
                modifier = Modifier.padding(style.titlePadding),
                text = title,
                maxLines = style.titleMaxLines,
                overflow = TextOverflow.Ellipsis,
                style = style.titleTextStyle,
            )

            if (subTitle.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(style.subTitlePadding),
                    text = subTitle,
                    maxLines = style.subTitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    style = style.subTitleTextStyle,
                )
            }

        }
    }

    Box(Modifier.weight(1.0f, isWeight)) {
        if (content.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(style.contentPadding),
                text = content,
                maxLines = style.contentMaxLines,
                overflow = TextOverflow.Ellipsis,
                style = style.contentTextStyle,
            )
        }
    }

    trailing()

}

@Composable
fun SettingSwitch(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    title: String = "",
    subTitle: String = "",
    content: String = "",
    enabled: Boolean = true,
    isWeight: Boolean = true,
    useImageCompose: Boolean = false,
    contentPadding: PaddingValues = SettingItemContentPadding,
    onClick: (() -> Unit)? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    style: SettingStyle = SettingDefaults.settingStyle(),
) = SettingItem(
    modifier = modifier,
    icon = icon,
    title = title,
    subTitle = subTitle,
    content = content,
    enabled = enabled,
    isWeight = isWeight,
    useImageCompose = useImageCompose,
    contentPadding = contentPadding,
    onClick = onClick,
    style = style,
    trailing = {
        Switch(
            modifier = Modifier.padding(horizontal = 10.dp),
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
        )
    },
)

@Composable
fun SettingRadio(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    title: String = "",
    subTitle: String = "",
    content: String = "",
    enabled: Boolean = true,
    isWeight: Boolean = true,
    useImageCompose: Boolean = false,
    contentPadding: PaddingValues = SettingItemContentPadding,
    onClick: (() -> Unit)? = null,
    checked: Boolean,
    onCheckedChange: () -> Unit,
    style: SettingStyle = SettingDefaults.settingStyle(),
) = SettingItem(
    modifier = modifier,
    icon = icon,
    title = title,
    subTitle = subTitle,
    content = content,
    enabled = enabled,
    isWeight = isWeight,
    useImageCompose = useImageCompose,
    contentPadding = contentPadding,
    onClick = onClick,
    style = style,
    trailing = {
        RadioButton(
            modifier = Modifier.padding(horizontal = 10.dp),
            selected = checked,
            enabled = enabled,
            onClick = onCheckedChange,
        )
    },
)

@Composable
fun SettingCheckbox(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    title: String = "",
    subTitle: String = "",
    content: String = "",
    enabled: Boolean = true,
    isWeight: Boolean = true,
    useImageCompose: Boolean = false,
    contentPadding: PaddingValues = SettingItemContentPadding,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    onClick: (() -> Unit)? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    style: SettingStyle = SettingDefaults.settingStyle(),
) = SettingItem(
    modifier = modifier,
    icon = icon,
    title = title,
    subTitle = subTitle,
    content = content,
    enabled = enabled,
    isWeight = isWeight,
    useImageCompose = useImageCompose,
    contentPadding = contentPadding,
    onClick = onClick,
    style = style,
    trailing = {
        Checkbox(
            modifier = Modifier.padding(horizontal = 10.dp),
            checked = checked,
            enabled = enabled,
            colors = colors,
            onCheckedChange = onCheckedChange,
        )
    },
)

@Composable
fun SettingScrollChoose(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    title: String = "",
    subTitle: String = "",
    content: String = "",
    enabled: Boolean = true,
    isWeight: Boolean = true,
    useImageCompose: Boolean = false,
    contentPadding: PaddingValues = SettingItemContentPadding,
    onClick: (() -> Unit)? = null,
    labels: Array<String>,
    selection: Int = 0,
    onSelectionChange: (selection: Int) -> Unit,
    style: SettingStyle = SettingDefaults.settingStyle(),
) = SettingItem(
    modifier = modifier,
    icon = icon,
    title = title,
    subTitle = subTitle,
    content = content,
    enabled = enabled,
    isWeight = isWeight,
    useImageCompose = useImageCompose,
    contentPadding = contentPadding,
    onClick = onClick,
    style = style,
    trailing = {
        ScrollChoose(
            labels = labels,
            selection = selection,
            onSelectionChange = onSelectionChange
        )
    },
)

@Composable
fun SettingIconButton(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    title: String = "",
    subTitle: String = "",
    content: String = "",
    enabled: Boolean = true,
    isWeight: Boolean = true,
    useImageCompose: Boolean = false,
    iconButton: ImageVector,
    contentPadding: PaddingValues = PaddingValues(vertical = 10.dp),
    onClick: () -> Unit,
    onIconClick: () -> Unit,
    style: SettingStyle = SettingDefaults.settingStyle(),
) = SettingItem(
    modifier = modifier,
    icon = icon,
    title = title,
    subTitle = subTitle,
    content = content,
    enabled = enabled,
    isWeight = isWeight,
    useImageCompose = useImageCompose,
    contentPadding = contentPadding,
    onClick = onClick,
    style = style,
    trailing = {
        OutlinedIconButton(
            modifier = Modifier
                .padding(vertical = 5.dp)
                .size(52.dp, 31.dp),
            enabled = enabled,
            onClick = onIconClick
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = iconButton,
                contentDescription = "Edit"
            )
        }
    },
)


object SettingDefaults {

    val TitleTextStyle: TextStyle
        @Composable
        get() = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.125f,
            fontWeight = FontWeight.Bold
        )

    val ContentTextStyle: TextStyle
        @Composable
        get() = MaterialTheme.typography.bodyMedium.copy(
            color = Content,
            fontSize = MaterialTheme.typography.titleSmall.fontSize,
            fontWeight = FontWeight.Bold
        )

    val SubTitleTextStyle: TextStyle
        @Composable
        get() = MaterialTheme.typography.bodySmall.copy(
            color = Content,
            fontWeight = FontWeight.Bold
        )


    @Composable
    fun settingStyle(

        backgroundColor: Color = SettingItemBackgroundColor,
        padding: PaddingValues = SettingItemContentPadding,

        iconSize: DpSize = DpSize(30.dp, 30.dp),
        iconColor: Color = LocalContentColor.current,
        iconPadding: PaddingValues = PaddingValues(horizontal = 5.dp),

        titleMaxLines: Int = 1,
        titlePadding: PaddingValues = PaddingValues(horizontal = 2.5.dp),
        titleTextStyle: TextStyle = TitleTextStyle,

        subTitleMaxLines: Int = 1,
        subTitlePadding: PaddingValues = PaddingValues(horizontal = 2.5.dp),
        contentTextStyle: TextStyle = ContentTextStyle,

        contentMaxLines: Int = 1,
        contentPadding: PaddingValues = PaddingValues(horizontal = 2.5.dp),
        subTitleTextStyle: TextStyle = SubTitleTextStyle,

        trailingIconSize: DpSize = DpSize(30.dp, 30.dp),
        trailingIconColor: Color = LocalContentColor.current,
        trailingIconPadding: PaddingValues = PaddingValues(horizontal = 5.dp),


        ): SettingStyle = SettingStyle(
        backgroundColor = backgroundColor,
        padding = padding,
        iconSize = iconSize,
        iconColor = iconColor,
        iconPadding = iconPadding,
        titleMaxLines = titleMaxLines,
        titlePadding = titlePadding,
        titleTextStyle = titleTextStyle,
        subTitleMaxLines = subTitleMaxLines,
        subTitlePadding = subTitlePadding,
        subTitleTextStyle = subTitleTextStyle,
        contentMaxLines = contentMaxLines,
        contentPadding = contentPadding,
        contentTextStyle = contentTextStyle,
        trailingIconSize = trailingIconSize,
        trailingIconColor = trailingIconColor,
        trailingIconPadding = trailingIconPadding,
    )

}


@Immutable
data class SettingStyle(

    val backgroundColor: Color,
    val padding: PaddingValues,

    val iconSize: DpSize,
    val iconColor: Color,
    val iconPadding: PaddingValues,

    val titleMaxLines: Int,
    val titlePadding: PaddingValues,
    val titleTextStyle: TextStyle,

    val subTitleMaxLines: Int,
    val subTitlePadding: PaddingValues,
    val subTitleTextStyle: TextStyle,

    val contentMaxLines: Int,
    val contentPadding: PaddingValues,
    val contentTextStyle: TextStyle,

    val trailingIconSize: DpSize,
    val trailingIconColor: Color,
    val trailingIconPadding: PaddingValues,

    )