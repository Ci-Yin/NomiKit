package ciyin.ai.core.image

/**
 * 生图模式：决定本次请求是文生图、图生图还是局部重绘。
 *
 * 选 sealed interface 是为了让调用方可以用 `when (request.source)` 做编译期穷举判断；
 * 也避免后续新增模式（如 outpainting）时悄悄破坏既有调用方代码。
 */
sealed interface ImageSource {

    /** 纯文本生成图像。 */
    data object TextToImage : ImageSource

    /**
     * 基于已有图像的图生图。
     *
     * @property sourceImage 输入图像原始字节，由调用方保证格式可被引擎识别（PNG / JPEG / WebP 等）。
     * @property denoisingStrength 去噪强度，范围 `0.0..1.0`，越大越偏离原图。
     */
    data class ImageToImage(
        val sourceImage: ByteArray,
        val denoisingStrength: Float = 0.75f,
    ) : ImageSource {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ImageToImage

            if (denoisingStrength != other.denoisingStrength) return false
            if (!sourceImage.contentEquals(other.sourceImage)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = denoisingStrength.hashCode()
            result = 31 * result + sourceImage.contentHashCode()
            return result
        }
    }

    /**
     * 局部重绘 / 蒙版重绘。
     *
     * @property sourceImage 原图字节。
     * @property mask 蒙版字节，白色区域为重绘区域；尺寸需与 [sourceImage] 一致。
     * @property denoisingStrength 重绘强度，与 [ImageToImage.denoisingStrength] 同义。
     */
    data class Inpainting(
        val sourceImage: ByteArray,
        val mask: ByteArray,
        val denoisingStrength: Float = 0.75f,
    ) : ImageSource {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Inpainting

            if (denoisingStrength != other.denoisingStrength) return false
            if (!sourceImage.contentEquals(other.sourceImage)) return false
            if (!mask.contentEquals(other.mask)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = denoisingStrength.hashCode()
            result = 31 * result + sourceImage.contentHashCode()
            result = 31 * result + mask.contentHashCode()
            return result
        }
    }
}
