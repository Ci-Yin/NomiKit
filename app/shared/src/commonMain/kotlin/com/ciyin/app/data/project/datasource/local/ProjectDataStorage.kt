package com.ciyin.app.data.project.datasource.local

import ciyin.datastore.DataStorage
import ciyin.io.File
import ciyin.serialization.json.writeJson
import com.ciyin.app.data.project.model.ProjectLocalData
import com.ciyin.app.util.FilePath


/**
 *
 * kotlin类作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/6 14:50
 * @version: 1.0
 */
class ProjectDataStorage : DataStorage<ProjectLocalData>(
    defaultData = ProjectLocalData(),
    file = FilePath.GamaDataFile,
    /*onAfterRead = {
        it.gameProjects.forEach { project ->
            val file = project.gameConfig.toFile()
            if (file.exists()) {
                file.readJson<Game>().copy(id = project.games.uniqueId { game -> game.id }).apply {
                    project.games += this
                    file.writeJson(this, true)
                }
            }
        }
    },
     */
    onBeforePersist = { dataStore ->
        dataStore.projects.forEach { project ->
            val game = project.games.toList().find { it.isConfig } ?: return@forEach
            val config = File(project.gameConfig)
            if (config.exists()) {
                config.writeJson(game, true)
            }
        }
        dataStore
    },
)