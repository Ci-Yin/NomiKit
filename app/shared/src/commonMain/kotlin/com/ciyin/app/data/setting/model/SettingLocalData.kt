package com.ciyin.app.data.setting.model

import ciyin.io.File
import ciyin.platform.platform
import kotlinx.serialization.Serializable

@Serializable
data class SettingLocalData(
    val isStartup: Boolean = false,
    val startInTray: Boolean = false,
    val jdkDir: String = platform.getJavaHome(),
    val windowsDriverPath: String = "",
    val webDriverPath: String = "",
)

val SettingLocalData.java get() = "$jdkDir${File.separator}bin${File.separator}java"
