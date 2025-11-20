package com.ciyin.app.ui.app

import com.ciyin.app.ui.app.navigation.NavUiItem


/**
 *
 * kotlin接口作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/13 20:29
 * @version: 1.0
 */
sealed interface AppAction {
    data class NavigateItemClick(val nav: NavUiItem) : AppAction
}