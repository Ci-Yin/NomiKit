package ciyin.ai.integrate.image

import ciyin.ai.core.engine.EngineId

/**
 * 生图引擎的**唯一**配置入口：共通字段在父类型声明，各后端为 `sealed` 子类。
 *
 * 首版仅 [SdWebUi]；后续每增加一种后端，在此增加子类，并在 [IntegrateImageEngineIds] 增加对应常量。
 */
sealed class ImageEngineConfig {

    /** 与 [IntegrateImageEngineIds] 对齐的引擎标识。 */
    abstract val engineId: EngineId

    /** 服务根地址，例如 `http://127.0.0.1:7860`（尾斜杠可有可无，由解析实现规范化）。 */
    abstract val baseUrl: String

    /**
     * 鉴权用密钥；当前 [ciyin.ai.image.sdwebui.SdWebUiImageEngine] 尚未接线时仍保留字段，
     * 由应用层持久化与注入，**禁止**在聚合模块内写入日志或调试输出。
     */
    abstract val apiKey: String

    /** 当 [ciyin.ai.core.image.ImageRequest.model] 为 null 时合并的默认模型名。 */
    abstract val defaultModel: String?

    /**
     * AUTOMATIC1111 SD WebUI 后端配置。
     *
     * @property baseUrl WebUI HTTP(S) 根地址。
     * @property apiKey 预留鉴权字段。
     * @property defaultModel 默认逻辑模型名，可为 null。
     */
    data class SdWebUi(
        override val baseUrl: String,
        override val apiKey: String,
        override val defaultModel: String?,
    ) : ImageEngineConfig() {
        override val engineId: EngineId = IntegrateImageEngineIds.sdWebUi
    }
}
