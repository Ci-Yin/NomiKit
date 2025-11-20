package ciyin.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/10/26 16:09
 * @version: 1.0
 */


/**
 * 系统托盘
 *
 * @param onShowWindows 显示窗口的回调
 * @param exitApplication 退出程序回调
 */
@Composable
fun SystemTray(
    icon: ImageBitmap,
    name: String,
    onMenu: PopupMenu.() -> Unit = {},
    onShowWindows: () -> Unit,
    exitApplication: () -> Unit
) {
    if (!SystemTray.isSupported()) {
        println("系统托盘不支持")
        return
    }

    val tray = SystemTray.getSystemTray()
//    val icon = imageResource(Res.drawable.ic_launcher).toAwtImage()

    // 创建菜单
    val popupMenu = PopupMenu().apply {

        // 打开窗口
        menuItem("Open Window") {
            onShowWindows()
        }
        onMenu()
        // 退出程序
        menuItem("Exit") {
            //trayIcon?.let { tray.remove(it) }  // 安全移除托盘图标
            exitApplication()
        }

    }

    // 托盘图标
    val image = remember(icon) { icon.toAwtImage() }
    val trayIcon = TrayIcon(image, name, popupMenu).apply {
        isImageAutoSize = true
        addActionListener {
            // 双击托盘图标 -> 打开窗口
            onShowWindows()
        }
    }

    // 添加到系统托盘
    tray.add(trayIcon)
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