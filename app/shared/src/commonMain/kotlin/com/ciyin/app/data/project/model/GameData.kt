package com.ciyin.app.data.project.model

import kotlinx.serialization.Serializable

/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2024/10/19 下午9:09
 * @version: 1.0
 */

const val GAME_TYPE_GENSHIN = 0
const val GAME_TYPE_HONKAI = 1
const val GAME_TYPE_STARRAIL = 2
const val GAME_TYPE_ZENLESS_ZONE_ZERO = 3


/**
 * 游戏数据类，用于表示游戏的基本信息和任务列表。
 *
 * @property id 游戏的唯一标识符。
 * @property type 游戏的类型，用整数表示不同的游戏种类。
 * @property name 游戏的名字。
 * @property preset 预设配置或设置，可以被修改以适应不同的游戏场景需求。
 * @property tasks 游戏中的任务列表。每个任务包含其特定的信息如ID、名称等，并且支持状态管理，允许实时更新任务的状态。
 * @property isConfig 为`ture`时，表示该游戏预设用于保存到 [GameProject.gameConfig] 文件里
 */
@Serializable
data class Game(
    val id: Int,
    val type: Int,
    val name: String,
    val packageName: String = "",
    var preset: String,
    val tasks: List<Task>,
    val isConfig: Boolean = false
)

/**
 * 任务数据类，用于表示游戏中的各种任务。
 *
 * 该类包含了任务的基本信息，如任务ID、名称、子名称等，并且支持任务参数的动态配置以及任务选中状态的标记。
 * 通过[TaskChild]实例列表来存储具体的任务参数，这些参数可用于进一步定义和执行特定任务时的行为。
 * 此外，还提供了多个静态常量以标识不同类型的预定义任务，便于在应用中快速引用。
 *
 * @param id 任务唯一标识符
 * @param name 任务名称
 * @param subName 任务子名称，默认为空字符串
 * @param params 任务参数列表，默认为一个空的可变状态列表
 * @param checked 标记任务是否被选中，默认为true
 */
@Serializable
data class Task(
    val id: Int,
    val name: String,
    val subName: String = "",
    val checked: Boolean = true,
    val params: List<TaskChild> = listOf(),
) {

    companion object {

        /**
         * 登录游戏
         */
        const val TASK_ID_LOGIN = 0

        /**
         * 模拟宇宙
         */
        const val TASK_ID_SIMULATED_UNIVERSE = 1

        /**
         * 拟造花萼(金)
         */
        const val TASK_ID_CALYX_GOLDEN = 2

        /**
         * 拟造花萼(赤)
         */
        const val TASK_ID_CALYX_CRIMSON = 3

        /**
         * 凝滞虚影
         */
        const val TASK_ID_STAGNANT_SHADOW = 4

        /**
         * 侵蚀隧洞
         */
        const val TASK_ID_CAVERN_OF_CORROSION = 5

        /**
         * 历战余响
         */
        const val TASK_ID_ECHO_OF_WAR = 6

        /**
         * 忘却之庭
         */
        const val TASK_ID_FORGOTTEN_HALL = 7

        /**
         * 委托任务
         */
        const val TASK_ID_ASSIGNMENTS = 8

        /**
         * 无名勋礼
         */
        const val TASK_ID_NAMELESS_HONOR = 9

        /**
         * 旅情事记
         */
        const val TASK_ID_TRAVEL_LOG = 10

        /**
         * 每日实训
         */
        const val TASK_ID_TRAINING = 11

        /**
         * 关闭游戏
         */
        const val TASK_ID_CLOSE = 12


    }

}

/**
 * 任务参数数据类
 *
 * 该类用于定义任务的参数，包括任务标题、OCR识别文本、任务是否选中、挑战次数和重复战斗次数等信息
 * 主要用于任务配置和执行过程中参数的传递和存储
 *
 * @param title 任务标题，用于标识任务的名称
 * @param ocrText OCR识别文本，默认为任务标题，用于OCR识别
 * @param checked 任务是否被选中，默认为false，用于标记任务的选中状态
 * @param count 挑战次数参数，可选参数，默认为null，用于定义挑战的次数范围
 * @param repeat 重复战斗次数，用于定义重复战斗的次数范围
 */
@Serializable
data class TaskChild(
    val title: String,
    val ocrText: String = title,
    val checked: Boolean = false,
    val count: MenuParam? = null,
    val repeat: MenuParam,
)

/**
 * 任务参数数据类
 *
 * 该类用于定义任务的参数，包括任务标题、OCR识别文本、任务是否选中、挑战次数和重复战斗次数等信息
 * 主要用于任务配置和执行过程中参数的传递和存储
 *
 * @param title 任务标题，用于标识任务的名称
 * @param ocrText OCR识别文本，默认为任务标题，用于OCR识别
 * @param checked 任务是否被选中，默认为false，用于标记任务的选中状态
 * @param count 挑战次数参数，可选参数，默认为null，用于定义挑战的次数范围
 * @param repeat 重复战斗次数，用于定义重复战斗的次数范围
 */
fun taskChild(
    title: String,
    ocrText: String = title,
    checked: Boolean = false,
    count: MenuParam? = MenuParam("挑战次数", 6, 6),
    repeat: MenuParam = MenuParam("重复次数", 99, 1),
) = TaskChild(title, ocrText, checked, count, repeat)

/**
 * 菜单参数数据类
 *
 * 该类用于定义菜单项的参数，包括菜单标题、菜单项数量以及当前选中的菜单项索引。
 * 主要用于UI组件中菜单配置和显示过程中参数的传递与存储。
 *
 * @param title 菜单标题，用于标识菜单的名称
 * @param menus 菜单项的数量，表示菜单内包含多少个选项
 * @param selected 当前选中的菜单项索引，默认为1，即默认选中第一个菜单项
 */
@Serializable
data class MenuParam(
    val title: String,
    val menus: Int,
    val selected: Int = 1,
)

