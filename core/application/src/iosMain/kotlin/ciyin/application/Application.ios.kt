package ciyin.application

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import androidx.compose.ui.window.ComposeUIViewController
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

fun runApplication(
    createApplication: (context: Context) -> MultiplatformApplication,
    configure: ComposeUIViewControllerConfiguration.() -> Unit = {},
    content: @Composable () -> Unit
) = ComposeUIViewController(
    configure = configure,
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


private fun desktopContext(): IosContext {
    val dataDir = SystemSupportDir.apply { mkdirs() }
    val files = CommonContextFiles(
        cacheDir = SystemCacheDir.apply { mkdirs() },
        dataDir = dataDir,
        defaultBaseMediaCacheDir = dataDir
    )
    return IosContext(files)
}

@OptIn(ExperimentalForeignApi::class)
private val SystemDocumentDir by lazy {
    NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory.convert(),
        NSUserDomainMask.convert(),
        true
    ).firstOrNull()?.toString()?.toFile() ?: error("Cannot get SystemDocumentDir")

}

@OptIn(ExperimentalForeignApi::class)
private val SystemSupportDir by lazy {
    NSSearchPathForDirectoriesInDomains(
        NSApplicationSupportDirectory.convert(),
        NSUserDomainMask.convert(),
        true
    ).firstOrNull()?.toString()?.toFile() ?: error("Cannot get SystemSupportDir")
}

@OptIn(ExperimentalForeignApi::class)
private val SystemCacheDir by lazy {
    NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory.convert(),
        NSUserDomainMask.convert(),
        true
    ).firstOrNull()?.toString()?.toFile() ?: error("Cannot get SystemCacheDir")
}
