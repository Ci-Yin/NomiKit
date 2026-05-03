package ciyin.ai.core.image

/**
 * 一张已生成的图像。
 *
 * 通用层选用"原始字节 + MIME 类型"承载，避免依赖任何平台位图类型；
 * 上层做展示 / 持久化时按需解码。
 *
 * @property bytes 原始字节。
 * @property mimeType 标准 MIME 类型，如 `"image/png"` / `"image/jpeg"` / `"image/webp"`。
 * @property infotext 描述信息，如 `"1girl, blue hair, blue eyes, looking at viewer, (best quality), masterpiece"`。
 * @property seed 实际使用的种子；`null` 表示上游未提供（如随机种子未回传）。
 */
data class GeneratedImage(
    val mimeType: String,
    val seed: Long = -1,
    val infotext: String = "",
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GeneratedImage

        if (seed != other.seed) return false
        if (mimeType != other.mimeType) return false
        if (infotext != other.infotext) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = seed.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + infotext.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
