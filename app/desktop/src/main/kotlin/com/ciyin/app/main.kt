package com.ciyin.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import ciyin.application.runApplication
import ciyin.ui.foundation.currentWindowDpSize
import ciyin.ui.foundation.widget.SystemTray
import com.ciyin.app.application.DesktopApplication
import com.ciyin.app.ui.app.App
import nomikit.app.desktop.generated.resources.Res
import nomikit.app.desktop.generated.resources.app_name
import nomikit.app.desktop.generated.resources.ic_launcher
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


fun main(args: Array<String>) = runApplication(::DesktopApplication) {

    // 主窗口的状态
    val state = rememberWindowState(
        position = WindowPosition.Aligned(Alignment.Center),
        size = DpSize(1000.dp, 800.dp),
//            size = DpSize(450.dp, 1000.dp),
    )
    var isVisible by remember { mutableStateOf(true) }

    currentWindowDpSize = state.size

    // 系统托盘图标
    SystemTray(
        icon = imageResource(Res.drawable.ic_launcher),
        name = stringResource(Res.string.app_name),
        onShowWindows = {
            isVisible = true
            state.isMinimized = false
        },
        exitApplication = {
            exitApplication()
        }
    )


    Window(
        state = state,
        visible = isVisible,
        icon = painterResource(Res.drawable.ic_launcher),
        title = stringResource(Res.string.app_name),
        onCloseRequest = {
            isVisible = false
        },
        content = {
            App()
        }
    )

}


