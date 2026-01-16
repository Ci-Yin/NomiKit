package ciyin.platform

import android.os.Environment
import androidx.compose.runtime.ProvidableCompositionLocal
import ciyin.io.File
import ciyin.platform.context.ContextFiles

actual typealias Context = android.content.Context

actual val LocalContext: ProvidableCompositionLocal<Context>
    get() = androidx.compose.ui.platform.LocalContext


actual val Context.files: ContextFiles get() = AndroidContextFiles(this)

internal class AndroidContextFiles(context: android.content.Context) : ContextFiles {
    override val cacheDir: File = File(context.cacheDir.path ?: "")
    override val dataDir: File = File(context.filesDir.path ?: "") // can be null when previewing
    override val defaultBaseMediaCacheDir: File =
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path ?: "")
}