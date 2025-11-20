package com.ciyin.app.domain.timed

import ciyin.system.coroutines.runBlockingCrossPlatform
import com.ciyin.app.data.project.datasource.local.ProjectDataStorage
import com.ciyin.app.domain.script.JarScriptManager
import com.ciyin.app.domain.script.usecase.RunJarScriptUseCase
import com.ciyin.app.ui.screen.timer.TimerTask
import com.ciyin.app.ui.screen.timer.isCurTime
import com.ciyin.app.ui.screen.timer.project
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.mp.KoinPlatform.getKoin

class TimedProjectRunner(
    private val runJarScriptUseCase: RunJarScriptUseCase,
    private val dataStorage: ProjectDataStorage,
) : KoinComponent {

    var timerTask: TimerTask? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun start() {
        dataStorage.snapshot.timerTasks.asFlow()
            .filter { timer ->
                timer.timings.any { it.isCurTime() }
            }
            .onEach { timer ->
                timerTask = timer
                runJarScriptUseCase(timer.project)
                JarScriptManager.wait(timer.project.jarPath, timer.name)
            }
            .onCompletion {
                it?.printStackTrace()
                JarScriptManager.destroyAll()
            }
            .collect()
    }

    fun stop() {
        JarScriptManager.destroyAll()
    }

}

fun runTimerTask(): TimedProjectRunner = runBlockingCrossPlatform {
    getKoin().get<TimedProjectRunner>().apply { start() }
}