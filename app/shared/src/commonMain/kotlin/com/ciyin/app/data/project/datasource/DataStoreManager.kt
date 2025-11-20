package com.ciyin.app.data.project.datasource

import ciyin.io.File
import ciyin.serialization.json.writeJson
import com.ciyin.app.data.project.model.ProjectLocalData
import com.ciyin.app.data.setting.model.SettingLocalData
import com.ciyin.app.util.DataStore
import com.ciyin.app.util.FilePath

object DataStoreManager {

    val gameDataStore2 by lazy {
        DataStore(
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
            onBeforeWrite = {
                projects.forEach { project ->
                    val game = project.games.toList().find { it.isConfig } ?: return@forEach
                    val config = File(project.gameConfig)
                    if (config.exists()) {
                        config.writeJson(game, true)
                    }
                }
                this
            },
        )
    }

    val settingLocalData2 by lazy { DataStore(SettingLocalData(), FilePath.ConfigDataFile) }

}