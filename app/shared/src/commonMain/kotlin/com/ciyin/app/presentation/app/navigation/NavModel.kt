package com.ciyin.app.presentation.app.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.icon.Null
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
    /**
     * 首页导航项。
     */
    Main,

    /**
     * 空占位导航项；导航外壳把它解释为主导航与底部导航的分组边界。
     */
    Null,

    /**
     * 主题导航项。
     */
    Theme,

    /**
     * 设置导航项。
     */
    Settings,
}

