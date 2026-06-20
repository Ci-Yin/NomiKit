package ciyin.application

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import ciyin.application.config.AppBuildConfig
import ciyin.application.config.LocalAppBuildConfig
import ciyin.application.config.toDesktopAppInfo
import ciyin.io.resolve
import ciyin.io.toFile
import ciyin.platform.Context
import ciyin.platform.DesktopContext
import ciyin.platform.LocalContext
import ciyin.platform.context.CommonContextFiles
import ciyin.system.storage.AppFolderResolver
import ciyin.system.storage.AppInfo

/**
 * 启动 Compose Desktop 应用，并注入桌面平台上下文。
 *
 * @param createApplication 根据平台上下文创建跨平台应用实例
 * @param exitProcessOnExit 关闭应用时是否退出进程
 * @param appBuildConfig 当前平台注入的应用构建配置
 * @param appInfo 用于解析桌面数据目录的应用身份
 * @param content Compose Desktop 根内容
 */
fun runApplication(
    createApplication: (context: Context) -> MultiplatformApplication,
    exitProcessOnExit: Boolean = true,
    appBuildConfig: AppBuildConfig = AppBuildConfig,
    appInfo: AppInfo = appBuildConfig.toDesktopAppInfo(),
    content: @Composable (ApplicationScope.() -> Unit)
) = application(
    exitProcessOnExit = exitProcessOnExit,
) {
    val context = desktopContext(appInfo)
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
 * 根据应用身份创建桌面平台上下文。
 */
private fun desktopContext(appInfo: AppInfo): DesktopContext {

    val projectDirectories = AppFolderResolver.resolve(appInfo)
    val dataDir = projectDirectories.data.toString().toFile()
    val cacheDir = projectDirectories.cache.toString().toFile()

    val files = CommonContextFiles(
        dataDir = dataDir,
        cacheDir = cacheDir,
        defaultBaseMediaCacheDir = dataDir.resolve("media-downloads")
    )
    return DesktopContext(files)
}
