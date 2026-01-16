package ciyin.application

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import ciyin.io.resolve
import ciyin.io.toFile
import ciyin.platform.Context
import ciyin.platform.DesktopContext
import ciyin.platform.LocalContext
import ciyin.platform.context.CommonContextFiles
import ciyin.system.storage.AppFolderResolver
import ciyin.system.storage.AppInfo

fun runApplication(
    createApplication: (context: Context) -> MultiplatformApplication,
    exitProcessOnExit: Boolean = true,
    content: @Composable (ApplicationScope.() -> Unit)
) = application(
    exitProcessOnExit = exitProcessOnExit,
) {
    val context = desktopContext()
    val application = createApplication(context)
    application.onCreate()
    CompositionLocalProvider(
        LocalContext provides context,
    ) {
        content()
    }
}

private fun desktopContext(): DesktopContext {

    val projectDirectories = AppFolderResolver.resolve(
        AppInfo(
            "com",
            "ciyin",
            "NomiKit",
        ),
    )
    val dataDir = projectDirectories.data.toString().toFile()
    val cacheDir = projectDirectories.cache.toString().toFile()

    val files = CommonContextFiles(
        dataDir = dataDir,
        cacheDir = cacheDir,
        defaultBaseMediaCacheDir = dataDir.resolve("media-downloads")
    )
    return DesktopContext(files)
}
