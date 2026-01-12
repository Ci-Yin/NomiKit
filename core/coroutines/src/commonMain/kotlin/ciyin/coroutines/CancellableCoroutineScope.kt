package ciyin.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * A [coroutineScope] that can be [canceled][CancellableCoroutineScope.cancelScope]
 * without causing the [coroutineScope] to throw a [CancellationException].
 */
suspend inline fun <R> cancellableCoroutineScope(
    onCancel: () -> R,
    crossinline block: suspend CancellableCoroutineScope.() -> R
): R {
    val owner = Any()
    return try {
        coroutineScope {
            val self = this
            block(
                object : CancellableCoroutineScope {
                    override fun cancelScope() {
                        self.cancel(OwnedCancellationException(owner))
                    }

                    override val coroutineContext: CoroutineContext = self.coroutineContext
                },
            )
        }
    } catch (e: OwnedCancellationException) {
        e.checkOwner(owner)
        onCancel()
    }
}

/**
 * A [coroutineScope] that can be [cancelled][CancellableCoroutineScope.cancelScope]
 * without causing the [coroutineScope] to throw a [CancellationException].
 */
suspend inline fun <R> cancellableCoroutineScope(
    crossinline block: suspend CancellableCoroutineScope.() -> R
): R? {
    return cancellableCoroutineScope(
        onCancel = { null },
        block = block,
    )
}

interface CancellableCoroutineScope : CoroutineScope {
    fun cancelScope()
}


class OwnedCancellationException(val owner: Any) : CancellationException("Aborted by $owner")

fun OwnedCancellationException.checkOwner(owner: Any) {
    if (this.owner !== owner) throw this
}
