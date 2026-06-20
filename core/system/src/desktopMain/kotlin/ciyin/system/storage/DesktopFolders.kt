package ciyin.system.storage

import ciyin.platform.Platform
import ciyin.platform.currentPlatformDesktop


/**
 * 桌面应用用于解析系统数据目录的身份信息。
 */
sealed interface AppInfo {
    /**
     * 应用的稳定标识，用于日志、调试和默认目录名。
     */
    val id: String

    /**
     * 使用完整应用 ID 作为应用目录名的身份信息。
     *
     * @property id 完整应用 ID，例如 `com.ciyin.nomikit`
     */
    data class ApplicationId(
        override val id: String,
    ) : AppInfo {
        init {
            require(id.isNotBlank()) { "app.id 不能为空" }
        }
    }

    /**
     * 使用组织名和应用名分层解析目录的身份信息。
     *
     * @property qualifier 反向域名前缀，例如 `com`
     * @property organization 组织目录名，例如 `CiYin`
     * @property name 应用目录名，例如 `NomiKit`
     */
    data class OrganizationName(
        val qualifier: String,
        val organization: String,
        val name: String,
    ) : AppInfo {
        override val id: String = "$qualifier.$organization.$name"

        init {
            require(qualifier.isNotBlank()) { "应用限定符不能为空" }
            require(organization.isNotBlank()) { "app.organization 不能为空" }
            require(name.isNotBlank()) { "app.name 不能为空" }
        }
    }
}

/**
 * 桌面应用数据目录解析器。
 */
interface AppFolderResolver {
    /**
     * 按当前系统约定解析应用的数据目录与缓存目录。
     */
    fun resolve(appInfo: AppInfo): AppDataDirectories

    /**
     * 当前桌面平台默认使用的目录解析器。
     */
    companion object : AppFolderResolver by when (currentPlatformDesktop()) {
        is Platform.Linux -> UnixAppFolderResolver
        is Platform.MacOS -> UnixAppFolderResolver
        is Platform.Windows -> WindowsAppFolderResolver
    }
}

/**
 * Windows 桌面应用目录解析器。
 */
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

/**
 * Unix-like 桌面应用目录解析器。
 */
object UnixAppFolderResolver : AppFolderResolver {
    override fun resolve(appInfo: AppInfo): AppDataDirectories {
        return ProjectDirectoriesAppFolderResolver.resolve(appInfo)
    }
}
