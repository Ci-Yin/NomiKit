package com.ciyin.app.ui.app

import com.ciyin.app.ui.app.navigation.NavUiItem


/**
 *
 * kotlin接口作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/13 20:29
 */
sealed interface AppAction {
    data class NavigateItemClick(val nav: NavUiItem) : AppAction
}