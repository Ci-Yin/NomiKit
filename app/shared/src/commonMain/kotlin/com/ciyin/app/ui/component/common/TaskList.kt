package com.ciyin.app.ui.component.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.lang.numberList
import ciyin.lang.replace
import com.ciyin.app.data.project.datasource.DataStoreManager.gameDataStore2
import com.ciyin.app.data.project.datasource.defaultGames
import com.ciyin.app.data.project.model.Game
import com.ciyin.app.data.project.model.MenuParam
import com.ciyin.app.data.project.model.Task
import com.ciyin.app.data.project.model.TaskChild
import com.ciyin.app.ui.app.dialog
import com.ciyin.app.ui.component.ContentBody
import com.ciyin.app.ui.component.MenuChip
import com.ciyin.app.ui.component.TextButton2
import com.ciyin.app.ui.component.Title
import com.ciyin.app.util.DataStore
import com.ciyin.app.util.value
import org.jetbrains.compose.resources.stringResource
import rpa.app.shared.generated.resources.Res
import rpa.app.shared.generated.resources.task_list_clear_selection
import rpa.app.shared.generated.resources.task_list_reset
import rpa.app.shared.generated.resources.task_list_reset_confirm_message
import rpa.app.shared.generated.resources.task_list_reverse_selection
import rpa.app.shared.generated.resources.task_list_select_all

/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2024/10/22 下午4:20
 * @version: 1.0
 */

@Composable
fun GamePage(
    game: Game,
    state: GamePageState = rememberGamePageState(),
    onSaveData: () -> Unit,

    onTaskResetClick: (Game) -> Unit,
    onTaskAllSelectionClick: (Game) -> Unit = state::onTaskAllSelectionClick,
    onTaskClearSelectClick: (Game) -> Unit = state::onTaskClearSelectClick,
    onTaskReverseSelectionClick: (Game) -> Unit = state::onTaskReverseSelectionClick,

    onTaskItemCheckedChange: (MutableList<Task>, Task, Boolean) -> Unit = state::onTaskItemCheckedChange,
    onTaskItemAllSelectionClick: (MutableList<TaskChild>) -> Unit = state::onTaskItemAllSelectionClick,
    onTaskItemClearSelectClick: (MutableList<TaskChild>) -> Unit = state::onTaskItemClearSelectClick,
    onTaskItemReverseSelectionClick: (MutableList<TaskChild>) -> Unit = state::onTaskItemReverseSelectionClick,
    onTaskItemResetClick: (Int, Game) -> Unit = state::onTaskItemResetClick,

    menuLayout: @Composable () -> Unit = {},
    isLazyColumn: Boolean = true,
    modifier: Modifier = Modifier
) = Column(modifier) {
    Row {
        TextButton2(stringResource(Res.string.task_list_select_all)) { onTaskAllSelectionClick(game) }
        TextButton2(stringResource(Res.string.task_list_clear_selection)) {
            onTaskClearSelectClick(
                game
            )
        }
        TextButton2(stringResource(Res.string.task_list_reverse_selection)) {
            onTaskReverseSelectionClick(
                game
            )
        }
        TextButton2(stringResource(Res.string.task_list_reset)) {
            dialog(message = Res.string.task_list_reset_confirm_message.value) {
                onTaskResetClick(game)
                true
            }
        }
        Spacer(Modifier.weight(1f))
        menuLayout()
    }
    TaskList(
        tasks = game.tasks.toMutableStateList(),
        onSaveData = onSaveData,
        onTaskItemCheckedChange = onTaskItemCheckedChange,
        onTaskItemAllSelectionClick = onTaskItemAllSelectionClick,
        onTaskItemClearSelectClick = onTaskItemClearSelectClick,
        onTaskItemReverseSelectionClick = onTaskItemReverseSelectionClick,
        onTaskItemResetClick = { onTaskItemResetClick(it, game) },
        isLazyColumn = isLazyColumn
    )
}

@Composable
fun TaskList(
    tasks: MutableList<Task>,
    onSaveData: () -> Unit,
    onTaskItemCheckedChange: (MutableList<Task>, Task, Boolean) -> Unit,
    onTaskItemAllSelectionClick: (MutableList<TaskChild>) -> Unit,
    onTaskItemClearSelectClick: (MutableList<TaskChild>) -> Unit,
    onTaskItemReverseSelectionClick: (MutableList<TaskChild>) -> Unit,
    onTaskItemResetClick: (Int) -> Unit,
    isLazyColumn: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (isLazyColumn) {
        LazyColumn(modifier) {
            itemsIndexed(tasks) { index, task ->
                TaskItem(
                    title = task.name,
                    subTitle = task.subName,
                    checked = task.checked,
                    index = index,
                    params = task.params.toMutableStateList(),
                    onCheckedChange = { onTaskItemCheckedChange(tasks, task, it) },
                    onSaveData = onSaveData,
                    onTaskItemAllSelectionClick = onTaskItemAllSelectionClick,
                    onTaskItemClearSelectClick = onTaskItemClearSelectClick,
                    onTaskItemReverseSelectionClick = onTaskItemReverseSelectionClick,
                    onTaskItemResetClick = onTaskItemResetClick
                )
            }
        }
    } else {
        Column(modifier) {
            for ((index, task) in tasks.withIndex()) {
                TaskItem(
                    title = task.name,
                    subTitle = task.subName,
                    checked = task.checked,
                    index = index,
                    params = task.params.toMutableStateList(),
                    onCheckedChange = { onTaskItemCheckedChange(tasks, task, it) },
                    onSaveData = onSaveData,
                    onTaskItemAllSelectionClick = onTaskItemAllSelectionClick,
                    onTaskItemClearSelectClick = onTaskItemClearSelectClick,
                    onTaskItemReverseSelectionClick = onTaskItemReverseSelectionClick,
                    onTaskItemResetClick = onTaskItemResetClick
                )
            }
        }
    }
}


@Composable
private fun TaskItem(
    title: String,
    subTitle: String,
    checked: Boolean,
    index: Int,
    params: MutableList<TaskChild>,
    onCheckedChange: (Boolean) -> Unit,
    onSaveData: () -> Unit,
    onTaskItemAllSelectionClick: (MutableList<TaskChild>) -> Unit,
    onTaskItemClearSelectClick: (MutableList<TaskChild>) -> Unit,
    onTaskItemReverseSelectionClick: (MutableList<TaskChild>) -> Unit,
    onTaskItemResetClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) = Column {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { expanded = !expanded },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Title(
            modifier = Modifier.padding(horizontal = 2.5.dp),
            text = title,
            fontSize = 20.sp
        )
        ContentBody(
            modifier = Modifier.padding(horizontal = 2.5.dp),
            text = subTitle
        )
        Spacer(Modifier.weight(1f))
        Icon(
            modifier = Modifier.rotate(if (expanded) 180f else 0f),
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null
        )
    }
    if (params.isEmpty()) {
        return
    }
    AnimatedVisibility(expanded) {
        Column(Modifier.padding(start = 40.dp)) {
            Row {
                TextButton2(stringResource(Res.string.task_list_select_all)) {
                    onTaskItemAllSelectionClick(params)
                }
                TextButton2(stringResource(Res.string.task_list_clear_selection)) {
                    onTaskItemClearSelectClick(params)
                }
                TextButton2(stringResource(Res.string.task_list_reverse_selection)) {
                    onTaskItemReverseSelectionClick(params)
                }
                TextButton2(stringResource(Res.string.task_list_reset)) {
                    dialog(message = Res.string.task_list_reset_confirm_message.value) {
                        onTaskItemResetClick(index)
                        true
                    }
                }
            }
            for ((index1, taskParam) in params.withIndex()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        modifier = Modifier
                            .size(30.dp)
                            .scale(0.85f),
                        checked = taskParam.checked,
                        onCheckedChange = {
                            params[index1] = taskParam.copy(checked = it)
                            onSaveData()
                        }
                    )
                    Title(
                        modifier = Modifier.padding(start = 5.dp, end = 10.dp),
                        text = taskParam.title,
                        fontSize = 16.sp
                    )
                    if (taskParam.count != null) {
                        Menu2(taskParam.count) {
                            params[index1] =
                                taskParam.copy(count = taskParam.count.copy(selected = it + 1))
                            onSaveData()
                        }
                    }
                    Menu2(taskParam.repeat) {
                        params[index1] =
                            taskParam.copy(repeat = taskParam.repeat.copy(selected = it + 1))
                        onSaveData()
                    }

                }
            }
        }

    }
}

@Composable
private fun Menu2(
    menu: MenuParam,
    onSelectedChange: (Int) -> Unit,
) {
    Menu(
        title = menu.title,
        menuItems = remember(menu.menus) { numberList(1, menu.menus) },
        selectedItemIndex = (menu.selected - 1).coerceAtLeast(0),
        onSelectedChange = onSelectedChange
    )
}

@Composable
private fun Menu(
    title: String,
    menuItems: List<String>,
    selectedItemIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) = Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        modifier = Modifier.padding(horizontal = 5.dp),
        text = title,
        fontSize = 13.sp
    )
    MenuChip(
        //modifier = Modifier.padding(horizontal = 5.dp),
        menuItems = menuItems,
        selectedItemIndex = selectedItemIndex,
        onSelectedChange = onSelectedChange,
        border = null
    )
}

@Composable
fun rememberGamePageState(): GamePageState = rememberSaveable { GamePageState() }

class GamePageState {

    /**
     * 选择当前游戏中的所有任务。
     */
    fun onTaskAllSelectionClick(game: Game) {
        for ((index, task) in game.tasks.withIndex()) {
//            game.tasks[index] = task.copy(checked = true)
        }
        onSaveData()
    }

    /**
     * 取消选择当前游戏中的所有任务。
     */
    fun onTaskClearSelectClick(game: Game) {
        for ((index, task) in game.tasks.withIndex()) {
//            game.tasks[index] = task.copy(checked = false)
        }
        onSaveData()
    }

    /**
     * 反选当前游戏中的所有任务的选择状态。
     */
    fun onTaskReverseSelectionClick(game: Game) {
        for ((index, task) in game.tasks.withIndex()) {
//            game.tasks[index] = task.copy(checked = !task.checked)
        }
        onSaveData()
    }

    /**
     * 更改任务列表中指定任务的选中状态。
     *
     * @param tasks 要操作的任务列表。
     * @param task 要更新的任务。
     * @param checked 新的选中状态。
     */
    fun onTaskItemCheckedChange(tasks: MutableList<Task>, task: Task, checked: Boolean) {
        tasks.replace(task, task.copy(checked = checked)) { a, b -> a.id == b.id }
        onSaveData()
    }

    /**
     * 选择参数列表中的所有任务。
     *
     * @param params 要操作的任务参数列表。
     */
    fun onTaskItemAllSelectionClick(params: MutableList<TaskChild>) {
        for ((index, param) in params.withIndex()) {
            params[index] = param.copy(checked = true)
        }
        onSaveData()
    }

    /**
     * 取消选择参数列表中的所有任务。
     *
     * @param params 要操作的任务参数列表。
     */
    fun onTaskItemClearSelectClick(params: MutableList<TaskChild>) {
        for ((index, param) in params.withIndex()) {
            params[index] = param.copy(checked = false)
        }
        onSaveData()
    }

    /**
     * 反选参数列表中的所有任务的选择状态。
     *
     * @param params 要操作的任务参数列表。
     */
    fun onTaskItemReverseSelectionClick(params: MutableList<TaskChild>) {
        for ((index, param) in params.withIndex()) {
            params[index] = param.copy(checked = !param.checked)
        }
        onSaveData()
    }

    /**
     * 重置指定索引的任务为默认状态。
     *
     * @param index 要重置的任务的索引。
     */
    fun onTaskItemResetClick(index: Int, game: Game) {
        val defGame = defaultGames.find { it.type == game.type }!!
//        game.tasks[index] = defGame.tasks[index].copy(checked = game.tasks[index].checked)
        onSaveData()
    }

    /**
     * 保存数据功能
     *
     * 此函数旨在将当前的游戏数据保存到一个持久化的存储中
     * 它调用了[DataStore.writeBacking]方法来执行实际的数据写入操作
     */
    private fun onSaveData() {
        gameDataStore2.writeBacking()
    }

}