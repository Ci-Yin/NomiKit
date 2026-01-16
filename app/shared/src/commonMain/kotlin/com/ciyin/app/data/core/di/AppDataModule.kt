package com.ciyin.app.data.core.di

import ciyin.room.singleDao
import com.ciyin.app.data.core.datasource.AppDatabase
import org.koin.dsl.module


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/6 14:05
 */

val AppDataModule = module {
    singleDao(AppDatabase::appDao)
}
