package ciyin.ai.integrate.image

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.ImageEngine

/**
 * 聚合层用来表达「如何路由到生图引擎（及可选逻辑模型）」的轻量描述。
 *
 * 调用方只描述选择意图，不直接持有 [ImageEngine]；具体引擎由 [AiImageIntegrate] 根据当前
 * 注册表与配置快照解析。
 */
sealed interface ImageEngineSpec {

    /** 使用 [IntegrateEnginePreferences] 提供的默认描述。 */
    data object Default : ImageEngineSpec

    /**
     * 显式指定 [EngineId] 与该引擎下的模型名。
     *
     * @property engineId 目标引擎 ID。
     * @property model 模型名；`null` 表示沿用请求模型或引擎配置默认模型。
     */
    data class Explicit(
        val engineId: EngineId,
        val model: String? = null,
    ) : ImageEngineSpec

    /**
     * 按能力筛选生图引擎。
     *
     * @property required 必须同时具备的能力集合；空集合表示不限定具体能力。
     */
    data class ByCapability(
        val required: Set<ImageCapability>,
    ) : ImageEngineSpec
}
