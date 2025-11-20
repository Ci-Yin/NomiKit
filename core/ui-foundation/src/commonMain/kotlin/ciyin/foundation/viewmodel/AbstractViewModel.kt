package ciyin.foundation.viewmodel

import androidx.annotation.CallSuper
import androidx.compose.runtime.RememberObserver
import androidx.lifecycle.ViewModel
import ciyin.platform.thisLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * 带有 [backgroundScope], 当 [AbstractViewModel] 被 forget 时自动 close scope 以防资源泄露.
 *
 * 因此 [AbstractViewModel] 需要与 compose remember 一起使用, 否则需手动管理生命周期.
 * 在构造 [AbstractViewModel] 时需要考虑其声明周期问题.
 */ // We can't use Android's Viewmodel because it's not available in Desktop platforms.
abstract class AbstractViewModel : RememberObserver, ViewModel(), HasBackgroundScope {

    protected val logger by lazy { thisLogger() }

    private var _backgroundScope = createBackgroundScope()
    override val backgroundScope: CoroutineScope
        get() {
            return _backgroundScope
        }


    private var referenceCount = 0

    @CallSuper
    override fun onAbandoned() {
        referenceCount--
    }

    @CallSuper
    override fun onForgotten() {
        referenceCount--
    }

    @CallSuper
    override fun onRemembered() {
        referenceCount++
        logger.v { "${this::class.simpleName} onRemembered, refCount=$referenceCount" }
        if (referenceCount == 1) {
            this.init() // first remember
        }
    }

    private fun createBackgroundScope(): CoroutineScope {
        return CoroutineScope(
            CoroutineExceptionHandler(::onBackgroundScopeException)
                    + SupervisorJob(),
        )
    }

    /**
     * Called when the view model is remembered the first time.
     */
    protected open fun init() {
    }

    protected open fun onBackgroundScopeException(
        coroutineContext: CoroutineContext,
        throwable: Throwable
    ) {
        logger.e(throwable) { "Unhandled exception in background scope for viewmodel ${this::class.qualifiedName}, coroutineContext: $coroutineContext" }
    }

    override fun onCleared() {
        backgroundScope.cancel()
        super.onCleared()
    }
}