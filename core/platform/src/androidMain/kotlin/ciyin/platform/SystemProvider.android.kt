package ciyin.platform

import android.os.Build
import ciyin.io.File
import ciyin.platform.model.TaskSchedule

actual fun getPlatform(): SystemProvider = AndroidSystemProvider()

class AndroidSystemProvider : SystemProvider {

    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override val systemName: String = "Android"

    override val platformType: PlatformType = PlatformType.Android

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





