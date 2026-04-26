package com.ciyin.app.ui.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface NavRouter : NavKey

@Serializable
object MainRouter : NavRouter

@Serializable
object SettingRouter : NavRouter

@Serializable
object AiImageDemoRouter : NavRouter


val NavSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MainRouter::class, MainRouter.serializer())
            subclass(SettingRouter::class, SettingRouter.serializer())
            subclass(AiImageDemoRouter::class, AiImageDemoRouter.serializer())
        }
    }
}
