package ciyin.ui.foundation.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/10/26 16:09
 */


/**
 * 系统托盘
 *
 * @param onShowWindows 显示窗口的回调
 * @param exitApplication 退出程序回调
 * @param openWindowLabel 打开窗口菜单文案
 * @param exitLabel 退出菜单文案
 */
@Composable
fun SystemTray(
    icon: ImageBitmap,
    name: String,
    openWindowLabel: String = "Open Window",
    exitLabel: String = "Exit",
    onMenu: PopupMenu.() -> Unit = {},
    onShowWindows: () -> Unit,
    exitApplication: () -> Unit
) {
    if (!SystemTray.isSupported()) {
        println("系统托盘不支持")
        return
    }

    val image = remember(icon) { icon.toAwtImage() }
    val currentOnMenu = rememberUpdatedState(onMenu)
    val currentOnShowWindows = rememberUpdatedState(onShowWindows)
    val currentExitApplication = rememberUpdatedState(exitApplication)

    DisposableEffect(image, name, openWindowLabel, exitLabel) {
        val tray = SystemTray.getSystemTray()
        val popupMenu = PopupMenu().apply {
            menuItem(openWindowLabel) { currentOnShowWindows.value() }
            currentOnMenu.value(this)
            menuItem(exitLabel) { currentExitApplication.value() }
        }
        val trayIcon = TrayIcon(image, name, popupMenu).apply {
            isImageAutoSize = true
            addActionListener { currentOnShowWindows.value() }
        }
        val registered = runCatching { tray.add(trayIcon) }
            .onFailure { error ->
                System.err.println("System tray registration failed: ${error.message}")
            }
            .isSuccess

        onDispose {
            if (registered) {
                tray.remove(trayIcon)
            }
        }
    }
}

fun PopupMenu.menuItem(label: String, action: () -> Unit) {
    MenuItem(label).apply {
        addActionListener {
            action()
        }
    }.apply {
        this@menuItem.add(this)
    }
}
