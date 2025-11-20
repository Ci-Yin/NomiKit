package com.yy.myuko.core.testing
//
//import com.yy.myuko.core.coroutines.OwnedCancellationException
//import com.yy.myuko.core.coroutines.checkOwner
//import kotlinx.coroutines.*
//
//
//suspend inline fun assertCoroutineSuspends(crossinline block: suspend () -> Unit) {
//    var suspended = false
//    val owner = Any()
//    try {
//        coroutineScope {
//            val parent = this
//            launch(start = CoroutineStart.UNDISPATCHED) {
//                yield()
//                suspended = true
//                parent.cancel(OwnedCancellationException(owner))
//            }
//
//            launch(start = CoroutineStart.UNDISPATCHED) {
//                block()
//            }
//        }
//    } catch (e: OwnedCancellationException) {
//        e.checkOwner(owner)
//    }
//    if (!suspended) {
//        throw AssertionError("Expected coroutine suspends, but it did not suspend")
//    }
//}
//
//suspend inline fun assertCoroutineNotSuspend(crossinline block: suspend () -> Unit) {
//    coroutineScope {
//        val job = launch {
//            yield()
//            throw AssertionError("Expected coroutine does not suspend, but it did suspend")
//        }
//
//        block()
//        job.cancel()
//    }
//}