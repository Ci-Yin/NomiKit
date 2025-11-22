package com.ciyin.app.ui.screen.main

import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewModelScope
import ciyin.foundation.viewmodel.AbsMvvmViewModel
import ciyin.io.File
import ciyin.io.copy
import ciyin.io.extension
import ciyin.io.nameWithoutExtension
import ciyin.io.resolve
import ciyin.io.toFile
import ciyin.lang.unit
import ciyin.platform.platform
import ciyin.serialization.json.readJson
import ciyin.system.coroutines.IO
import ciyin.system.utils.image.ImageUtils
import ciyin.system.utils.image.resizeImageFile
import com.ciyin.app.data.project.ProjectRepository
import com.ciyin.app.data.project.datasource.defaultGameProjects
import com.ciyin.app.data.project.datasource.defaultGames
import com.ciyin.app.data.project.model.Game
import com.ciyin.app.domain.script.JarScriptManager
import com.ciyin.app.domain.script.usecase.RunJarScriptUseCase
import com.ciyin.app.ui.app.dialog
import com.ciyin.app.ui.app.dialogError
import com.ciyin.app.util.DataStore
import com.ciyin.app.util.FilePath.IconsDir
import com.ciyin.app.util.addAfter
import com.ciyin.app.util.uniqueId
import com.ciyin.app.util.withIncrementName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


/**
 *
 * kotlin类作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/1 下午6:44
 */
class MainViewModel() : AbsMvvmViewModel<MainUiState, MainEffect>(null), KoinComponent {

    private val repository by inject<ProjectRepository>()
    private val runJarScriptUseCase by inject<RunJarScriptUseCase>()

    private val scriptState = JarScriptManager.state

    init {
        repository.observeGameProjects().onEach { projects ->
            updateState {
                copy(projects = projects.map { it.toProjectUiModel() })
            }
        }.launchIn(viewModelScope)
    }

    override fun initState(): MainUiState {
        return MainUiState()
    }

    fun onProjectIconSelected(
        project: ProjectUiModel,
        path: String,
        intSize: IntSize
    ) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val selectedPath = path.toFile()
            val icon = IconsDir.resolve(selectedPath.nameWithoutExtension.lowercase() + "_icon.png")
            val iconPath = if (selectedPath.extension == "exe") {
                platform.extractExeIcon(path, icon)
                icon
            } else {
                runCatching {
                    ImageUtils.resizeImageFile(selectedPath, icon, intSize.width, intSize.height)
                    icon
                }.getOrElse {
                    it.printStackTrace()
                    IconsDir.resolve(selectedPath.nameWithoutExtension.lowercase() + "_icon." + selectedPath.extension)
                        .apply {
                            icon.copy(this)
                        }
                }
            }

            if (iconPath.exists().not()) error("文件不存在$iconPath")

            updateProject(project.id) {
                copy(icon = iconPath.absolutePath)
            }

            onSaveData()

        }.onFailure {
            dialogError("图标文件读取失败\n$it")
            it.printStackTrace()
        }
    }.unit()

    fun onProjectCreateClick(type: Int) {
        val project = defaultGameProjects.find { it.type == type } ?: error("")
        updateState {
            copy(
                projects = projects + ProjectUiModel(
                    id = projects.size,
                    name = "新建项目".withIncrementName(projects) { it.name },
                    type = project.type,
                    selection = project.selection,
                    games = project.games.toMutableStateList()
                )
            )
        }
    }

    /**
     * 创建项目的副本。
     *
     * @param project 当前要被复制的游戏项目实例。
     */
    fun onProjectCopyClick(project: ProjectUiModel) {
        updateState {
            val mutableProjects = projects.toMutableList().apply {
                addAfter(
                    project,
                    project.copy(
                        id = size,
                        name = project.name.withIncrementName(this) { it.name },
                    )
                )
            }
            copy(projects = mutableProjects)
//              .depthCopy<GameProjectUiModel>()
        }
        onSaveData()
    }

    /**
     * 检查给定的项目ID是否已经存在于项目列表中。
     *
     * @param project 当前操作的游戏项目实例。
     * @param value 需要检查是否重复的项目ID，以字符串形式提供。
     * @return 如果提供的ID在项目列表中已存在（除了当前项目自身），则返回true；否则返回false。
     */
    fun onProjectIdIsRepeat(project: ProjectUiModel, value: String): Boolean {
        if (project.id == value.toInt()) return false
        return stateValue.projects.find { it.id == value.toInt() } != null
    }

    /**
     * 处理弹窗项目编辑变更。
     *
     * @param project 当前要被修改的游戏项目实例。
     * @param new 包含了新的项目信息的编辑对象。
     */
    fun onProjectEditChange(project: ProjectUiModel, new: ProjectUiModel) {

        val games = project.games.toMutableList()
        val config = File(new.gameConfig)
        if (project.gameConfig != new.gameConfig && config.exists()) {
            games.removeAll { it.isConfig }
            games += config.readJson<Game>().copy(
                id = project.games.uniqueId { it.id },
                isConfig = true,
                preset = "配置文件"
            )
        } else {
            games.removeAll { it.isConfig }
        }
        updateProject(project.id) {
            new.copy(games = games.map { it.copy(packageName = new.packageName) })
        }
        onSaveData()

    }

    fun onProjectRemoveClick(project: ProjectUiModel) {
        updateState {
            copy(projects = projects.toMutableList().apply { remove(project) })
        }
        onSaveData()
    }

    /**
     * 重置当前游戏项目的任务为默认状态。
     */
    fun onTaskResetClick(project: ProjectUiModel) {
        val defGame = defaultGames.find { it.type == project.game.type }!!.copy(
            id = project.game.id,
            preset = project.game.preset,
            isConfig = project.game.isConfig
        )
//      .depthCopy<Game>()
        updateProject(project.id) {
            copy(games = games.map { if (it.id == project.game.id) defGame else it })
        }
        onSaveData()
    }

    /**
     * 处理预设菜单选择变更事件。
     *
     * 此方法用于当用户更改了某个游戏项目的预设菜单选项时，更新该项目的`selection`属性，并保存当前的数据状态。
     *
     * @param project 当前操作的游戏项目实例。
     * @param selection 用户选择的新选项索引。
     */
    fun onPresetMenuSelectedChange(project: ProjectUiModel, selection: Int) {
        updateProject(project.id) {
            copy(selection = selection)
        }
        onSaveData()
    }

    /**
     * 处理预设菜单中`配置文件`项移除点击事件。
     *
     * 该方法在用户尝试从项目中移除一个配置为可保存到[ProjectUiModel.gameConfig]的配置时被调用。
     * 如果提供的预设[game]对象的[Game.isConfig]属性为`true`，则将该项目中的[ProjectUiModel.gameConfig]字段设置为空字符串，
     *
     * @param project 当前操作的游戏项目实例。
     * @param game 被考虑移除的游戏实例。
     */
    fun onPresetItemRemoveClick(project: ProjectUiModel, game: Game) {
        if (game.isConfig) {
            updateProject(project.id) {
                copy(gameConfig = "")
            }
            onSaveData()
        }
    }

    /**
     * 控制脚本的运行状态。
     */
    fun onScriptRunClick(project: ProjectUiModel) = runCatching {
        // 运行jar文件
        viewModelScope.launch {
            updateProject(project.id) { copy(isRunning = true) }
            runJarScriptUseCase(project.toGameProject()).fold(
                ifLeft = {
                    dialog(message = it.message)
                },
                ifRight = {
                    updateProject(project.id) { copy(isRunning = false) }
                }
            )
        }
        //appNavController.navigateTo(NavId.Logcat.name)
    }.onFailure {
        it.printStackTrace()
        dialog(message = it.message)
    }

    /**
     * 控制脚本的运行状态。
     */
    fun onScriptRunClick() {
        if (scriptState.value.isRunning) {
            JarScriptManager.destroyAll()
        } else {
            //appNavController.navigateTo(NavId.Logcat.name)
        }
    }

    /**
     * 保存数据功能
     *
     * 此函数旨在将当前的游戏数据保存到一个持久化的存储中
     * 它调用了[DataStore.writeBacking]方法来执行实际的数据写入操作
     */
    fun onSaveData() {
        repository.saveGameProjects(stateValue.projects.map { it.toGameProject() })
    }

    private fun updateProject(
        id: Int,
        transform: ProjectUiModel.() -> ProjectUiModel
    ) = updateState {
        copy(projects = projects.map { if (it.id == id) transform(it) else it })
    }

}
