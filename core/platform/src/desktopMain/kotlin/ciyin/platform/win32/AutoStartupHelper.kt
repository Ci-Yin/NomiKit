package ciyin.platform.win32

import ciyin.platform.win32.AutoStartupHelper.KEY
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg

/**
 * [AutoStartupHelper] 是一个帮助类，用于管理应用程序的自动启动设置。它提供了启用、禁用以及检查指定应用是否已设置为开机自启的功能。
 */
object AutoStartupHelper {

    /**
     * [KEY] 是一个常量字符串，用于表示注册表中的特定路径。
     * 该路径指向 Windows 操作系统中当前版本的运行项位置，
     * 通常用于存储应用程序的启动信息，使得这些应用程序能够在系统启动时自动运行。
     */
    private const val KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run"

    /**
     * 启用程序自启动功能。
     *
     * 该方法通过在注册表中设置指定应用程序的路径，来实现开机自启动。路径字符串会被双引号包围以防止路径中包含空格导致的问题。
     *
     * @param appName 应用程序名称，将作为注册表中的键名使用。
     * @param exePath 应用程序可执行文件的完整路径。
     */
    fun enableAutoStartup(appName: String, exePath: String) {
        Advapi32Util.registrySetStringValue(
            WinReg.HKEY_CURRENT_USER,
            KEY,
            appName,
            "\"$exePath\"" // 带引号以防路径里有空格
        )
    }

    /**
     * 禁用指定应用程序的自动启动。
     *
     * 该方法会检查注册表中是否存在给定的应用程序名称作为键值。如果存在，将删除该键值以禁用自动启动功能。
     *
     * @param appName 要禁用自动启动的应用程序名称。
     */
    fun disableAutoStartup(appName: String) {
        if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, KEY, appName)) {
            Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, KEY, appName)
        }
    }

    /**
     * 检查指定的应用程序是否设置了自动启动。
     *
     * 该函数通过读取注册表中的相关键值来判断给定的[appName]和[exePath]所代表的应用程序是否开启了自动启动功能。
     * 如果注册表中存在对应的键并且其值与提供的[exePath]相匹配（忽略大小写），则认为该应用程序已设置为自动启动。
     *
     * @param appName 应用程序名称，用于定位注册表中的特定键。
     * @param exePath 应用程序的可执行文件路径，用于验证自动启动设置。
     * @return 如果应用程序已设置为自动启动，则返回`true`；否则返回`false`。在发生异常时也会返回`false`。
     */
    fun isAutoStartupEnabled(appName: String, exePath: String): Boolean {
        return try {
            val value = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, KEY, appName)
            value.equals("\"$exePath\"", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
}