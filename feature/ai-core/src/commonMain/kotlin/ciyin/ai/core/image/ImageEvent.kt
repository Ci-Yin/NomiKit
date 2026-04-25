package ciyin.ai.core.image

import ciyin.ai.core.error.AiEngineError

/**
 * 生图调用过程中产出的事件。
 *
 * 引擎实现的 `Flow<ImageEvent>` 必须遵守的约束（与 `ChatEvent` 同构）：
 * 1. 第一个事件应为 [Started]；
 * 2. 流必须以 [Completed] 或 [Failed] 之一结束；
 * 3. 同一次调用中 `Completed` 与 `Failed` **不能**同时出现；
 * 4. 中间过程允许任意数量的 [Progress] / [Preview] 穿插。
 */
sealed interface ImageEvent {

    /** 调用已发出，正在等待第一个响应。 */
    data object Started : ImageEvent

    /**
     * 进度更新。
     *
     * 同步类引擎（如当前的 SD WebUI 阻塞返回）默认不发送 `Progress`；
     * 未来若适配 progress polling 或队列任务，可在中间补发。
     *
     * @property progress 进度比例 `0.0..1.0`。
     * @property message 可选的进度描述（如 `"step 12 / 30"`）。
     */
    data class Progress(
        val progress: Float,
        val message: String? = null,
    ) : ImageEvent

    /**
     * 中间预览图。
     *
     * 用于"实时出图"场景；调用方一般只展示最近一张预览，等到 [Completed] 时替换为最终图。
     */
    data class Preview(val image: GeneratedImage) : ImageEvent

    /**
     * 调用成功完成。
     *
     * @property result 完整聚合后的结果，参见 [ImageResult]。
     */
    data class Completed(val result: ImageResult) : ImageEvent

    /**
     * 调用失败。
     *
     * @property error 引擎层错误模型，参见 `ciyin.ai.core.error.AiEngineError`。
     */
    data class Failed(val error: AiEngineError) : ImageEvent
}
