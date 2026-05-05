package ciyin.ai.facade.selection

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.ImageEngine

/**
 * 业务侧用来表达「如何路由到生图引擎（及可选逻辑模型）」的轻量描述，与 [ChatEngineSpec] 形态完全对称。
 *
 * 关键约束：业务层**永远不**直接持有 [ImageEngine] 实例，只通过本类型表达意图。
 */
sealed interface ImageEngineSpec {

    /** 使用 `EnginePreferences` 提供的默认描述。 */
    data object Default : ImageEngineSpec

    /**
     * 显式指定 [EngineId] + 该引擎下的 [model]。
     *
     * @property engineId 目标引擎 ID。
     * @property model 模型名；`null` 表示沿用引擎当前激活的默认模型。
     */
    data class Explicit(
        val engineId: EngineId,
        val model: String? = null,
    ) : ImageEngineSpec

    /**
     * 按能力筛选。
     *
     * @property required 必须同时具备的能力集合，空集合等价于 [Default]。
     */
    data class ByCapability(val required: Set<ImageCapability>) : ImageEngineSpec
}
