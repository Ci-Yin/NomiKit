package ciyin.platform

import androidx.compose.runtime.ProvidableCompositionLocal
import ciyin.platform.context.ContextFiles

/**
 * 平台 Context
 */
expect abstract class Context()

expect val LocalContext: ProvidableCompositionLocal<Context>

expect val Context.files: ContextFiles
