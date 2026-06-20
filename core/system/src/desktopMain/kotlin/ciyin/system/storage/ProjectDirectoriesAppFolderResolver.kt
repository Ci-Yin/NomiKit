package ciyin.system.storage

import dev.dirs.ProjectDirectories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout
import java.io.File

/**
 * 基于 directories-jvm 的桌面应用目录解析器。
 */
object ProjectDirectoriesAppFolderResolver : AppFolderResolver {
    override fun resolve(appInfo: AppInfo): AppDataDirectories = runBlocking {
        withTimeout(5000) {
            runInterruptible(Dispatchers.IO) {
                val projectDirectories = when (appInfo) {
                    is AppInfo.ApplicationId -> ProjectDirectories.from("", "", appInfo.id)
                    is AppInfo.OrganizationName -> ProjectDirectories.from(
                        appInfo.qualifier,
                        appInfo.organization,
                        appInfo.name,
                    )
                }

                AppDataDirectories(
                    File(projectDirectories.dataDir).toPath(),
                    File(projectDirectories.cacheDir).toPath(),
                )
            }
        }
    }
}
