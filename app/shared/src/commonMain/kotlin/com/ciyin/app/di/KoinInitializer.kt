package com.ciyin.app.di

import ciyin.generated.AppBootInitializer
import ciyin.koin.runKoinBoot
import ciyin.platform.Context
import com.ciyin.app.di.modules.AppModules
import com.ciyin.app.di.modules.MainModules
import com.ciyin.app.di.modules.SettingsModules


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/6 14:11
 */

fun initKoin(context: Context) {
    runKoinBoot {
        AppBootInitializer()
        AppModules()
        MainModules()
        SettingsModules()
        appDeclaration {
            koin.declare(context, secondaryTypes = listOf(Context::class))
        }
    }
}