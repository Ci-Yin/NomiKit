package com.ciyin.app.util

import ciyin.system.coroutines.runBlockingCrossPlatform
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * 获取字符串资源的值。
 *
 * 该属性通过异步方式获取当前字符串资源对象对应的字符串值。
 * 使用时，它会阻塞当前协程直到字符串被成功获取。
 */
val StringResource.value get() = runBlockingCrossPlatform { getString(this@value) }
