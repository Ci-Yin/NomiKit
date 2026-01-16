package ciyin.platform

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import ciyin.platform.context.ContextFiles

actual abstract class Context

actual val LocalContext: ProvidableCompositionLocal<Context> = compositionLocalOf {
    error("No Context provided")
}

class IosContext(val contextFiles: ContextFiles) : Context()

fun Context.asIosContext(): IosContext {
    return this as IosContext
}

actual val Context.files: ContextFiles get() = asIosContext().contextFiles

