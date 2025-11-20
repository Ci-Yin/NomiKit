package com.ciyin.app.ui.app

import androidx.compose.runtime.Immutable
import com.ciyin.app.ui.app.navigation.NavRouter
import com.ciyin.app.ui.app.navigation.NavUiItem
import kotlinx.serialization.Serializable

/**
 * 应用 UI 状态数据类
 *
 * @property navList 导航列表
 * @property curNav 当前选中的导航项
 * @property startRoute 当前的路由对象
 */
@Immutable
@Serializable
data class AppUiState(
    val navList: List<NavUiItem>,
    val curNav: NavUiItem,
    val startRoute: NavRouter
)
