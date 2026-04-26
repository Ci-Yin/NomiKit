package com.ciyin.app.ui.screen.aichat.data

import androidx.datastore.core.DataStore
import ciyin.datastore.DataStoreFactory
import org.koin.mp.KoinPlatform.getKoin

/**
 * AI 聊天示例的 DataStore 包装，通过 KoinComponent 获取全局 [DataStoreFactory]。
 *
 * 在 app:sample 没有独立 Koin 模块的前提下，借助 app:shared 已启动的全局 Koin 上下文读取工厂。
 */
internal class AiChatDataStore : DataStore<AiChatPreferences> by getKoin()
    .get<DataStoreFactory>()
    .create(defaultValue = AiChatPreferences())

