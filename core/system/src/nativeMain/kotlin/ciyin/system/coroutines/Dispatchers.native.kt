package ciyin.system.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

actual val Dispatchers.IO: CoroutineDispatcher get() = Dispatchers.Default
actual fun <T> runBlockingCrossPlatform(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T = runBlocking(context, block)