package com.ciyin.app.ui.screen.timer

import ciyin.platform.model.TaskSchedule


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/3 11:59
 */

fun Timing.toTaskSchedule(): TaskSchedule {
    return TaskSchedule(
        week = week,
        hour = hour,
        minute = minute,
    )
}