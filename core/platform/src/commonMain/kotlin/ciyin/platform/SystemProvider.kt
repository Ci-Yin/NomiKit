package ciyin.platform

import ciyin.io.File
import ciyin.platform.model.TaskSchedule

interface SystemProvider {

    val name: String

    val systemName: String

    val platformType: PlatformType

    val packageName: String get() = "Rpa"

    fun getJavaHome(): String

    fun getAppDataDir(): File

    fun createScheduledTasksInFolder(
        taskFolder: String,
        taskNamePrefix: String,
        timings: List<TaskSchedule>
    )

    fun deleteScheduledTask(
        taskFolder: String,
        taskNamePrefix: String,
    )

    fun extractExeIcon(exePath: String, outputIcoPath: File, size: Int = 32)

    fun setAutoStartup(enable: Boolean)

}

enum class PlatformType {
    Android,
    Windows,
    Ios,
    Web,
    Unknown
}

val platform by lazy { getPlatform() }

fun SystemProvider.isAndroid() = platform.platformType == PlatformType.Android

fun SystemProvider.isWindows() = platform.platformType == PlatformType.Windows

fun SystemProvider.isIos() = platform.platformType == PlatformType.Ios

fun SystemProvider.isWeb() = platform.platformType == PlatformType.Web


expect fun getPlatform(): SystemProvider
