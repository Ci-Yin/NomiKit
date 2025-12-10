package com.ciyin.app.ui.app.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRouter

@Serializable
object MainRouter : NavRouter

@Serializable
object SettingsRouter : NavRouter
