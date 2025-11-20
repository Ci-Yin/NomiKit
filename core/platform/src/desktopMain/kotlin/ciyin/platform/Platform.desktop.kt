package ciyin.platform

import ciyin.io.File
import ciyin.io.resolve
import ciyin.io.toFile
import ciyin.platform.model.TaskSchedule
import ciyin.platform.win32.AutoStartupHelper
import ciyin.platform.win32.ExeIconExtractor
import ciyin.platform.win32.getCurrentExePath

actual fun getPlatform(): Platform = DesktopPlatform()

class DesktopPlatform : Platform {

    override val name: String = "Java ${System.getProperty("java.version")}"

    override val systemName: String = "Windows"

    override val platformType: PlatformType = PlatformType.Windows

    override fun getJavaHome(): String {
        return System.getenv("JAVA_HOME")
    }

    override fun getAppDataDir(): File {
        return System.getenv("APPDATA").toFile().resolve(packageName) // 获取 APPDATA 环境变量的值
    }

    override fun deleteScheduledTask(taskFolder: String, taskNamePrefix: String) =
        ciyin.platform.win32.deleteScheduledTask(
            taskFolder = taskFolder,
            taskNamePrefix = taskNamePrefix
        )

    override fun createScheduledTasksInFolder(
        taskFolder: String,
        taskNamePrefix: String,
        timings: List<TaskSchedule>
    ) = ciyin.platform.win32.createScheduledTasksInFolder(
        taskFolder = taskFolder,
        taskNamePrefix = taskNamePrefix,
        timings = timings,
        execPath = getCurrentExePath(),
        programArgs = AppArguments(timing = true).toProgramArgs()
    )

    override fun extractExeIcon(exePath: String, outputIcoPath: File, size: Int) {
        ExeIconExtractor.extractExeIcon(exePath, java.io.File(outputIcoPath.absolutePath), size)
    }

    override fun setAutoStartup(enable: Boolean) {
        if (enable) {
            AutoStartupHelper.enableAutoStartup(packageName, getCurrentExePath())
        } else {
            AutoStartupHelper.disableAutoStartup(packageName)
        }
    }

}





