package com.ciyin.app.ui.screen.aiimage

import com.ciyin.app.ui.screen.aiimage.data.AiImagePreferences

/**
 * 文生图演示页面的用户交互与内部状态回灌事件。
 *
 * 由 [AiImageViewModel] 在 FlowRedux2 状态机内通过 `on<...> { ... }` 消费。
 * 其中 [GenerationProgress]、[GenerationPreview]、[GenerationCompleted] 等由生成协程回灌，请勿在 UI 层发送。
 */
internal sealed interface AiImageAction {

    /**
     * 从 DataStore 恢复的偏好（进入状态时由 ViewModel 派发一次）。
     *
     * @property prefs 磁盘中的偏好快照。
     */
    data class PrefsLoaded(val prefs: AiImagePreferences) : AiImageAction

    /** 点击顶部返回按钮。 */
    data object BackClick : AiImageAction

    /** 用户修改 WebUI 主机或 IP（不含协议与端口）。 */
    data class ServerHostChange(val value: String) : AiImageAction

    /** 用户修改提示词。 */
    data class PromptChange(val value: String) : AiImageAction

    /** 点击「生成」按钮。 */
    data object GenerateClick : AiImageAction

    /** 生成流上报进度（由 ViewModel 内部协程回灌）。 */
    data class GenerationProgress(
        /** 进度比例 `0f..1f`，与 [ciyin.ai.core.image.ImageEvent.Progress] 一致。 */
        val progress: Float,
        val message: String?,
    ) : AiImageAction

    /** 生成流返回中间预览图（由 ViewModel 内部协程回灌）。 */
    data class GenerationPreview(val bytes: ByteArray, val mimeType: String) : AiImageAction

    /** 生成成功结束并返回图像（由 ViewModel 内部协程回灌）。 */
    data class GenerationCompleted(val bytes: ByteArray, val mimeType: String) : AiImageAction

    /** 生成成功结束但未返回任何图像（由 ViewModel 内部协程回灌）。 */
    data object GenerationCompletedEmpty : AiImageAction

    /** 生成失败（由 ViewModel 内部协程回灌）。 */
    data class GenerationFailed(val message: String) : AiImageAction

    /** 生成协程被取消时复位加载态（由 ViewModel 内部协程回灌）。 */
    data object GenerationDismissed : AiImageAction
}
