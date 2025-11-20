package com.ciyin.app.util

import ciyin.io.File
import ciyin.io.resolve
import ciyin.platform.platform

object FilePath {

    val AppDataDir = platform.getAppDataDir()

    val GamaDataFile = AppDataDir.resolve("game_data.json")

    val IconsDir = AppDataDir.resolve("icons")

    val ConfigDataFile = AppDataDir.resolve("config.json")

    val RuntimeDataDir = AppDataDir.resolve("runtime_data")

    val ScriptProjectDir = File("D:\\Studio\\KotlinProjects")

}