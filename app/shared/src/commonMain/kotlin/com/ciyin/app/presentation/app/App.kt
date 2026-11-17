package com.ciyin.app.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ciyin.designsystem.fluent.navigation.LocalFluentSystemBackdropActive
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.presentation.app.navigation.MainRouter
import com.ciyin.app.presentation.app.navigation.NavId.Main
import com.ciyin.app.presentation.app.navigation.NavId.Null
import com.ciyin.app.presentation.app.navigation.NavId.Settings
import com.ciyin.app.presentation.app.navigation.NavId.Theme
import com.ciyin.app.presentation.app.navigation.NavSavedStateConfig
import com.ciyin.app.presentation.app.navigation.NavigationBar
import com.ciyin.app.presentation.app.navigation.SettingRouter
import com.ciyin.app.presentation.app.navigation.back
import com.ciyin.app.presentation.app.navigation.navigateWithSingleTop
import com.ciyin.app.presentation.screen.main.MainScreen
import com.ciyin.app.presentation.screen.setting.SettingScreen


/**
 * 应用根入口，按生命周期收集 AppViewModel 状态，并将动作分发到该 ViewModel。
 *
 * 主题与导航树由纯内容层装配。
 *
 * @param windowBackdropActive 当前窗口是否启用系统背景材质，原样传给内容层。
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/23 下午5:52
 */
@Composable
fun App(windowBackdropActive: Boolean = false) {
    val viewModel = viewModel(::AppViewModel)
    val state by viewModel.state.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalFluentSystemBackdropActive provides windowBackdropActive) {
        NomiTheme(NomiDesignSystem.Default) {
            AppContent(state, viewModel.dispatchAction)
        }
    }
}

/**
 * 应用根内容。
 *
 * @param state 提供导航条目与当前选中项的 UI 状态；不在导航条目中的页面不选中任何项。
 * @param onAction 点击导航项时接收 NavigateItemClick；当前内容层同时将主页面或设置页路由更新到子栈。
 */
@Composable
private fun AppContent(
    state: AppUiState,
    onAction: (AppAction) -> Unit
) {
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
