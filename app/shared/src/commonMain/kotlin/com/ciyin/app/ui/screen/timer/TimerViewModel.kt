package com.ciyin.app.ui.screen.timer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ciyin.lang.replace
import ciyin.lang.unit
import ciyin.platform.platform
import ciyin.system.coroutines.IO
import com.ciyin.app.data.project.datasource.DataStoreManager.gameDataStore2
import com.ciyin.app.data.project.datasource.defaultGames
import com.ciyin.app.data.project.model.Game
import com.ciyin.app.data.project.model.game
import com.ciyin.app.ui.app.dialogError
import com.ciyin.app.ui.component.common.TimeMenuState
import com.ciyin.app.util.DataStore
import com.ciyin.app.util.addAfter
import com.ciyin.app.util.depthCopy
import com.ciyin.app.util.value
import com.ciyin.app.util.withIncrementName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rpa.app.shared.generated.resources.Res
import rpa.app.shared.generated.resources.app_name


/**
 *
 * kotlin类作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2024/10/19 上午4:02
 * @version: 1.0
 */
class TimerViewModel : ViewModel() {

    companion object {
        const val TAG = "TimerViewModel"
    }

    val projects get() = gameDataStore2.data.projects.toMutableStateList()

    val timers get() = gameDataStore2.data.timerTasks.toMutableStateList()

    var selectProject by mutableStateOf(projects.first())

    var isShowCreateTimingTaskDialog by mutableStateOf(false)

    /**
     * 创建一个新定时任务的按钮点击。
     */
    fun onCreateTimingTaskButtonClick() {
        isShowCreateTimingTaskDialog = true
    }

    /**
     * 创建一个新定时任务。
     */
    fun onTimerCreateClick(timer: TimerTask) {
        timers += timer
        isShowCreateTimingTaskDialog = false
        onSaveData()
    }

    /**
     * 复制当前定时任务。
     */
    fun onTimerCopyClick(timer: TimerTask) = runCatching {

        val new = timer.copy(
            id = timers.size,
            name = timer.name.withIncrementName(timers) { it.name },
            timings = timer.timings.toList(),
        ).depthCopy<TimerTask>()

        timers.addAfter(timer, new)

        // 创建或者更新任务程序计划（仅支持Windows）
        if (new.isAddToScheduledTask) {
            viewModelScope.launch(Dispatchers.IO) {
                platform.createScheduledTasksInFolder(
                    taskFolder = Res.string.app_name.value,
                    taskNamePrefix = new.id.toString(),
                    timings = new.timings.map { it.toTaskSchedule() }
                )
            }
        }

        onSaveData()
    }.onFailure {
        it.printStackTrace()
        dialogError("复制任务失败：${it.message}")
    }.unit()

    /**
     * 删除当前定时任务。
     */
    fun onTimerDelClick(timer: TimerTask) = runCatching {
        // 移除任务程序计划（仅支持Windows）
        if (timer.isAddToScheduledTask) {
            viewModelScope.launch(Dispatchers.IO) {
                platform.deleteScheduledTask(Res.string.app_name.value, timer.id.toString())
            }
        }
        timers -= timer
        onSaveData()
    }.onFailure {
        it.printStackTrace()
        dialogError("删除任务失败：${it.message}")
    }.unit()

    /**
     * 检查给定的定时任务ID是否已经存在于当前的定时器列表中，但不包括传入的任务本身。
     *
     * @param project 一个[TimerTask]对象，代表要检查重复性的定时任务。
     * @param value 一个字符串，表示要检查的定时任务ID。
     * @return 如果定时任务ID在其他定时器中存在，则返回true；否则返回false。
     */
    fun onTimerIdIsRepeat(project: TimerTask, value: String): Boolean {
        if (project.id == value.toInt()) return false
        return timers.find { it.id == value.toInt() } != null
    }

    /**
     * 更新当前定时任务的预设名字。
     */
    fun onTimerEditChange(timer: TimerTask, new: TimerTask) = runCatching {


        timers.replace(timer, new) { a, b -> a.id == b.id }

        // 创建或者更新任务程序计划（仅支持Windows）
        viewModelScope.launch(Dispatchers.IO) {
            if (new.isAddToScheduledTask) {

                // 如果id改变了就删除旧任务计划
                if (timer.id != new.id) {
                    platform.deleteScheduledTask(Res.string.app_name.value, timer.id.toString())
                }

                platform.createScheduledTasksInFolder(
                    taskFolder = Res.string.app_name.value,
                    taskNamePrefix = new.id.toString(),
                    timings = new.timings.map { it.toTaskSchedule() }
                )
            } else {
                platform.deleteScheduledTask(Res.string.app_name.value, timer.id.toString())
            }
        }
        onSaveData()

    }.onFailure {
        it.printStackTrace()
        dialogError(it.message)
    }.unit()

    /**
     * 定时任务时间设置
     */
    fun onTimerTimingChange(timer: TimerTask, state: TimeMenuState) {
        /*timer.timing.apply {
            week = state.week
            hour = state.hour.first()
            minute = state.minute.first()
            onSaveData()
        }*/
    }

    fun onTimerPresetSelectionChange(timer: TimerTask, selection: Int) {
        timers.replace(
            timer,
            timer.copy(gameId = timer.games[selection].id)
        ) { a, b -> a.id == b.id }
        onSaveData()
    }

    /**
     * 重置当前定时任务里的游戏配置为默认状态。
     */
    fun onTaskResetClick(timer: TimerTask) {
        val project = timer.project
        val defGame = defaultGames.find { it.type == project.game.type }!!.copy(
            id = project.game.id,
            preset = project.game.preset,
            isConfig = project.game.isConfig
        ).depthCopy<Game>()
//        project.games.replace(project.game, defGame) { a, b -> a.id == b.id }
        onSaveData()
    }

    /**
     * 保存数据功能
     *
     * 此函数旨在将当前的游戏数据保存到一个持久化的存储中
     * 它调用了[DataStore.writeBacking]方法来执行实际的数据写入操作
     */
    fun onSaveData() {
        gameDataStore2.writeBacking()
    }

}