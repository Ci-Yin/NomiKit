package ciyin.platform

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import ciyin.platform.context.ContextFiles

actual abstract class Context

actual val LocalContext: ProvidableCompositionLocal<Context> = compositionLocalOf {
    error("No Context provided")
}

class DesktopContext(val contextFiles: ContextFiles) : Context()

fun Context.asDesktopContext(): DesktopContext {
    return this as DesktopContext
}

actual val Context.files: ContextFiles get() = asDesktopContext().contextFiles
