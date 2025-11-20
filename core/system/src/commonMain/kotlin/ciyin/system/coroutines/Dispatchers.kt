package ciyin.system.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

expect val Dispatchers.IO: CoroutineDispatcher

expect fun <T> runBlockingCrossPlatform(
    context: CoroutineContext = Dispatchers.Default,
    block: suspend CoroutineScope.() -> T
): T