package com.ciyin.app.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.ui.app.navigation.MainRouter
import com.ciyin.app.ui.app.navigation.NavId.Main
import com.ciyin.app.ui.app.navigation.NavId.Null
import com.ciyin.app.ui.app.navigation.NavId.Settings
import com.ciyin.app.ui.app.navigation.NavId.Theme
import com.ciyin.app.ui.app.navigation.NavigationBar
import com.ciyin.app.ui.app.navigation.SettingRouter
import com.ciyin.app.ui.app.navigation.back
import com.ciyin.app.ui.app.navigation.navigateWithSingleTop
import com.ciyin.app.ui.screen.main.MainScreen
import com.ciyin.app.ui.screen.setting.SettingScreen
import com.ciyin.app.ui.theme.AppTheme
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.ui.tooling.preview.AppPreview


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/23 下午5:52
 */
private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MainRouter::class, MainRouter.serializer())
            subclass(SettingRouter::class, SettingRouter.serializer())
        }
    }
}

@Composable
fun App() {

    val viewModel = viewModel(::AppViewModel)
    val navBackStack = rememberNavBackStack(config, MainRouter)
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

}

@AppPreview
@Composable
fun AppPreview() {
    App()
}
