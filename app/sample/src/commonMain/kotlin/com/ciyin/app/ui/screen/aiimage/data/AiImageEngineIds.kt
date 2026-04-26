package com.ciyin.app.ui.screen.aiimage.data

import ciyin.ai.core.engine.EngineId

/**
 * 文生图演示用的稳定 [EngineId]，须与 [AiImageEnginePreferences.defaultImageSpec] 中字符串完全一致。
 */
internal object AiImageEngineIds {
    const val VALUE = "sdwebui:local-7860"
    val id: EngineId = EngineId(VALUE)
    const val PORT: Int = 7860
}
