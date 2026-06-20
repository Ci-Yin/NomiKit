package ciyin.application

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import androidx.compose.ui.window.ComposeUIViewController
import ciyin.application.config.AppBuildConfig
import ciyin.application.config.LocalAppBuildConfig
import ciyin.io.toFile
import ciyin.platform.Context
import ciyin.platform.IosContext
import ciyin.platform.LocalContext
import ciyin.platform.context.CommonContextFiles
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * 启动 Compose iOS 应用，并注入 iOS 平台上下文。
 *
 * @param createApplication 根据平台上下文创建跨平台应用实例
 * @param configure iOS Compose 容器配置
 * @param appBuildConfig 当前平台注入的应用构建配置
 * @param content Compose 根内容
 */
fun runApplication(
    createApplication: (context: Context) -> MultiplatformApplication,
    configure: ComposeUIViewControllerConfiguration.() -> Unit = {},
    appBuildConfig: AppBuildConfig = AppBuildConfig,
    content: @Composable () -> Unit
) = ComposeUIViewController(
    configure = configure,
) {
    val context = desktopContext()
    val application = createApplication(context)
    application.onCreate()
    CompositionLocalProvider(
        LocalContext provides context,
        LocalAppBuildConfig provides appBuildConfig,
    ) {
        content()
    }
}


/**
 * 根据 iOS 系统目录创建平台上下文。
 */
private fun desktopContext(): IosContext {
    val dataDir = SystemSupportDir.apply { mkdirs() }
    val files = CommonContextFiles(
        cacheDir = SystemCacheDir.apply { mkdirs() },
        dataDir = dataDir,
        defaultBaseMediaCacheDir = dataDir
    )
    return IosContext(files)
}

/**
 * iOS 文档目录。
 */
@OptIn(ExperimentalForeignApi::class)
private val SystemDocumentDir by lazy {
    NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory.convert(),
        NSUserDomainMask.convert(),
        true
    ).firstOrNull()?.toString()?.toFile() ?: error("Cannot get SystemDocumentDir")

}

/**
 * iOS Application Support 目录。
 */
@OptIn(ExperimentalForeignApi::class)
private val SystemSupportDir by lazy {
    NSSearchPathForDirectoriesInDomains(
        NSApplicationSupportDirectory.convert(),
        NSUserDomainMask.convert(),
        true
    ).firstOrNull()?.toString()?.toFile() ?: error("Cannot get SystemSupportDir")
}

/**
 * iOS 缓存目录。
 */
@OptIn(ExperimentalForeignApi::class)
private val SystemCacheDir by lazy {
    NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory.convert(),
        NSUserDomainMask.convert(),
        true
    ).firstOrNull()?.toString()?.toFile() ?: error("Cannot get SystemCacheDir")
}
