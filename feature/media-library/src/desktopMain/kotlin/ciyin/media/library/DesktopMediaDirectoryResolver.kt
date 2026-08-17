package ciyin.media.library

import ciyin.io.File
import com.sun.jna.platform.win32.KnownFolders
import com.sun.jna.platform.win32.Shell32Util
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/** 解析 Desktop 平台用户标准媒体目录。 */
internal fun interface DesktopMediaDirectoryResolver {
    /** 返回指定媒体分类对应的用户标准目录。 */
    fun resolve(collection: MediaCollection): File
}

/** 使用操作系统标准位置解析用户媒体目录。 */
internal object DefaultDesktopMediaDirectoryResolver : DesktopMediaDirectoryResolver {
    /** 按当前 JVM 操作系统解析目标目录。 */
    override fun resolve(collection: MediaCollection): File {
        val path = when {
            isWindows() -> windowsDirectory(collection)
            isLinux() -> linuxDirectory(collection)
            isMacOs() -> macOsDirectory(collection)
            else -> fallbackDirectory(collection)
        }
        return File(path.toAbsolutePath().normalize().toString())
    }

    /** 使用 Windows Known Folder API 解析目录，API 失败时回退到用户目录。 */
    private fun windowsDirectory(collection: MediaCollection): Path = runCatching {
        val folderId = when (collection) {
            MediaCollection.Images -> KnownFolders.FOLDERID_Pictures
            MediaCollection.Videos -> KnownFolders.FOLDERID_Videos
            MediaCollection.Audio -> KnownFolders.FOLDERID_Music
            MediaCollection.Downloads -> KnownFolders.FOLDERID_Downloads
        }
        Paths.get(Shell32Util.getKnownFolderPath(folderId))
    }.getOrElse {
        fallbackDirectory(collection)
    }

    /** 解析 Linux XDG user-dirs 配置，配置缺失时回退到约定目录。 */
    private fun linuxDirectory(collection: MediaCollection): Path {
        val key = when (collection) {
            MediaCollection.Images -> "XDG_PICTURES_DIR"
            MediaCollection.Videos -> "XDG_VIDEOS_DIR"
            MediaCollection.Audio -> "XDG_MUSIC_DIR"
            MediaCollection.Downloads -> "XDG_DOWNLOAD_DIR"
        }
        val configured = readXdgUserDirectories()[key] ?: return fallbackDirectory(collection)
        return Paths.get(configured)
    }

    /** 返回 macOS 用户标准媒体目录。 */
    private fun macOsDirectory(collection: MediaCollection): Path {
        val directoryName = when (collection) {
            MediaCollection.Images -> "Pictures"
            MediaCollection.Videos -> "Movies"
            MediaCollection.Audio -> "Music"
            MediaCollection.Downloads -> "Downloads"
        }
        return userHome().resolve(directoryName)
    }

    /** 返回各平台标准 API 不可用时的显式用户目录回退。 */
    private fun fallbackDirectory(collection: MediaCollection): Path {
        val directoryName = when (collection) {
            MediaCollection.Images -> "Pictures"
            MediaCollection.Videos -> "Videos"
            MediaCollection.Audio -> "Music"
            MediaCollection.Downloads -> "Downloads"
        }
        return userHome().resolve(directoryName)
    }

    /** 读取并展开当前用户的 XDG user-dirs 配置。 */
    private fun readXdgUserDirectories(): Map<String, String> {
        val configHome = System.getenv("XDG_CONFIG_HOME")
            ?.takeIf(String::isNotBlank)
            ?.let(Paths::get)
            ?: userHome().resolve(".config")
        val configFile = configHome.resolve("user-dirs.dirs")
        if (!Files.isRegularFile(configFile)) return emptyMap()

        return runCatching {
            Files.readAllLines(configFile).mapNotNull(::parseXdgUserDirectory).toMap()
        }.getOrDefault(emptyMap())
    }

    /** 解析一行 XDG user-dirs 配置。 */
    private fun parseXdgUserDirectory(line: String): Pair<String, String>? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith('#')) return null
        val separator = trimmed.indexOf('=')
        if (separator <= 0) return null
        val key = trimmed.substring(0, separator).trim()
        val rawValue = trimmed.substring(separator + 1).trim().removeSurrounding("\"")
        if (!key.startsWith("XDG_") || rawValue.isBlank()) return null
        val home = userHome().toString()
        val expanded = rawValue
            .replace("${'$'}{HOME}", home)
            .replace("${'$'}HOME", home)
        val path = Paths.get(expanded)
        return key to if (path.isAbsolute) path.normalize().toString() else userHome().resolve(path).toString()
    }

    /** 返回当前用户主目录。 */
    private fun userHome(): Path {
        val value = System.getProperty("user.home")?.takeIf(String::isNotBlank)
            ?: MediaLibraryError.Unsupported("无法解析当前用户主目录").raise()
        return Paths.get(value).toAbsolutePath().normalize()
    }

    /** 判断当前 JVM 是否运行在 Windows。 */
    private fun isWindows(): Boolean = osName().startsWith("windows")

    /** 判断当前 JVM 是否运行在 Linux。 */
    private fun isLinux(): Boolean = osName().startsWith("linux")

    /** 判断当前 JVM 是否运行在 macOS。 */
    private fun isMacOs(): Boolean = osName().startsWith("mac")

    /** 返回用于平台匹配的小写操作系统名称。 */
    private fun osName(): String = System.getProperty("os.name").orEmpty().lowercase()
}
