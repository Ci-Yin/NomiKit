package com.ciyin.app.data.setting.di

import com.ciyin.app.data.setting.datasource.SettingsDataStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/6 14:05
 * @version: 1.0
 */

val SettingsDataModule = module {
    singleOf(::SettingsDataStorage)
}
