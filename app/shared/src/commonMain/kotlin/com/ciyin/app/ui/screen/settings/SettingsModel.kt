package com.ciyin.app.ui.screen.settings

import androidx.compose.runtime.Immutable
import ciyin.io.File

/**
 * 设置项密封类
 */
@Immutable
sealed class SettingItem {

    abstract val id: String
    abstract val title: String
    abstract val enable: Boolean

    /**
     * 面板类型设置
     */
    data class Panel(
        override val id: String,
        override val title: String,
        override val enable: Boolean = true,
        val icon: Int = 0,
        val settings: List<SettingItem> = emptyList()
    ) : SettingItem()

    /**
     * 目录选择设置
     */
    data class Directory(
        override val id: String,
        override val title: String,
        override val enable: Boolean = true,
        val path: String = "",
        val file: File? = null
    ) : SettingItem()

    /**
     * 文件选择设置
     */
    data class FileSelect(
        override val id: String,
        override val title: String,
        override val enable: Boolean = true,
        val path: String = "",
        val file: File? = null
    ) : SettingItem()

    /**
     * 单选设置
     */
    data class Choice(
        override val id: String,
        override val title: String,
        override val enable: Boolean = true,
        val menus: List<Menu> = emptyList(),
        val selectedIndex: Int = 0
    ) : SettingItem()

    /**
     * 开关设置
     */
    data class Switch(
        override val id: String,
        override val title: String,
        override val enable: Boolean = true,
        val checked: Boolean = false
    ) : SettingItem()

    /**
     * 导航设置
     */
    data class Navigation(
        override val id: String,
        override val title: String,
        override val enable: Boolean = true,
        val route: String = "",
        val icon: Int = 0
    ) : SettingItem()

    /**
     * 通用设置
     */
    data class Generic(
        override val id: String,
        override val title: String,
        override val enable: Boolean = true,
        val content: String = ""
    ) : SettingItem()
}

/**
 * 菜单数据类
 */
@Immutable
data class Menu(
    val id: Int,
    val title: String
)