package com.yy.myuko.core.testing

/**
 * 标记一个函数, 表示它是一个 sample. (KDoc 里的 `@sample` 标签)
 * 用来 suppress unused.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Sample
