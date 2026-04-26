package com.ciyin.app.ui.screen.aiimage.data

import androidx.datastore.core.DataStore
import ciyin.datastore.DataStoreFactory
import org.koin.mp.KoinPlatform.getKoin

/**
 * 文生图演示的 DataStore 包装，通过全局 Koin 读取 [DataStoreFactory]。
 */
internal class AiImageDataStore : DataStore<AiImagePreferences> by getKoin()
    .get<DataStoreFactory>()
    .create(defaultValue = AiImagePreferences())
