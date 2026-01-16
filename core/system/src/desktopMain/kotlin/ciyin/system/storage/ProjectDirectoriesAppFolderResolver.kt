package ciyin.system.storage

import dev.dirs.ProjectDirectories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout
import java.io.File

object ProjectDirectoriesAppFolderResolver : AppFolderResolver {
    override fun resolve(appInfo: AppInfo): AppDataDirectories = runBlocking {
        withTimeout(5000) {
            runInterruptible(Dispatchers.IO) {
                val projectDirectories = ProjectDirectories.from(
                    appInfo.qualifier,
                    appInfo.organization,
                    appInfo.name,
                )

                AppDataDirectories(
                    File(projectDirectories.dataDir).toPath(),
                    File(projectDirectories.cacheDir).toPath(),
                )
            }
        }
    }
}