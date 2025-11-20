package com.yy.myuko.component.koin.ciyin.koin

import kotlinx.serialization.MetaSerializable

@MetaSerializable
@Target(AnnotationTarget.CLASS)
annotation class KoinPropInstance(val preKey: String = "")
