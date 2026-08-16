package ciyin.video.player

import org.openani.mediamp.MediampPlayerFactoryLoader
import org.openani.mediamp.compose.MediampPlayerSurfaceProviderLoader
import org.openani.mediamp.vlc.VlcMediampPlayerFactory
import org.openani.mediamp.vlc.compose.VlcMediampPlayerSurfaceProvider
import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider
import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryProviderPriority
import java.io.File

/** Desktop VLC 原生库的外部配置入口。 */
object VlcRuntimeConfiguration {
    /** 显式指定 VLC 原生库目录的 JVM 系统属性。 */
    const val LibraryPathSystemProperty: String = "ciyin.video.player.vlc.library.path"

    /** 显式指定 VLC 原生库目录的环境变量。 */
    const val LibraryPathEnvironmentVariable: String = "CIYIN_VIDEO_PLAYER_VLC_LIBRARY_PATH"
}

/** 从外部配置、Compose Desktop 打包资源中发现 VLC 原生库。 */
class VlcDiscoveryDirectoryProvider : DiscoveryDirectoryProvider {
    init {
        registerVlcProviders()
    }

    /** 返回用户目录发现优先级。 */
    override fun priority(): Int = DiscoveryProviderPriority.USER_DIR

    /** 返回按优先级排列且实际存在的 VLC 动态库目录。 */
    override fun directories(): Array<String> = resolveVlcLibraryDirectories(
        systemPropertyPath = System.getProperty(VlcRuntimeConfiguration.LibraryPathSystemProperty),
        environmentPath = System.getenv(VlcRuntimeConfiguration.LibraryPathEnvironmentVariable),
        composeResourcesPath = System.getProperty(ComposeResourcesDirectoryProperty),
    ).map(File::getAbsolutePath).toTypedArray()

    /** 始终允许 VLCJ 尝试该发现方式。 */
    override fun supported(): Boolean = true
}

/** 解析按外部配置优先的有效 VLC 原生库目录。 */
internal fun resolveVlcLibraryDirectories(
    systemPropertyPath: String?,
    environmentPath: String?,
    composeResourcesPath: String?,
): List<File> = listOfNotNull(
    systemPropertyPath,
    environmentPath,
    composeResourcesPath?.let { File(it).resolve(ComposeResourcesLibraryDirectory).path },
).map(String::trim)
    .filter(String::isNotEmpty)
    .map(::File)
    .map(File::getAbsoluteFile)
    .filter(File::isDirectory)
    .distinctBy(File::getAbsolutePath)

/** 注册 Mediamp 的 VLC 播放器与渲染表面实现。 */
private fun registerVlcProviders() {
    MediampPlayerFactoryLoader.register(VlcMediampPlayerFactory())
    MediampPlayerSurfaceProviderLoader.register(VlcMediampPlayerSurfaceProvider())
}

/** Compose Desktop 注入的应用资源目录系统属性。 */
private const val ComposeResourcesDirectoryProperty = "compose.application.resources.dir"

/** Compose Desktop 分发资源中的 VLC 子目录。 */
private const val ComposeResourcesLibraryDirectory = "lib"
