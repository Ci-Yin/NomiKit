package com.ciyin.app.ui.screen.timer

import ciyin.lang.findIndex
import com.ciyin.app.data.project.datasource.DataStoreManager.gameDataStore2
import com.ciyin.app.data.project.model.Game
import com.ciyin.app.data.project.model.GameProject
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * [TimerTask] 代表一个定时任务，包含了任务的基本信息及其时间配置。
 *
 * @param id 任务的唯一标识符。
 * @param name 任务名称。
 * @param projectId 所属项目的ID。
 * @param projectType 项目类型，以整数表示。
 * @param gameId 游戏ID，如果此任务与特定游戏相关联。
 * @param gameType 游戏类型，用整数区分不同的游戏种类。
 * @param isAddToScheduledTask 是否将此任务添加到计划任务中，默认为`false`。
 * @param icon 任务图标URL或路径，默认为空字符串。
 * @param timings 定时任务的时间配置列表，每个元素都是一个[Timing]对象，描述了任务执行的具体时间安排。默认为空列表。
 */
@Serializable
data class TimerTask(
    val id: Int,
    val name: String,
    val projectId: Int,
    val projectType: Int,
    val gameId: Int,
    val gameType: Int,
    val isAddToScheduledTask: Boolean = false,
    val icon: String = "",
    val timings: List<Timing> = listOf(),
)

/**
 * 用于表示定时任务的时间配置。
 *
 * 此类包含三个属性：week, hour, minute，分别代表周几、小时和分钟。
 * week是一个可变集合，默认包含1到7，表示一周中的每一天。
 * hour和minute都是整数类型，分别表示一天中的小时（0-23）和分钟（0-59）。
 *
 * @property week 一周中的哪几天，使用1到7的整数表示周一到周日。
 * @property hour 定时任务执行的具体小时，范围是0到23。
 * @property minute 定时任务执行的具体分钟，范围是0到59。
 */
@Serializable
data class Timing(
    var week: MutableSet<Int> = mutableSetOf(1, 2, 3, 4, 5, 6, 7),
    var hour: Int = 0,
    var minute: Int = 0,
)

/**
 * 获取当前定时任务所属的游戏项目。
 * 如果找不到对应ID的游戏项目，则抛出错误。
 *
 * @return 返回与定时任务关联的[GameProject]对象。
 * @throws IllegalStateException 如果找不到与[projectId]匹配的游戏项目。
 */
val TimerTask.project: GameProject
    get() {
        return gameDataStore2.data.projects.find { it.id == projectId }
            ?: error("未找到游戏项目，id: $projectId")
    }

/**
 * 获取当前项目中的游戏列表。
 *
 * 该属性返回一个包含所有游戏的快照状态列表。列表中的每个元素都是一个 `Game` 对象，代表一个单独的游戏。
 * 通过这个列表，可以访问到游戏中定义的所有信息和状态。
 *
 */
val TimerTask.games: List<Game> get() = project.games

/**
 * 获取与当前任务关联的游戏配置。
 * 如果找不到对应的游戏配置，将抛出异常。
 *
 * @return 返回与任务关联的游戏配置对象。
 * @throws IllegalStateException 如果没有找到与给定ID匹配的游戏配置。
 */
val TimerTask.game: Game
    get() = games.find { game -> game.id == gameId } ?: error("未找到游戏对于的配置")

/**
 * 获取当前游戏在游戏列表中的索引位置。
 *
 * 该属性通过查找与当前游戏ID匹配的游戏来确定其在游戏列表中的位置。如果找到匹配项，则返回该游戏的索引；如果没有找到匹配项，则可能返回-1或抛出异常，具体行为取决于`findIndex`方法的实现细节。
 */
val TimerTask.selection: Int get() = games.findIndex { it.id == game.id }

/**
 * 检查当前时间是否与指定的[Timing]对象中的星期、小时和分钟匹配。
 *
 * @return 如果当前时间与指定的星期、小时和分钟都匹配，则返回`true`；否则返回`false`。
 */
@OptIn(ExperimentalTime::class)
fun Timing.isCurTime(): Boolean {
    val now = Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    // 检查星期、小时、分钟是否都匹配
    return week.contains(now.dayOfWeek.isoDayNumber) && hour == now.hour && minute == now.minute
}

/**
 * 检查当前[Timing]实例与给定的[Timing]实例是否相等。
 * 相等性基于两个[Timing]实例的week集合有交集，且hour和minute字段完全相同。
 *
 * @param newTiming 要比较的另一个[Timing]实例
 * @return 如果两个[Timing]实例根据上述条件相等则返回`true`，否则返回`false`
 */
fun Timing.isEqual(newTiming: Timing): Boolean {
    return week.any { newTiming.week.contains(it) } && hour == newTiming.hour && minute == newTiming.minute
}

/**
 * 创建一个新的[Timing]实例，确保其在给定的[timings]列表中是唯一的。
 *
 * 此方法基于当前系统时间生成一个[Timing]对象，并通过调用[copyUnique]方法来保证新生成的时间配置不会与[timings]列表中的任何已有时间配置重复。
 * 新的[Timing]实例将设置为当前的小时和分钟，并且默认一周中的每一天都会被包含（即`week`属性包含了从`1`到`7`）。
 *
 * @param timings 一个已有的[Timing]对象列表，用于检查新生成的时间配置是否唯一。
 * @return 返回一个新的[Timing]实例，该实例在提供的[timings]列表中是唯一的。
 */
@OptIn(ExperimentalTime::class)
fun Timing.createUnique(timings: List<Timing>): Timing {
    val now = Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    return copyUnique(
        timings = timings,
        week = mutableSetOf(1, 2, 3, 4, 5, 6, 7),
        hour = now.hour,
        minute = now.minute
    )
}


/**
 * 生成一个新的[Timing]实例，确保该实例在给定的[timings]列表以及[gameData.timerTasks]中的所有定时任务中是唯一的。
 * 如果新生成的[Timing]与现有定时任务时间冲突，则会通过增加分钟数来调整，直到找到一个唯一的时间点为止。
 * 调整过程中，如果小时超过23则会自动进位到下一天，并从0点开始计算。
 *
 * @param timings 已存在的[Timing]实例列表
 * @param week 一周中的哪几天执行此定时任务，默认为调用者自身的week属性值
 * @param hour 定时任务的具体小时数，默认为调用者自身的hour属性值
 * @param minute 定时任务的具体分钟数，默认为调用者自身的minute属性值
 * @return 返回一个保证不与[timings]和[gameData.timerTasks]中任何定时任务时间冲突的新[Timing]实例
 */
@OptIn(ExperimentalTime::class)
fun Timing.copyUnique(
    timings: List<Timing>,
    week: MutableSet<Int> = this.week,
    hour: Int = this.hour,
    minute: Int = this.minute,
): Timing {
    var newTiming = Timing(week, hour, minute)
    repeat(999) {
        val isExists =
            timings.any { it.isEqual(newTiming) } || gameDataStore2.data.timerTasks.any { timer ->
                timer.timings.any { it.isEqual(newTiming) }
            }
        if (isExists) {
            val totalMinutes = newTiming.hour * 60 + newTiming.minute + 1  // 总分钟数+1
            newTiming = newTiming.copy(hour = totalMinutes / 60, minute = totalMinutes % 60)
        } else {
            return newTiming
        }
    }
    return newTiming
}

