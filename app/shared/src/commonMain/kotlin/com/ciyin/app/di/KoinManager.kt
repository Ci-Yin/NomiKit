package com.ciyin.app.di

import com.ciyin.app.di.modules.ProjectModules
import com.ciyin.app.di.modules.ScriptModules
import com.ciyin.app.di.modules.SettingsModules
import com.ciyin.app.di.modules.TimedModules
import com.yy.myuko.component.koin.ciyin.koin.runKoinBoot


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/6 14:11
 */

object KoinManager {
    fun init() {
        runKoinBoot {
            ProjectModules()
            ScriptModules()
            TimedModules()
            SettingsModules()
        }
    }
}