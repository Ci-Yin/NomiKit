package ciyin.system.storage

import ciyin.platform.Platform
import ciyin.platform.currentPlatformDesktop


data class AppInfo(
    val qualifier: String,
    val organization: String,
    val name: String,
)

interface AppFolderResolver {
    fun resolve(appInfo: AppInfo): AppDataDirectories

    companion object : AppFolderResolver by when (currentPlatformDesktop()) {
        is Platform.Linux -> UnixAppFolderResolver
        is Platform.MacOS -> UnixAppFolderResolver
        is Platform.Windows -> WindowsAppFolderResolver
    }
}

object WindowsAppFolderResolver : AppFolderResolver {
    override fun resolve(appInfo: AppInfo): AppDataDirectories {
        return runCatching {
            WindowsJnaAppFolderResolver.resolve(appInfo)
        }
//            .recoverCatching {
//            it.printStackTrace()
//            if (System.getenv("MYUKO_DISALLOW_PROJECT_DIRECTORIES_FALLBACK") == "true") throw it
//            ProjectDirectoriesAppFolderResolver.resolve(appInfo)
//        }
            .getOrThrow()
    }
}

object UnixAppFolderResolver : AppFolderResolver {
    override fun resolve(appInfo: AppInfo): AppDataDirectories {
        return ProjectDirectoriesAppFolderResolver.resolve(appInfo)
    }
}
