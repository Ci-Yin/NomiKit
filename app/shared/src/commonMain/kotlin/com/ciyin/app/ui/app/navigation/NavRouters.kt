package com.ciyin.app.ui.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRouter : NavKey

@Serializable
object MainRouter : NavRouter

@Serializable
object SettingRouter : NavRouter
