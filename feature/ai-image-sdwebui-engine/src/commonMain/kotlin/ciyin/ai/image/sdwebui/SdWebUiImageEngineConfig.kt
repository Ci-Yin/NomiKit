package ciyin.ai.image.sdwebui

import ciyin.ai.core.engine.EngineId
import ciyin.sdwebui.SdWebUi

/**
 * [SdWebUiImageEngine] 的最小配置项。
 *
 * 当调用方不想自己构造 [SdWebUi] 时，可直接传本配置给便利构造函数。
 */
data class SdWebUiImageEngineConfig(
    val id: EngineId,
    val host: String = SdWebUi.DEFAULT_HOST,
    val port: Int = SdWebUi.DEFAULT_PORT,
    val useHttps: Boolean = false,
)
