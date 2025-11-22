package com.ciyin.app.ui.app

import androidx.lifecycle.SavedStateHandle
import ciyin.foundation.viewmodel.AbsMviViewModel
import ciyin.foundation.viewmodel.MviStore
import com.ciyin.app.ui.app.AppAction.NavigateItemClick
import com.ciyin.app.ui.app.navigation.MainRouter
import com.ciyin.app.ui.app.navigation.NavId.Logcat
import com.ciyin.app.ui.app.navigation.NavId.Main
import com.ciyin.app.ui.app.navigation.NavId.Null
import com.ciyin.app.ui.app.navigation.NavId.Settings
import com.ciyin.app.ui.app.navigation.NavId.Theme
import com.ciyin.app.ui.app.navigation.NavId.Timer
import com.ciyin.app.ui.app.navigation.NavUiItem
import com.ciyin.app.ui.theme.iconpack.Home
import com.ciyin.app.ui.theme.iconpack.IconPack
import com.ciyin.app.ui.theme.iconpack.LightMode
import com.ciyin.app.ui.theme.iconpack.Logcat
import com.ciyin.app.ui.theme.iconpack.Null
import com.ciyin.app.ui.theme.iconpack.Settings
import com.ciyin.app.ui.theme.iconpack.Timing
import com.ciyin.app.util.value
import org.koin.core.component.KoinComponent
import rpa.app.shared.generated.resources.Res
import rpa.app.shared.generated.resources.nav_home
import rpa.app.shared.generated.resources.nav_logcat
import rpa.app.shared.generated.resources.nav_settings
import rpa.app.shared.generated.resources.nav_theme
import rpa.app.shared.generated.resources.nav_timer


/**
 *
 * kotlin类作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/2 下午12:31
 */
class AppViewModel(savedStateHandle: SavedStateHandle) :
    AbsMviViewModel<AppUiState, AppAction, AppEffect>(savedStateHandle), KoinComponent {

    private val navList = listOf(
        NavUiItem(Main, IconPack.Home, Res.string.nav_home.value, true),
        NavUiItem(Timer, IconPack.Timing, Res.string.nav_timer.value, true),
        NavUiItem(Logcat, IconPack.Logcat, Res.string.nav_logcat.value, true),
        NavUiItem(Null, IconPack.Null, ""),
        NavUiItem(Theme, IconPack.LightMode, Res.string.nav_theme.value),
        NavUiItem(Settings, IconPack.Settings, Res.string.nav_settings.value, true),
    )

    /**
     * 初始化应用的UI状态
     *
     * @return AppUiState 返回初始化后的应用UI状态对象，包含导航列表、起始路由和当前导航项
     */
    override val initState: AppUiState = AppUiState(
        navList = navList,
        startRoute = MainRouter,
        curNav = navList.find { it.id == Main }!!
    )


    /**
     * 定义MVI存储的状态更新规范
     *
     * 该函数通过重写spec方法来定义应用状态的处理逻辑，
     * 当接收到NavigateItemClick动作时，会更新当前的导航状态
     */
    override fun MviStore<AppUiState, AppAction>.spec() {
        // 处理导航项点击事件，更新当前导航状态
        on<NavigateItemClick> { action ->
            update { copy(curNav = action.nav) }
        }
    }


}
