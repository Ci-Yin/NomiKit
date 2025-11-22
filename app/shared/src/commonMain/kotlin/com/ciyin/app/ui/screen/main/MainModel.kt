package com.ciyin.app.ui.screen.main

import androidx.compose.runtime.Immutable
import com.ciyin.app.data.project.model.Game

/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/16 下午10:28
 */


/**
 * 游戏项目的数据类
 * @property id 项目ID
 * @property type 项目类型
 * @property name 项目名称
 * @property selection 当前选择的游戏索引
 * @property gameConfig 游戏配置
 * @property packageName 包名
 * @property jarPath Jar文件路径
 * @property scriptProjectClass 脚本项目类
 * @property icon 图标
 * @property games 游戏列表
 */
@Immutable
data class ProjectUiModel(
    val id: Int,
    val type: Int,
    val name: String,
    val selection: Int = 0,
    val isRunning: Boolean = false,
    val gameConfig: String = "",
    val packageName: String = "",
    val jarPath: String = "",
    val scriptProjectClass: String = "",
    val icon: String = "",
    val games: List<Game>,
)

/**
 * 获取或设置当前选择的游戏
 */
val ProjectUiModel.game get() = games[selection]


