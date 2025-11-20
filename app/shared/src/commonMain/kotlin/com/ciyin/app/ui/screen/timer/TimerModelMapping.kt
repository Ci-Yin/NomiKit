package com.ciyin.app.ui.screen.timer

import ciyin.platform.model.TaskSchedule


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/3 11:59
 * @version: 1.0
 */

fun Timing.toTaskSchedule(): TaskSchedule {
    return TaskSchedule(
        week = week,
        hour = hour,
        minute = minute,
    )
}