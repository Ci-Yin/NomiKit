package ciyin.platform

import android.app.Application

actual typealias Context = android.content.Context

private var _ApplicationContext: Context? = null

val ApplicationContext: Context
    get() = _ApplicationContext ?: error("Application context not initialized")

fun initApplicationContext(context: Context) {
    _ApplicationContext = context
}

actual object EmptyContext : Application()
