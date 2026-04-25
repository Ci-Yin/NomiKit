package ciyin.ai.core.image

/**
 * 一张已生成的图像。
 *
 * 通用层选用"原始字节 + MIME 类型"承载，避免依赖任何平台位图类型；
 * 上层做展示 / 持久化时按需解码。
 *
 * @property bytes 原始字节。
 * @property mimeType 标准 MIME 类型，如 `"image/png"` / `"image/jpeg"` / `"image/webp"`。
 * @property seed 实际使用的种子；`null` 表示上游未提供（如随机种子未回传）。
 */
data class GeneratedImage(
    val bytes: ByteArray,
    val mimeType: String,
    val seed: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GeneratedImage

        if (seed != other.seed) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = seed?.hashCode() ?: 0
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}
