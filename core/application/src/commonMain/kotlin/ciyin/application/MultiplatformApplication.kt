package ciyin.application

import androidx.annotation.CallSuper
import ciyin.platform.Context

/**
 * 通用 Application 抽象类。
 *
 * 为多平台（Android, Desktop, iOS, Wasm等）提供统一的应用程序生命周期管理。
 * 子类可以继承此类来实现特定平台的初始化和清理逻辑。
 */
interface MultiplatformApplication {

    /** 当前平台上下文 */
    val context: Context

    /** 应用初始化入口（类似 Android 的 onCreate） */
    @CallSuper
    fun onCreate()

    /** 应用关闭或销毁时调用 */
    @CallSuper
    fun onDestroy()

}