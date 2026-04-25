package ciyin.ai.core.image

/**
 * 后处理流水线节点。
 *
 * 表达"先生图，再做几道修饰"的通用意图；
 * 适配层负责将其翻译为底层 SDK 的 alwayson script 或独立的二次 pass 调用。
 */
sealed interface ImagePostProcessor {

    /**
     * 面部细节修复（ADetailer 等）。
     *
     * @property model 检测模型名（如 `"face_yolov8n.pt"`）。
     * @property confidence 检测置信度阈值，范围 `0.0..1.0`。
     */
    data class FaceDetailer(
        val model: String,
        val confidence: Float = 0.3f,
    ) : ImagePostProcessor

    /**
     * 换脸（ReActor 等）。
     *
     * @property sourceFace 源人脸图字节。
     */
    data class FaceSwap(val sourceFace: ByteArray) : ImagePostProcessor {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as FaceSwap

            if (!sourceFace.contentEquals(other.sourceFace)) return false

            return true
        }

        override fun hashCode(): Int {
            return sourceFace.contentHashCode()
        }
    }

    /** 移除背景。 */
    data object BackgroundRemoval : ImagePostProcessor

    /**
     * 图像放大 / 超分。
     *
     * @property factor 放大倍率，常见取值 `2.0` / `4.0`。
     * @property model 放大模型名（如 `"R-ESRGAN 4x+"`）；`null` 表示由引擎选择默认。
     */
    data class Upscale(
        val factor: Float,
        val model: String? = null,
    ) : ImagePostProcessor
}
