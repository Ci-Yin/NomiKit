package ciyin.ai.facade

import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.facade.selection.EnginePreferences
import ciyin.ai.facade.selection.ImageModelSpec
import kotlinx.coroutines.flow.Flow

/**
 * 生图能力的统一入口，与 [AiChat] 形态完全对称。
 *
 * 默认实现见 [DefaultAiImage]。
 */
interface AiImage {

    /** 使用 [EnginePreferences.defaultImageSpec] 指定的默认模型生成图像。 */
    fun generate(request: ImageRequest): Flow<ImageEvent>

    /**
     * 显式指定模型生成图像。
     *
     * @param spec 模型选择描述，参见 [ImageModelSpec]。
     * @param request 通用生图请求。
     */
    fun generate(spec: ImageModelSpec, request: ImageRequest): Flow<ImageEvent>

    /** 列出全部已注册引擎的可用生图模型。语义见 [AiChat.listAvailableModels]。 */
    suspend fun listAvailableModels(): Result<List<ImageModelInfo>>
}
