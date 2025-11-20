package com.ciyin.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ciyin.foundation.SystemTray
import ciyin.foundation.currentWindowDpSize
import ciyin.jar.getScriptProjectClass
import ciyin.platform.AppArguments
import ciyin.platform.parseAppArguments
import com.ciyin.app.api.Platform
import com.ciyin.app.api.model.ScriptArgs
import com.ciyin.app.api.model.toKeyValueArgs
import com.ciyin.app.application.CommonApplication
import com.ciyin.app.application.DesktopApplication
import com.ciyin.app.data.project.datasource.DataStoreManager.settingLocalData2
import com.ciyin.app.domain.script.JarScriptManager
import com.ciyin.app.domain.timed.runTimerTask
import com.ciyin.app.ui.app.App
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import rpa.app.desktop.generated.resources.Res
import rpa.app.desktop.generated.resources.app_name
import rpa.app.desktop.generated.resources.ic_launcher


@Preview
@Composable
fun AppDesktopPreview() {
    App()
}

fun main(args: Array<String>) {

    val commonApplication = CommonApplication()
    val desktopApplication = DesktopApplication()

    desktopApplication.onCreate()
    commonApplication.onCreate()

    // 如果参数不为空，仅仅执行cil
    if (args.isNotEmpty()) {
        val arguments = parseAppArguments(args)
        if (arguments.timing) {
            runTimerTask()
        } else {
            runJarScriptWithArguments(arguments)
        }
        return
    }

    application {

        // 主窗口的状态
        val state = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(1000.dp, 800.dp),
//            size = DpSize(450.dp, 1000.dp),
        )
        var isVisible by remember { mutableStateOf(settingLocalData2.data.startInTray.not()) }

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
                desktopApplication.onDestroy()
                commonApplication.onDestroy()
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

}


private fun runJarScriptWithArguments(arguments: AppArguments) = runBlocking {
    JarScriptManager.run(
        jarPath = arguments.jarPath,
        args = ScriptArgs(
            driverPath = arguments.windowsDriverPath.ifBlank { settingLocalData2.data.windowsDriverPath },
            scriptProjectClass = arguments.scriptProjectClass.ifBlank {
                getScriptProjectClass(arguments.jarPath)
            },
            platform = Platform.Windows.ordinal,
        ).toKeyValueArgs()
    )

    JarScriptManager.wait(arguments.jarPath)
}


