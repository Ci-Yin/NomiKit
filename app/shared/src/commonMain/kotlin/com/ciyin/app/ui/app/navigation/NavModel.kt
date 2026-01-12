package com.ciyin.app.ui.app.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.ciyin.app.ui.theme.iconpack.IconPack
import com.ciyin.app.ui.theme.iconpack.Null
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

enum class NavId {
    Main,
    Null,
    Theme,
    Settings,
}

enum class NavigationSuiteType {
    NavigationBar,
    NavigationRail,
    NavigationDrawer
}

