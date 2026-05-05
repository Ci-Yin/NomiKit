package ciyin.ai.core.capability

/**
 * 生图引擎能力。
 *
 * 用于 `Registry.findByCapability(...)` 与 [ciyin.ai.core.registry.ImageEngineSelector] `select(required = ...)`。
 * 新增能力请保持向后兼容（只追加、不删除）。
 */
sealed interface ImageCapability : AiCapability {

    /** 文生图。 */
    data object TextToImage : ImageCapability

    /** 图生图。 */
    data object ImageToImage : ImageCapability

    /** 局部重绘 / 蒙版重绘。 */
    data object Inpainting : ImageCapability

    /** ControlNet 结构化条件控制。 */
    data object ControlNet : ImageCapability

    /** IP-Adapter 风格 / 主体迁移。 */
    data object IPAdapter : ImageCapability

    /** 面部细节修复（ADetailer 等）。 */
    data object FaceDetailer : ImageCapability

    /** 换脸（ReActor 等）。 */
    data object FaceSwap : ImageCapability

    /** 移除背景。 */
    data object BackgroundRemoval : ImageCapability

    /** 图像放大 / 超分辨率。 */
    data object Upscale : ImageCapability
}
