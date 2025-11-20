package com.ciyin.app.ui.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.foundation.thenIf
import ciyin.lang.replace
import com.ciyin.app.data.project.datasource.DataStoreManager
import com.ciyin.app.data.project.model.Game
import com.ciyin.app.ui.component.IconButton2
import com.ciyin.app.ui.theme.border
import com.ciyin.app.ui.theme.iconpack.*
import com.ciyin.app.util.depthCopy
import com.ciyin.app.util.uniqueId
import com.ciyin.app.util.withIncrementName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetMenu(
    title: String,
    state: PresetState,
    modifier: Modifier = Modifier,
    onSelectedChange: (Int) -> Unit = state::onSelectedChange,
    onItemCreateClick: (Game) -> Unit = state::onItemCreateClick,
    onItemEditClick: (Game, String) -> Unit = state::onItemEditClick,
    onItemCopyClick: (Game) -> Unit = state::onItemCopyClick,
    onItemRemoveClick: (Game) -> Unit = state::onItemRemoveClick,
) {

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier.padding(horizontal = 5.dp),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        FilterChip(
            selected = false,
            elevation = null,
            onClick = {},
            shape = MaterialTheme.shapes.border,
            label = {
                Text(
                    modifier = Modifier.widthIn(min = 180.dp),
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )
            },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )

        ExposedDropdownMenu(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ExposedDropdownMenuDefaults.ItemContentPadding)
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(15))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .clickable { onItemCreateClick(state.games.first()) },
                    imageVector = IconPack.Add,
                    contentDescription = null
                )
            }
            state.games.forEachIndexed { index, game ->

                var isEdit by remember { mutableStateOf(false) }
                val focusRequester = remember { FocusRequester() }
                var textState by remember(game.preset) { mutableStateOf(TextFieldValue(game.preset)) }

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                modifier = Modifier
                                    .size(30.dp)
                                    .scale(0.85f)
                                    .padding(end = 10.dp),
                                selected = state.selection == index,
                                onClick = {
                                    onSelectedChange(index)
                                },
                            )
                            BasicTextField(
                                modifier = Modifier
                                    .thenIf(isEdit.not()) { size(1.dp).alpha(0f) }
                                    .focusRequester(focusRequester)
                                    .onFocusChanged {
                                        if (it.isFocused.not()) {
                                            isEdit = false
                                            onItemEditClick(game, textState.text)
                                        }
                                    },
                                value = textState,
                                singleLine = true,
                                onValueChange = { textState = it },
                            )
                            if (isEdit.not()) {
                                Text(
                                    text = textState.text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        Row {
                            IconButton2(
                                icon = IconPack.Edit,
                                enabled = game.isConfig.not(),
                                fraction = 0.8f,
                                onClick = {
                                    if (isEdit) {
                                        return@IconButton2
                                    }
                                    textState = textState.copy(
                                        selection = TextRange(0, textState.text.length)
                                    )
                                    isEdit = true
                                    focusRequester.requestFocus()
                                }
                            )
                            IconButton2(
                                icon = IconPack.Copy,
                                fraction = 0.8f,
                                onClick = { onItemCopyClick(game) }
                            )
                            IconButton2(
                                icon = IconPack.Delete,
                                fraction = 0.8f,
                                onClick = { onItemRemoveClick(game) }
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelectedChange(index)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )

            }

        }
    }

}

@Composable
fun rememberPresetState(
    key: Any,
    games: SnapshotStateList<Game>,
    selection: Int,
    onSelectionChange: (Int) -> Unit,
): PresetState = remember(key) {
    PresetState(
        games,
        selection,
        onSelectionChange,
    )
}

@Composable
fun rememberPresetState(
    games: SnapshotStateList<Game>,
    selection: Int,
    onSelectionChange: (Int) -> Unit,
): PresetState = remember {
    PresetState(
        games,
        selection,
        onSelectionChange,
    )
}

class PresetState(
    val games: SnapshotStateList<Game>,
    selection1: Int,
    private val onSelectionChange: (Int) -> Unit,
) {
    var selection by mutableStateOf(selection1)

    fun onSelectedChange(selection: Int) {
        this.selection = selection
        onSelectionChange(selection)
    }

    fun onItemCreateClick(game: Game) {
        games += game.copy(
            id = games.uniqueId { it.id },
            preset = "新配置".withIncrementName(games) { it.preset },
        )
        onSaveData()
    }

    fun onItemCopyClick(game: Game) {
        games += game.copy(
            id = games.uniqueId { it.id },
            preset = game.preset.withIncrementName(games) { it.preset },
            isConfig = false,
        ).depthCopy<Game>()
        onSaveData()
    }

    fun onItemEditClick(game: Game, text: String) {
        games.replace(game, game.copy(preset = text)) { a, b -> a.id == b.id }
        onSaveData()
    }

    fun onItemRemoveClick(game: Game) {
        if (games.size > 1) {
            if (games[selection] == game) {
                onSelectionChange(0)
                selection = 0
            }
            val index = games.indexOf(game)
            games.removeAt(index)
            if (selection > index) {
                onSelectionChange(selection - 1)
                selection -= 1
            } else if (selection == index) {
                onSelectionChange(0)
                selection = 0
            }
            onSaveData()
        }
    }

    /**
     * 保存数据功能
     *
     * 此函数旨在将当前的游戏数据保存到一个持久化的存储中
     * 它调用了[com.ciyin.app.util.DataStore.writeBacking]方法来执行实际的数据写入操作
     */
    fun onSaveData() {
        DataStoreManager.gameDataStore2.writeBacking()
    }

}