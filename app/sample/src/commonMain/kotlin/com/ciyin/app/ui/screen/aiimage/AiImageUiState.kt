package com.ciyin.app.ui.screen.aiimage

/**
 * 文生图演示页面的不可变 UI 状态。
 *
 * 所有变更经 [AiImageViewModel] 通过 `copy` 产生新实例；[resultBytes] 使用自定义 [equals] / [hashCode] 以按内容比较字节数组。
 */
internal data class AiImageUiState(
    /** WebUI 主机名或 IP，无协议与端口。 */
    val serverHost: String = "192.168.31.10",
    /** 正向提示词文本。 */
    val prompt: String = "",
    /** 是否正在调用生图接口。 */
    val isLoading: Boolean = false,
    /** `0f..1f`，来自 [ciyin.ai.core.image.ImageEvent.Progress]；非加载中为 `null`。 */
    val progress: Float? = null,
    /** 进度描述文案；无则为 `null`。 */
    val progressMessage: String? = null,
    /** 最近一次生成图像的字节数据。 */
    val resultBytes: ByteArray? = null,
    /** 与 [resultBytes] 对应的 MIME 类型（如 `image/png`）。 */
    val resultMimeType: String? = null,
    /** 错误提示；无错误时为 `null`。 */
    val errorMessage: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AiImageUiState

        if (isLoading != other.isLoading) return false
        if (progress != other.progress) return false
        if (progressMessage != other.progressMessage) return false
        if (serverHost != other.serverHost) return false
        if (prompt != other.prompt) return false
        if (!resultBytes.contentEquals(other.resultBytes)) return false
        if (resultMimeType != other.resultMimeType) return false
        if (errorMessage != other.errorMessage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isLoading.hashCode()
        result = 31 * result + (progress?.hashCode() ?: 0)
        result = 31 * result + (progressMessage?.hashCode() ?: 0)
        result = 31 * result + serverHost.hashCode()
        result = 31 * result + prompt.hashCode()
        result = 31 * result + (resultBytes?.contentHashCode() ?: 0)
        result = 31 * result + (resultMimeType?.hashCode() ?: 0)
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }
}