package ciyin.ai.facade

import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.facade.selection.ImageEngineSpec
import kotlinx.coroutines.flow.Flow

/**
 * 生图能力的统一入口，与 [AiChat] 形态完全对称。
 *
 * 默认实现见 [DefaultAiImage]。
 */
interface AiImage {

    /**
     * 显式指定模型生成图像。
     *
     * @param spec 引擎路由描述，参见 [ImageEngineSpec]。
     * @param request 通用生图请求。
     */
    fun generate(
        request: ImageRequest,
        spec: ImageEngineSpec = ImageEngineSpec.Default
    ): Flow<ImageEvent>

    /** 列出全部已注册引擎的可用生图模型。语义见 [AiChat.models]。 */
    suspend fun models(): List<ImageModelInfo>
}
