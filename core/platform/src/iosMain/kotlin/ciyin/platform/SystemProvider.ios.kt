package ciyin.platform

import ciyin.io.File
import ciyin.platform.model.TaskSchedule

actual fun getPlatform(): SystemProvider {
    return IosSystemProvider()
}

class IosSystemProvider : SystemProvider {

    override val name: String = "Ios"

    override val systemName: String = "IOS"

    override val platformType: PlatformType = PlatformType.Ios

    override fun getJavaHome(): String {
        return ""
    }

    override fun getAppDataDir(): File {
        return File("")
    }

    override fun createScheduledTasksInFolder(
        taskFolder: String,
        taskNamePrefix: String,
        timings: List<TaskSchedule>
    ) {

    }

    override fun deleteScheduledTask(
        taskFolder: String,
        taskNamePrefix: String,
    ) {

    }

    override fun extractExeIcon(exePath: String, outputIcoPath: File, size: Int) {

    }

    override fun setAutoStartup(enable: Boolean) {

    }

}