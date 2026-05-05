package ciyin.ai.core.image

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId

/**
 * 生图模型描述信息。
 *
 * 由 `ImageEngine.models()` / `AiImage.models()` 返回，供 UI 展示与选择。
 *
 * @property engineId 模型所在引擎的 ID。
 * @property model 模型名（如 `"sd_xl_base_1.0"`）。
 * @property displayName 推荐展示名；为 `null` 时由 UI 直接展示 [model]。
 * @property capabilities 该模型实际具备的能力子集（可能比所属引擎宣称的更窄，例如某些 SD 模型不支持 ControlNet）。
 * @property maxSize 该模型推荐的最大边长（像素）；`null` 表示未知或无固定限制。
 */
data class ImageModelInfo(
    val engineId: EngineId,
    val model: String,
    val displayName: String? = null,
    val capabilities: Set<ImageCapability> = emptySet(),
    val maxSize: Int? = null,
)
