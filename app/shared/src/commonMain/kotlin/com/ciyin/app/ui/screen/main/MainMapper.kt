package com.ciyin.app.ui.screen.main

import com.ciyin.app.data.project.model.GameProject


fun ProjectUiModel.toGameProject() = GameProject(
    id = id,
    type = type,
    name = name,
    selection = selection,
    gameConfig = gameConfig,
    packageName = packageName,
    jarPath = jarPath,
    scriptProjectClass = scriptProjectClass,
    icon = icon,
    games = games,
)

fun GameProject.toProjectUiModel() = ProjectUiModel(
    id = id,
    type = type,
    name = name,
    selection = selection,
    gameConfig = gameConfig,
    packageName = packageName,
    jarPath = jarPath,
    scriptProjectClass = scriptProjectClass,
    icon = icon,
    games = games,
)