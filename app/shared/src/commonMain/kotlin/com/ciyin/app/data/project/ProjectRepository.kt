package com.ciyin.app.data.project

import com.ciyin.app.data.project.datasource.local.ProjectDataStorage
import com.ciyin.app.data.project.model.GameProject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 *
 * kotlin类作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/18 下午10:35
 */
class ProjectRepository(
    private val dataStore: ProjectDataStorage
) {

    /**
     * Observe game projects
     * @return a flow of game projects
     */
    fun observeGameProjects(): Flow<List<GameProject>> {
        return dataStore.state.map { it.projects }
    }

    /**
     * 保存数据功能
     *
     * 此函数旨在将当前的游戏数据保存到一个持久化的存储中
     */
    fun saveGameProjects(projects: List<GameProject>) {
        dataStore.update { it.copy(projects = projects) }
    }
}