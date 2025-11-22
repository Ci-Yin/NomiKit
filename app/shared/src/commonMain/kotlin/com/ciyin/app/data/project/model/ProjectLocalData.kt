package com.ciyin.app.data.project.model

import com.ciyin.app.data.project.datasource.defaultGameProjects
import com.ciyin.app.ui.screen.timer.TimerTask
import kotlinx.serialization.Serializable


/**
 *
 * kotlin类作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/20 上午1:09
 */
@Serializable
data class ProjectLocalData(
    var projectSelected: Int = 0,
    val projects: List<GameProject> = defaultGameProjects,
    val timerTasks: List<TimerTask> = emptyList(),
)

val ProjectLocalData.project: GameProject get() = projects[projectSelected]

