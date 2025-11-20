package ciyin.system.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.coroutines.CoroutineContext

/**
 * JS 平台无法阻塞线程，因此使用 Promise + 协程异步执行。
 * 注意：返回值在 JS 中为 Promise<T>
 */
@OptIn(DelicateCoroutinesApi::class, ExperimentalWasmJsInterop::class)
actual fun <T> runBlockingCrossPlatform(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T {
//    // Kotlin/JS 不支持真正阻塞，只能返回动态 Promise
//    val promise = GlobalScope.promise { block() }
//    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
//    return promise.unsafeCast<T>() // 运行时其实是 Promise<T>
    val promise = GlobalScope.promise { block() }
    return promise.get()
}