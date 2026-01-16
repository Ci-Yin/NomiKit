package com.ciyin.app.di.modules

import ciyin.koin.KoinBootInitializer
import com.ciyin.app.data.core.di.AppDataModule


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/6 18:37
 */


val AppModules: KoinBootInitializer = {
    modules(AppDataModule)
}

val MainModules: KoinBootInitializer = {
//    modules(MainDomainModule)
}

val SettingsModules: KoinBootInitializer = {
//    modules(SettingsDataModule)
}