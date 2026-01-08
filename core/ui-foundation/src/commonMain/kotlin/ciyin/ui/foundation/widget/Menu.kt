package ciyin.ui.foundation.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.AppPreview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuChip(
    label: String,
    menuItems: List<String>,
    selectedItemIndex: Int,
    onSelectedItemIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable ((Boolean) -> Unit)? = {
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = it)
    },
    shape: Shape = FilterChipDefaults.shape,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(),
    border: BorderStroke? = FilterChipDefaults.filterChipBorder(enabled, false),
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        FilterChip(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            elevation = null,
            selected = false,
            enabled = enabled,
            onClick = {},
            label = {
                Text(text = "$label：${menuItems[selectedItemIndex]}")
            },
            leadingIcon = leadingIcon,
            trailingIcon = { trailingIcon?.invoke(expanded) },
            colors = colors,
            shape = shape,
            border = border,
        )
        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false },
            matchTextFieldWidth = false
        ) {
            menuItems.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        expanded = false
                        onSelectedItemIndexChange(index)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

/**
 * 包含 Exposed Dropdown Menu 内容的弹出窗口。应该用在
 * [ExposedDropdownMenuBox] 的 MenuBox。
 *
 * @param expanded 菜单是否展开
 * @param onDismissRequest 当用户请求关闭菜单时（例如，点按超出菜单范围
 * @param modifier 修改要应用于此菜单的 [Modifier]
 * @param scrollState：菜单内容用于项目垂直滚动的 [ScrollState]
 * @param matchTextFieldWidth 是否应强制约束菜单的宽度以匹配它所附加到的文本字段的宽度。
 * @param shape 菜单的形状
 * @param containerColor 菜单的容器颜色
 * @param tonalElevation 当 [containerColor] 为 [ColorScheme.surface] 时，@param tonalElevation，半透明主色
 * 颜色叠加应用于容器顶部。较高的色调提升值将
 * 导致浅色主题中的颜色较暗，而深色主题中的颜色较亮。另请参阅：[Surface]。
 * @param shadowElevation 菜单下方阴影的标高
 * @param border 以在菜单的容器周围绘制。传递 'null' 表示无边框。
 * @param content 菜单内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    modifier2: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    matchTextFieldWidth: Boolean = false,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.background,
    tonalElevation: Dp = MenuDefaults.TonalElevation,
    shadowElevation: Dp = MenuDefaults.ShadowElevation,
    border: BorderStroke? = null,
    buttonContent: @Composable ExposedDropdownMenuBoxScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        buttonContent()
        Box(modifier = modifier2) {
            ExposedDropdownMenu(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                expanded = expanded,
                onDismissRequest = onDismissRequest,
                matchTextFieldWidth = matchTextFieldWidth,
                scrollState = scrollState,
                shape = shape,
                containerColor = containerColor,
                tonalElevation = tonalElevation,
                shadowElevation = shadowElevation,
                border = border,
                content = content
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <V : Any> MenuText(
    menuItems: List<V>,
    selectedItemIndex: Int,
    onSelectedItemIndexChange: (Int) -> Unit,
    labelText: String,
    itemText: @Composable (V) -> String,
    modifier: Modifier = Modifier,
    label: @Composable ExposedDropdownMenuBoxScope.() -> Unit = {
        Text(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            text = labelText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    },
) {

    var expanded by remember { mutableStateOf(false) }

    ExposedMenu(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        onDismissRequest = { expanded = false },
        buttonContent = label
    ) {
        menuItems.forEachIndexed { index, item ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = itemText(item),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                onClick = {
                    expanded = false
                    onSelectedItemIndexChange(index)
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )
        }
    }

}

@AppPreview
@Composable
private fun MenuChipPreview() = MaterialPreview {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        MenuChip(
            label = "测试",
            menuItems = listOf("选项1", "选项2", "选项3"),
            selectedItemIndex = 0,
            onSelectedItemIndexChange = {

            }
        )
    }
}