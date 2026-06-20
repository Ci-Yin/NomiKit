package ciyin.system.storage

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.win32.W32APIOptions
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal object WindowsJnaAppFolderResolver : AppFolderResolver {

    // 2) JNA interface to shell32.dll
    private interface Shell32 : Library {
        /**
         * SHGetFolderPathW:
         * https://learn.microsoft.com/en-us/windows/win32/api/shlobj_core/nf-shlobj_core-shgetfolderpathw
         */
        fun SHGetFolderPathW(
            hwndOwner: Pointer?,
            nFolder: Int,
            hToken: Pointer?,
            dwFlags: Int,
            pszPath: CharArray?
        ): Int

        companion object {
            val INSTANCE: Shell32 = Native.load(
                "shell32",
                Shell32::class.java,
                W32APIOptions.DEFAULT_OPTIONS,
            )
        }
    }

    // 3) Constants for folder identifiers:
    private const val CSIDL_APPDATA = 0x001A       // Roaming app data folder
    private const val CSIDL_LOCAL_APPDATA = 0x001C // Local app data folder
    private const val MAX_PATH = 260              // Typical Windows MAX_PATH

    /**
     * 4) Retrieve the Roaming AppData folder and append subdirectories.
     */
    private fun getRoamingAppDataDirectory(
        appInfo: AppInfo,
    ): Path {
        val pathBuffer = CharArray(MAX_PATH)
        val result = Shell32.INSTANCE.SHGetFolderPathW(null, CSIDL_APPDATA, null, 0, pathBuffer)

        val appDataPath = if (result == 0) {
            // Success: convert returned buffer to String
            Native.toString(pathBuffer)
        } else {
            // Fallback to %APPDATA%
            System.getenv("APPDATA")
                ?: throw RuntimeException("Failed to retrieve APPDATA. SHGetFolderPath error code: $result")
        }

        val targetDir = appInfo.resolveWindowsAppDirectory(Paths.get(appDataPath))
        ensureDirectoriesExist(targetDir)
        return targetDir
    }

    /**
     * 5) Retrieve the Local AppData folder and append subdirectories.
     */
    private fun getLocalAppDataDirectory(
        appInfo: AppInfo,
    ): Path {
        val pathBuffer = CharArray(MAX_PATH)
        val result =
            Shell32.INSTANCE.SHGetFolderPathW(null, CSIDL_LOCAL_APPDATA, null, 0, pathBuffer)

        val localAppDataPath = if (result == 0) {
            // Success
            Native.toString(pathBuffer)
        } else {
            // Fallback to %LOCALAPPDATA%
            System.getenv("LOCALAPPDATA")
                ?: throw RuntimeException("Failed to retrieve LOCALAPPDATA. SHGetFolderPath error code: $result")
        }

        val targetDir = appInfo.resolveWindowsAppDirectory(Paths.get(localAppDataPath))
        ensureDirectoriesExist(targetDir)
        return targetDir
    }

    /**
     * 6) Public function that returns a data class holding both paths.
     */
    @JvmStatic
    fun getAppDataDirectories(
        appInfo: AppInfo,
    ): AppDataDirectories {
        val roamingDir = getRoamingAppDataDirectory(appInfo)
        val localDir = getLocalAppDataDirectory(appInfo)
        return AppDataDirectories(roamingDir.resolve("data"), localDir.resolve("cache"))
    }

    /**
     * Helper function to create directories if they don't already exist.
     */
    private fun ensureDirectoriesExist(dir: Path) {
        if (Files.notExists(dir)) {
            try {
                Files.createDirectories(dir)
            } catch (e: Exception) {
                throw RuntimeException("Failed to create or access directory: $dir", e)
            }
        }
    }

    override fun resolve(appInfo: AppInfo): AppDataDirectories =
        getAppDataDirectories(appInfo)
}

/**
 * 根据应用身份拼出 Windows 下的应用根目录。
 */
internal fun AppInfo.resolveWindowsAppDirectory(baseDir: Path): Path =
    when (this) {
        is AppInfo.ApplicationId -> baseDir.resolve(id)
        is AppInfo.OrganizationName -> baseDir.resolve(organization).resolve(name)
    }
