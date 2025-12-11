package com.ciyin.app.di

import com.ciyin.app.di.modules.AppModule
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/6 14:11
 */

fun initKoin() {
    startKoin {
        modules(AppModule().module)
    }
}