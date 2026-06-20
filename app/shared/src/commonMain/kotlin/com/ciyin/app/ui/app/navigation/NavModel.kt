package com.ciyin.app.ui.app.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import ciyin.material.theme.iconpack.IconPack
import ciyin.material.theme.iconpack.Null
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 导航项 UI 数据类
 *
 * @property id 导航项 ID
 * @property icon 导航项图标
 * @property title 导航项标题
 * @property nav 是否可导航
 */
@Immutable
@Serializable
data class NavUiItem(
    val id: NavId,
    @Transient
    val icon: ImageVector = IconPack.Null,
    val title: String,
    val nav: Boolean = false,
)

/**
 * 应用主导航项 ID。
 */
enum class NavId {
    /** 首页导航项。 */
    Main,

    /** 空占位导航项。 */
    Null,

    /** 主题导航项。 */
    Theme,

    /** 设置导航项。 */
    Settings,
}

/**
 * 主导航组件的布局类型。
 */
enum class NavigationSuiteType {
    /** 底部导航栏。 */
    NavigationBar,

    /** 侧边导航栏。 */
    NavigationRail,

    /** 导航抽屉。 */
    NavigationDrawer
}

