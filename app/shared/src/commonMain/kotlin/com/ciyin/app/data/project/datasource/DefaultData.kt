package com.ciyin.app.data.project.datasource

import androidx.compose.runtime.mutableStateListOf
import com.ciyin.app.data.project.model.GAME_TYPE_GENSHIN
import com.ciyin.app.data.project.model.GAME_TYPE_HONKAI
import com.ciyin.app.data.project.model.GAME_TYPE_STARRAIL
import com.ciyin.app.data.project.model.GAME_TYPE_ZENLESS_ZONE_ZERO
import com.ciyin.app.data.project.model.Game
import com.ciyin.app.data.project.model.GameProject
import com.ciyin.app.data.project.model.Task
import com.ciyin.app.data.project.model.taskChild

val defaultGame = Game(id = 0, type = -1, name = "", preset = "", tasks = mutableStateListOf())

val defaultGameProjects
    get() = mutableStateListOf(
        GameProject(
            id = 0,
            type = GAME_TYPE_GENSHIN,
            name = "原神",
            games = mutableStateListOf(
                defaultGenshinGame
            ),
        ),
        GameProject(
            id = 1,
            type = GAME_TYPE_STARRAIL,
            name = "崩坏-星穹铁道",
            games = mutableStateListOf(
                defaultStarRailGame
            ),
        ),
        GameProject(
            id = 2,
            type = GAME_TYPE_HONKAI,
            name = "崩坏3",
            games = mutableStateListOf(
                defaultHonkaiGame
            ),
        ),
        GameProject(
            id = 3,
            type = GAME_TYPE_ZENLESS_ZONE_ZERO,
            name = "绝区零",
            games = mutableStateListOf(
                defaultZenlessZoneZeroGame
            ),
        )
    )

val defaultGenshinGame: Game
    get() = Game(
        id = GAME_TYPE_GENSHIN,
        type = GAME_TYPE_GENSHIN,
        name = "原神",
        preset = "默认",
        tasks = mutableStateListOf(
            Task(
                id = Task.Companion.TASK_ID_LOGIN,
                name = "登录游戏"
            ),
            Task(
                id = Task.Companion.TASK_ID_CLOSE,
                name = "关闭游戏"
            ),
        )
    )

val defaultHonkaiGame: Game
    get() = Game(
        id = GAME_TYPE_HONKAI,
        type = GAME_TYPE_HONKAI,
        name = "崩坏3",
        preset = "默认",
        tasks = mutableStateListOf(
            Task(
                id = Task.Companion.TASK_ID_LOGIN,
                name = "登录游戏"
            ),
            Task(
                id = Task.Companion.TASK_ID_CLOSE,
                name = "关闭游戏"
            ),
        )
    )

val defaultZenlessZoneZeroGame: Game
    get() = Game(
        id = GAME_TYPE_ZENLESS_ZONE_ZERO,
        type = GAME_TYPE_ZENLESS_ZONE_ZERO,
        name = "绝区零",
        preset = "默认",
        tasks = mutableStateListOf(
            Task(
                id = Task.Companion.TASK_ID_LOGIN,
                name = "登录游戏"
            ),
            Task(
                id = Task.Companion.TASK_ID_CLOSE,
                name = "关闭游戏"
            )
        )
    )


val defaultStarRailGame: Game
    get() = Game(
        id = GAME_TYPE_STARRAIL,
        type = GAME_TYPE_STARRAIL,
        name = "崩坏-星穹铁道",
        preset = "默认",
        tasks = mutableStateListOf(
            Task(
                id = Task.Companion.TASK_ID_LOGIN,
                name = "登录游戏"
            ),
            Task(
                id = Task.Companion.TASK_ID_CALYX_GOLDEN,
                name = "拟造花萼(金)",
                subName = "经验材料/信用点",
                params = mutableStateListOf(
                    taskChild("回忆之蕾"),
                    taskChild("以太之蕾"),
                    taskChild("藏珍之蕾"),
                )
            ),
            Task(
                id = Task.Companion.TASK_ID_CALYX_CRIMSON,
                name = "拟造花萼(赤)",
                subName = "行迹材料",
                params = mutableStateListOf(
                    taskChild("培养目标", "之蕾"),
                    taskChild("丰饶之蕾"),
                    taskChild("毁灭之蕾"),
                    taskChild("存护之蕾"),
                    taskChild("巡猎之蕾"),
                    taskChild("智识之蕾"),
                    taskChild("虚空之蕾"),
                    taskChild("同谐之蕾"),
                    taskChild("虚无之蕾"),
                )
            ),
            Task(
                id = Task.Companion.TASK_ID_STAGNANT_SHADOW,
                name = "凝滞虚影",
                subName = "角色晋阶材料",
                params = mutableStateListOf(
                    taskChild("培养目标", "之形", count = null),
                    taskChild("嗔怒之形", count = null),
                    taskChild("燔灼之形", count = null),
                    taskChild("炎华之形", count = null),
                    taskChild("幽府之形", count = null),
                    taskChild("锋芒之形", count = null),
                    taskChild("冰棱之形", count = null),
                    taskChild("霜晶之形", count = null),
                    taskChild("震厄之形", count = null),
                    taskChild("鸣雷之形", count = null),
                    taskChild("天人之形", count = null),
                    taskChild("巽风之形", count = null),
                    taskChild("孽兽之形", count = null),
                    taskChild("空海之形", count = null),
                    taskChild("偃偶之形", count = null),
                    taskChild("幻光之形", count = null),
                )
            ),
            Task(
                id = Task.Companion.TASK_ID_CAVERN_OF_CORROSION,
                name = "侵蚀隧洞",
                subName = "遗器",
                params = mutableStateListOf(
                    taskChild("培养目标", "之径", count = null),
                    taskChild("幽冥之径", count = null),
                    taskChild("药使之径", count = null),
                    taskChild("野焰之径", count = null),
                    taskChild("圣颂之径", count = null),
                    taskChild("睿治之径", count = null),
                    taskChild("漂泊之径", count = null),
                    taskChild("迅拳之径", count = null),
                    taskChild("霜风之径", count = null),
                )
            ),
            Task(
                id = Task.Companion.TASK_ID_ECHO_OF_WAR,
                name = "历战余响",
                subName = "行迹高级材料/光锥",
                params = mutableStateListOf(
                    taskChild("心兽的战场", count = null),
                    taskChild("尘梦的赞礼", count = null),
                    taskChild("蛀星的旧靥", count = null),
                    taskChild("不死的神实", count = null),
                    taskChild("寒潮的落幕", count = null),
                    taskChild("毁灭的开端", count = null),
                )
            ),
            Task(
                id = Task.Companion.TASK_ID_SIMULATED_UNIVERSE,
                name = "模拟宇宙",
                subName = "周期积分/位面饰品",
                params = mutableStateListOf(
                    taskChild("直接传送", "积分奖励", count = null),
                    taskChild("第八世界", count = null),
                    taskChild("第七世界", count = null),
                    taskChild("第六世界", count = null),
                    taskChild("第五世界", count = null),
                    taskChild("第四世界", count = null),
                    taskChild("第三世界", count = null),
                )
            ),
            Task(Task.Companion.TASK_ID_FORGOTTEN_HALL, "忘却之庭"),
            Task(Task.Companion.TASK_ID_ASSIGNMENTS, "委托任务"),
            Task(Task.Companion.TASK_ID_NAMELESS_HONOR, "无名勋礼"),
            Task(Task.Companion.TASK_ID_TRAVEL_LOG, "旅情事记"),
            Task(Task.Companion.TASK_ID_TRAINING, "每日实训"),
            Task(Task.Companion.TASK_ID_CLOSE, "关闭游戏"),
        )
    )

val defaultGames: MutableList<Game>
    get() = mutableListOf(
        defaultGenshinGame,
        defaultStarRailGame,
        defaultHonkaiGame,
        defaultZenlessZoneZeroGame
    )
