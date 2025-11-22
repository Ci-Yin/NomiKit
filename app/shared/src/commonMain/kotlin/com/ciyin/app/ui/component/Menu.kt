package com.ciyin.app.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.ciyin.app.ui.theme.border
import org.jetbrains.compose.ui.tooling.preview.Preview


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/8/27 上午2:23
 */
@Preview
@Composable
private fun MenuChipPreview() {
    MaterialTheme {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            MenuChip(
                menuItems = listOf("选项1", "选项2", "选项3"),
                selectedItemIndex = 0,
                onSelectedChange = {

                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuChip(
    menuItems: List<String>,
    selectedItemIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.border,
    border: BorderStroke? = FilterChipDefaults.filterChipBorder(enabled, false),
) {
    var expanded by remember { mutableStateOf(false) }
    var label by remember(selectedItemIndex) { mutableStateOf(menuItems[selectedItemIndex]) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        FilterChip(
            modifier = modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).sizeIn(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = shape,
            border = border,
            elevation = null,
            selected = false,
            onClick = {},
            label = {
                Text(
                    text = label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { expanded = false },
        ) {
            menuItems.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        expanded = false
                        onSelectedChange(index)
                        label = item
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
    buttonContent: @Composable ExposedDropdownMenuBoxScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        Box(Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)) {
            buttonContent()
        }
        Box(modifier = modifier2) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest,
                scrollState = scrollState,
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


