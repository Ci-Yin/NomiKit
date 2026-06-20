package com.ciyin.app.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ciyin.material.theme.AppTheme
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.ui.app.navigation.MainRouter
import com.ciyin.app.ui.app.navigation.NavId.Main
import com.ciyin.app.ui.app.navigation.NavId.Null
import com.ciyin.app.ui.app.navigation.NavId.Settings
import com.ciyin.app.ui.app.navigation.NavId.Theme
import com.ciyin.app.ui.app.navigation.NavSavedStateConfig
import com.ciyin.app.ui.app.navigation.NavigationBar
import com.ciyin.app.ui.app.navigation.SettingRouter
import com.ciyin.app.ui.app.navigation.back
import com.ciyin.app.ui.app.navigation.navigateWithSingleTop
import com.ciyin.app.ui.screen.main.MainScreen
import com.ciyin.app.ui.screen.setting.SettingScreen


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/23 下午5:52
 */

/**
 * 应用根入口。
 */
@Composable
fun App() {
    val viewModel = viewModel(::AppViewModel)
    val state by viewModel.state.collectAsStateWithLifecycle()
    AppContent(state, viewModel.dispatchAction)
}

/**
 * 应用根内容，负责主题包裹与导航树装配。
 *
 * @param state 应用 UI 状态
 * @param onAction 应用动作分发回调
 */
@Composable
private fun AppContent(
    state: AppUiState,
    onAction: (AppAction) -> Unit
) = AppTheme {
    val navBackStack = rememberNavBackStack(NavSavedStateConfig, MainRouter)
    NavigationBar(
        navList = state.navList,
        selection = remember(state.curNav) {
            if (state.curNav.nav) {
                state.navList.indexOf(state.curNav)
            } else {
                -1
            }
        },
        onNavigateItemClick = { nav ->
            onAction(AppAction.NavigateItemClick(nav))
            when (nav.id) {
                Main -> navBackStack.navigateWithSingleTop(MainRouter)
                Theme -> {}
                Settings -> navBackStack.navigateWithSingleTop(SettingRouter)
                Null -> {}
            }
        }
    ) {
        NavDisplay(
            backStack = navBackStack,
            onBack = {
                navBackStack.back()
            },
            entryProvider = entryProvider {
                entry<MainRouter> {
                    MainScreen()
                }
                entry<SettingRouter> {
                    SettingScreen()
                }
            }
        )
    }
}

