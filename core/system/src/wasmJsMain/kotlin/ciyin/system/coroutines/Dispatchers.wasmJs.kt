package ciyin.system.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.coroutines.CoroutineContext

/**
 * Wasm 平台 runBlocking 模拟
 *
 * WebAssembly 无法阻塞线程，因此只能通过 async 启动协程并立即返回 Promise。
 */
@OptIn(DelicateCoroutinesApi::class, ExperimentalWasmJsInterop::class)
actual fun <T> runBlockingCrossPlatform(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T {
    val promise = GlobalScope.promise { block() }
    return promise.toJsReference() as T
}