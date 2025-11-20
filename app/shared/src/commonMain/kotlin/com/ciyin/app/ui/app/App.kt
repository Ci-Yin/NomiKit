package com.ciyin.app.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ciyin.foundation.viewmodel.viewModel
import com.ciyin.app.ui.app.navigation.LogcatRouter
import com.ciyin.app.ui.app.navigation.MainRouter
import com.ciyin.app.ui.app.navigation.NavId.Logcat
import com.ciyin.app.ui.app.navigation.NavId.Main
import com.ciyin.app.ui.app.navigation.NavId.Null
import com.ciyin.app.ui.app.navigation.NavId.Settings
import com.ciyin.app.ui.app.navigation.NavId.Theme
import com.ciyin.app.ui.app.navigation.NavId.Timer
import com.ciyin.app.ui.app.navigation.NavigationBar
import com.ciyin.app.ui.app.navigation.SettingsRouter
import com.ciyin.app.ui.app.navigation.TimerRouter
import com.ciyin.app.ui.app.navigation.navigateTo
import com.ciyin.app.ui.screen.logcat.LogcatScreen
import com.ciyin.app.ui.screen.main.MainScreen
import com.ciyin.app.ui.screen.settings.SettingsScreen
import com.ciyin.app.ui.screen.timer.TimerScreen
import com.ciyin.app.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.AppPreview


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2024/10/23 下午5:52
 * @version: 1.0
 */

@Composable
fun App() {

    val viewModel = viewModel(::AppViewModel)
    val appNavController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppTheme {
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
                viewModel(AppAction.NavigateItemClick(nav))
                when (nav.id) {
                    Main -> appNavController.navigateTo(MainRouter)
                    Timer -> appNavController.navigateTo(TimerRouter)
                    Logcat -> appNavController.navigateTo(LogcatRouter)
                    Theme -> {}
                    Settings -> appNavController.navigateTo(SettingsRouter)
                    Null -> {}
                }
            }
        ) {
            NavHost(appNavController, state.startRoute) {
                composable<MainRouter> { MainScreen() }
                composable<TimerRouter> { TimerScreen() }
                composable<LogcatRouter> { LogcatScreen() }
                composable<SettingsRouter> { SettingsScreen() }
            }
        }
        AppDialog()
    }

}

@AppPreview
@Composable
fun AppPreview() {
    App()
}
