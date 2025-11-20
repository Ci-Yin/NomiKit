package ciyin.platform

import co.touchlab.kermit.Logger

inline fun <reified T> logger(): Logger = logger(tag = T::class.simpleName ?: "logger")

fun Any.thisLogger(): Logger = logger(tag = this::class.simpleName ?: "ThisLogger")

fun logger(tag: String): Logger = Logger.withTag(tag)

fun log(vararg logs: Any?) = Log.debug("日志打印", *logs)
