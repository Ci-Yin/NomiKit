package com.ciyin.app.ui.screen.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.foundation.viewmodel.viewModel
import com.ciyin.app.data.project.model.GameProject
import com.ciyin.app.ui.app.AppDialog
import com.ciyin.app.ui.app.dialogError
import com.ciyin.app.ui.component.AppPreview
import com.ciyin.app.ui.component.Card
import com.ciyin.app.ui.component.ContentBody
import com.ciyin.app.ui.component.IconButton2
import com.ciyin.app.ui.component.MenuChip
import com.ciyin.app.ui.component.ProjectImageButton
import com.ciyin.app.ui.component.Screen
import com.ciyin.app.ui.component.Title
import com.ciyin.app.ui.component.common.GamePage
import com.ciyin.app.ui.component.common.PresetMenu
import com.ciyin.app.ui.component.common.TimeMenu
import com.ciyin.app.ui.component.common.TimeMenuState
import com.ciyin.app.ui.component.common.Toolbar
import com.ciyin.app.ui.component.common.rememberPresetState
import com.ciyin.app.ui.component.common.rememberTimeMenuState
import com.ciyin.app.ui.theme.iconpack.Add
import com.ciyin.app.ui.theme.iconpack.Copy
import com.ciyin.app.ui.theme.iconpack.Delete
import com.ciyin.app.ui.theme.iconpack.Edit
import com.ciyin.app.ui.theme.iconpack.IconPack
import com.ciyin.app.util.value
import com.ciyin.app.util.withIncrementName
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview
import rpa.app.shared.generated.resources.Res
import rpa.app.shared.generated.resources.timer_create_task_dialog_title
import rpa.app.shared.generated.resources.timer_duplicate_message
import rpa.app.shared.generated.resources.timer_edit_title_dialog
import rpa.app.shared.generated.resources.timer_preset_prefix
import rpa.app.shared.generated.resources.timer_schedule_list
import rpa.app.shared.generated.resources.timer_screen_title
import rpa.app.shared.generated.resources.timer_task_id_error
import rpa.app.shared.generated.resources.timer_task_id_label
import rpa.app.shared.generated.resources.timer_task_title_label


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2024/10/19 上午4:01
 * @version: 1.0
 */


/**
 * 定时器主界面
 * 连接 ViewModel 和 UI 的桥接层
 */
@Composable
fun TimerScreen(
    vm: TimerViewModel = viewModel(::TimerViewModel)
) {
    TimerContent(
        projects = vm.projects,
        timers = vm.timers,
        isShowCreateTimingTaskDialog = vm.isShowCreateTimingTaskDialog,
        onCreateTimingTaskButtonClick = vm::onCreateTimingTaskButtonClick,
        onSaveData = vm::onSaveData,
        onTimerPresetSelectionChange = vm::onTimerPresetSelectionChange,
        onTimerTimingChange = vm::onTimerTimingChange,
        onTimerIdIsRepeat = vm::onTimerIdIsRepeat,
        onTimerEditChange = vm::onTimerEditChange,
        onTimerCopyClick = vm::onTimerCopyClick,
        onTimerDelClick = vm::onTimerDelClick,
        onTaskResetClick = vm::onTaskResetClick,
        onTimerCreateClick = vm::onTimerCreateClick,
        onShowCreateTimingTaskDialog = { vm.isShowCreateTimingTaskDialog = it }
    )
}

/**
 * 定时器内容组件
 * 纯 UI 组件，包含完整的界面布局和交互逻辑
 *
 * @param projects 项目列表
 * @param timers 定时任务列表
 * @param isShowCreateTimingTaskDialog 是否显示创建任务对话框
 * @param onCreateTimingTaskButtonClick 创建任务按钮点击事件
 * @param onSaveData 保存数据事件
 * @param onTimerPresetSelectionChange 预设选择变更事件
 * @param onTimerTimingChange 定时设置变更事件
 * @param onTimerIdIsRepeat ID 重复检查
 * @param onTimerEditChange 编辑任务事件
 * @param onTimerCopyClick 复制任务事件
 * @param onTimerDelClick 删除任务事件
 * @param onTaskResetClick 重置任务事件
 * @param onTimerCreateClick 创建任务事件
 * @param onShowCreateTimingTaskDialog 控制创建对话框显示
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimerContent(
    projects: SnapshotStateList<GameProject>,
    timers: SnapshotStateList<TimerTask>,
    isShowCreateTimingTaskDialog: Boolean,
    onCreateTimingTaskButtonClick: () -> Unit,
    onSaveData: () -> Unit,
    onTimerPresetSelectionChange: (TimerTask, Int) -> Unit,
    onTimerTimingChange: (TimerTask, TimeMenuState) -> Unit,
    onTimerIdIsRepeat: (TimerTask, String) -> Boolean,
    onTimerEditChange: (TimerTask, TimerTask) -> Unit,
    onTimerCopyClick: (TimerTask) -> Unit,
    onTimerDelClick: (TimerTask) -> Unit,
    onTaskResetClick: (TimerTask) -> Unit,
    onTimerCreateClick: (TimerTask) -> Unit,
    onShowCreateTimingTaskDialog: (Boolean) -> Unit
) = Screen(
    title = stringResource(Res.string.timer_screen_title),
    toolbar = {
        // 顶部工具栏
        Toolbar(
            stringResource(Res.string.timer_create_task_dialog_title),
            onCreateTimingTaskButtonClick
        )
    }
) {
    Box {
        // 定时任务列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(timers, { it.id }) { timer ->
                GameItem(
                    modifier = Modifier.animateItem(),
                    projects = projects,
                    timer = timer,
                    onSaveData = onSaveData,
                    onTimerPresetSelectionChange = onTimerPresetSelectionChange,
                    onTimerTimingChange = onTimerTimingChange,
                    onTimerIdIsRepeat = onTimerIdIsRepeat,
                    onTimerEditChange = onTimerEditChange,
                    onTimerCopyClick = onTimerCopyClick,
                    onTimerDelClick = onTimerDelClick,
                    onTaskResetClick = onTaskResetClick,
                )
            }
        }

        // 悬浮添加按钮
        FloatingActionButton(
            modifier = Modifier
                .padding(50.dp)
                .align(Alignment.BottomEnd),
            onClick = onCreateTimingTaskButtonClick,
            content = {
                Icon(
                    modifier = Modifier
                        .padding(14.dp)
                        .size(36.dp),
                    imageVector = IconPack.Add,
                    contentDescription = null
                )
            }
        )

        // 创建任务对话框
        CreateTimingTaskDialog(
            projects = projects,
            timers = timers,
            isShowCreateTimingTaskDialog = isShowCreateTimingTaskDialog,
            onShowCreateTimingTaskDialog = onShowCreateTimingTaskDialog,
            onTimerIdIsRepeat = onTimerIdIsRepeat,
            onTimerCreateClick = onTimerCreateClick
        )
    }
}

/**
 * 游戏定时任务项
 * 显示单个定时任务的卡片，包含展开/收起功能
 *
 * @param projects 项目列表
 * @param timer 定时任务数据
 * @param modifier 修饰符
 */
@Composable
private fun GameItem(
    projects: SnapshotStateList<GameProject>,
    timer: TimerTask,
    onSaveData: () -> Unit,

    onTimerPresetSelectionChange: (TimerTask, Int) -> Unit,
    onTimerTimingChange: (TimerTask, TimeMenuState) -> Unit,

    onTimerIdIsRepeat: (TimerTask, String) -> Boolean,
    onTimerEditChange: (TimerTask, TimerTask) -> Unit,
    onTimerCopyClick: (TimerTask) -> Unit,
    onTimerDelClick: (TimerTask) -> Unit,

    onTaskResetClick: (TimerTask) -> Unit,

    modifier: Modifier = Modifier
) = Card(
    modifier = modifier,
    contentPaddings = PaddingValues(0.dp)
) {

    // 展开/收起状态
    var expanded by rememberSaveable { mutableStateOf(false) }
    // 编辑对话框显示状态
    var isShowDialog by remember { mutableStateOf(false) }

    // 编辑对话框
    TimerEditDialog(
        timer = timer,
        projects = projects,
        isShowDialog = isShowDialog,
        onShowDialog = { isShowDialog = it },
        onTimerIdIsRepeat = onTimerIdIsRepeat,
        onTimerEditChange = onTimerEditChange,
    )

    // 任务头部信息行
    Row(
        modifier = Modifier
            .clip(CardDefaults.outlinedShape)
            .clickable { expanded = !expanded }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 项目图标按钮
        ProjectImageButton(
            modifier = Modifier.size(45.dp),
            icon = timer.icon
        ) {

        }

        Spacer(Modifier.width(15.dp))

        // 任务名称和游戏信息
        Column(Modifier.padding(horizontal = 5.dp)) {
            Title(
                text = timer.name,
                fontSize = 14.sp
            )
            ContentBody(
                text = timer.game.name,
                fontSize = 12.sp,
            )
        }

        Spacer(Modifier.weight(1f))

        // 预设选择菜单
        Box(modifier = Modifier.padding(horizontal = 5.dp)) {
            val key1 = timer.games.sumOf { it.preset.hashCode() }
            MenuChip(
                modifier = Modifier.sizeIn(minWidth = 110.dp, minHeight = 32.dp),
                menuItems = remember(key1) { timer.games.map { it.preset } },
                selectedItemIndex = remember(key1, timer.game) { timer.selection },
                onSelectedChange = { onTimerPresetSelectionChange(timer, it) }
            )
        }

        // 编辑按钮
        IconButton2(IconPack.Edit) {
            isShowDialog = true
        }
        // 复制按钮
        IconButton2(IconPack.Copy) {
            onTimerCopyClick(timer)
        }
        // 删除按钮
        IconButton2(IconPack.Delete) {
            onTimerDelClick(timer)
        }
        // 展开/收起图标
        Icon(
            modifier = Modifier.rotate(if (expanded) 180f else 0f),
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null
        )
    }

    // 展开的详细内容
    AnimatedVisibility(expanded) {
        GamePage(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .padding(horizontal = 10.dp),
            game = timer.game,
            isLazyColumn = false,
            onSaveData = onSaveData,
            onTaskResetClick = { onTaskResetClick(timer) },
            menuLayout = {
                // 预设菜单
                PresetMenu(
                    title = "${stringResource(Res.string.timer_preset_prefix)}${timer.game.preset}",
                    state = rememberPresetState(
                        games = timer.games.toMutableStateList(),
                        selection = timer.selection,
                        onSelectionChange = {
                            onTimerPresetSelectionChange(timer, it)
                        },
                    )
                )
            }
        )
    }

}

/**
 * 定时任务编辑对话框
 * 用于编辑任务的 ID、名称、项目、配置和定时安排
 *
 * @param projects 项目列表
 * @param timer 要编辑的定时任务
 * @param isShowDialog 是否显示对话框
 * @param onShowDialog 控制对话框显示回调
 * @param onTimerIdIsRepeat ID 重复检查回调
 * @param onTimerEditChange 编辑完成回调
 */
@Composable
private fun TimerEditDialog(
    projects: SnapshotStateList<GameProject>,
    timer: TimerTask,
    isShowDialog: Boolean,
    onShowDialog: (Boolean) -> Unit,
    onTimerIdIsRepeat: (TimerTask, String) -> Boolean,
    onTimerEditChange: (TimerTask, TimerTask) -> Unit,
) {

    if (!isShowDialog) return

    // 任务状态副本
    var timerState by remember(timer) { mutableStateOf(timer) }
    // 任务 ID
    var id by remember(timer.id) { mutableStateOf(timer.id.toString()) }
    // 定时安排列表
    val timings = timer.timings.toMutableStateList()
    // ID 错误状态
    var isIdError by remember { mutableStateOf(false) }

    AppDialog(
        title = stringResource(Res.string.timer_edit_title_dialog),
        onDismissRequest = { onShowDialog(false) },
        onDismissClick = { onShowDialog(false) },
        onConfirmClick = {
            if (isIdError) {
                dialogError(Res.string.timer_task_id_error.value)
                return@AppDialog
            }
            onTimerEditChange(timer, timerState.copy(id = id.toInt(), timings = timings.toList()))
            onShowDialog(false)
        }
    ) {
        Column(
            modifier = Modifier.width(500.dp).verticalScroll(rememberScrollState()),
        ) {
            // 任务 ID 输入框
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(Res.string.timer_task_id_label)) },
                value = id,
                isError = isIdError,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                onValueChange = {
                    id = it
                    if (it.isEmpty()) {
                        isIdError = true
                        return@OutlinedTextField
                    }
                    isIdError = onTimerIdIsRepeat(timer, it)
                },
            )
            // 任务名称输入框
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(Res.string.timer_task_title_label)) },
                value = timerState.name,
                onValueChange = {
                    timerState = timerState.copy(name = it)
                },
            )

            Title(
                modifier = Modifier.padding(vertical = 5.dp),
                text = "选择项目和配置",
                fontSize = 14.sp
            )
            // 项目和配置选择行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 项目选择菜单
                MenuChip(
                    menuItems = remember { projects.map { it.name } },
                    selectedItemIndex = remember(
                        projects.size,
                        timerState.project
                    ) { projects.indexOf(timerState.project) },
                    onSelectedChange = {
                        timerState = timerState.copy(
                            projectId = projects[it].id,
                            projectType = projects[it].type,
                            gameId = projects[it].games[0].id,
                            gameType = projects[it].games[0].type
                        )
                    }
                )
                Spacer(Modifier.width(15.dp))
                // 配置选择菜单
                val games = timerState.project.games
                MenuChip(
                    menuItems = remember(timerState.project) { games.map { it.preset } },
                    selectedItemIndex = remember(
                        timerState.project,
                        timerState.game
                    ) { games.indexOf(timerState.game) },
                    onSelectedChange = {
                        timerState = timerState.copy(
                            gameId = games[it].id,
                            gameType = games[it].type
                        )
                    }
                )
            }

            // 计划任务列表标题行
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Title(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.timer_schedule_list),
                    fontSize = 14.sp
                )

                // 添加到计划任务复选框
                Checkbox(
                    checked = timerState.isAddToScheduledTask,
                    onCheckedChange = { timerState = timerState.copy(isAddToScheduledTask = it) }
                )
                ContentBody("添加到计划任务里")
                Spacer(Modifier.width(10.dp))

                // 添加定时安排按钮
                Icon(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(15))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .clickable {
                            timings += Timing().createUnique(timings)
                        },
                    imageVector = IconPack.Add,
                    contentDescription = null
                )
            }

            // 定时安排列表
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
                    .height(300.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        RoundedCornerShape(10.dp)
                    ),
                contentPadding = PaddingValues(5.dp)
            ) {
                itemsIndexed(timings, { index, timing -> timing.hashCode() }) { index, timing ->
                    Row(
                        modifier = Modifier.animateItem(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 时间选择菜单
                        Box(modifier = Modifier.weight(1f)) {
                            TimeMenu(
                                modifier = Modifier.fillMaxWidth(),
                                state = rememberTimeMenuState(
                                    initialWeek = timing.week,
                                    initialHour = mutableSetOf(timing.hour),
                                    initialMinute = mutableSetOf(timing.minute),
                                    isWeekMultipleSelected = true
                                ),
                                onSelectedChange = {
                                    val newTiming = timing.copyUnique(
                                        timings = timings,
                                        week = it.week,
                                        hour = it.hour.first(),
                                        minute = it.minute.first()
                                    )
                                    if (timing.isEqual(newTiming)) {
                                        dialogError(Res.string.timer_duplicate_message.value)
                                    }
                                    timings[index] = newTiming.copyUnique(timings)
                                }
                            )
                        }
                        // 复制定时安排按钮
                        IconButton2(IconPack.Copy) {
                            timings.add(index + 1, timing.copyUnique(timings))
                        }
                        // 删除定时安排按钮
                        IconButton2(IconPack.Delete) {
                            timings.remove(timing)
                        }
                    }
                }
            }

        }
    }
}

/**
 * 创建定时任务对话框
 * 复用 TimerEditDialog 实现创建新任务的功能
 *
 * @param projects 项目列表
 * @param timers 现有定时任务列表
 * @param isShowCreateTimingTaskDialog 是否显示对话框
 * @param onShowCreateTimingTaskDialog 控制对话框显示回调
 * @param onTimerIdIsRepeat ID 重复检查回调
 * @param onTimerCreateClick 创建任务完成回调
 */
@Composable
private fun CreateTimingTaskDialog(
    projects: SnapshotStateList<GameProject>,
    timers: SnapshotStateList<TimerTask>,
    isShowCreateTimingTaskDialog: Boolean,
    onShowCreateTimingTaskDialog: (Boolean) -> Unit,
    onTimerIdIsRepeat: (TimerTask, String) -> Boolean,
    onTimerCreateClick: (TimerTask) -> Unit
) {
    TimerEditDialog(
        projects = projects,
        timer = TimerTask(
            id = timers.size,
            name = Res.string.timer_create_task_dialog_title.value.withIncrementName(timers) { it.name },
            projectId = 0,
            projectType = 0,
            gameId = 0,
            gameType = 0,
        ),
        isShowDialog = isShowCreateTimingTaskDialog,
        onShowDialog = onShowCreateTimingTaskDialog,
        onTimerIdIsRepeat = onTimerIdIsRepeat,
        onTimerEditChange = { _, new -> onTimerCreateClick(new) },
    )
}


/**
 * 定时器界面预览
 * 用于 Compose Preview，包含测试数据
 */
@AppPreview
@Composable
private fun TimerScreenPreview() = AppPreview {
    // 测试项目列表
    val projects = remember {
        mutableListOf<GameProject>().apply {
            // 添加测试项目数据
        }
    }
    // 测试定时任务列表
    val timers = remember {
        mutableListOf<TimerTask>().apply {
            for (i in 0..10) {
                add(
                    TimerTask(
                        id = i,
                        name = "任务$i",
                        projectId = 1,
                        projectType = 1,
                        gameType = 1,
                        gameId = 1,
                    )
                )
            }
        }
    }

    TimerContent(
        projects = projects.toMutableStateList(),
        timers = timers.toMutableStateList(),
        isShowCreateTimingTaskDialog = false,
        onCreateTimingTaskButtonClick = {},
        onSaveData = {},
        onTimerPresetSelectionChange = { _, _ -> },
        onTimerTimingChange = { _, _ -> },
        onTimerIdIsRepeat = { _, _ -> false },
        onTimerEditChange = { _, _ -> },
        onTimerCopyClick = {},
        onTimerDelClick = {},
        onTaskResetClick = {},
        onTimerCreateClick = {},
        onShowCreateTimingTaskDialog = {}
    )
}

