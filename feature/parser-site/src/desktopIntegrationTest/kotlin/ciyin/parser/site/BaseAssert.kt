package ciyin.parser.site

import ciyin.parser.model.Media
import ciyin.parser.model.Tag
import ciyin.parser.util.isValidFileName
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal abstract class BaseAssert {

    /** 正文内容 & 相关内容媒体 */
    internal fun Media.assertStrictAsComicMedia(context: String) {
        fileName.checkNotEmpty("fileName")
        web.checkNotEmpty("web")

        originalUrl.checkUrl("originalUrl")
        sampleUrl.checkUrl("sampleUrl")
        thumbUrl.checkUrl("thumbUrl")
        sourceUrl.checkUrlIfNotEmpty("sourceUrl")

        fileSize.checkBounds("($context) Media.fileSize")
        width.checkBounds("($context) Media.width")
        height.checkBounds("($context) Media.height")
        createdAt.checkBounds("($context) Media.createdAt")
        updatedAt.checkBounds("($context) Media.updatedAt")
    }

    internal fun List<Tag>.checkTags() = forEach { tag ->
        tag.tag.checkNotEmpty("Tag.tag")
        tag.count.checkBounds("Tag.count")
        tag.createdAt.checkBounds("Tag.createdAt")
        tag.updatedAt.checkBounds("Tag.updatedAt")
    }


    /** MD5字段（允许为空，但不允许首尾多余空格，且必须是 32 位 MD5）*/
    internal fun String.checkMd5(field: String) {

        checkNotEmpty(field)

        // 首尾空格
        assertEquals(this, this.trim(), "$field 首尾不应包含多余空格，实际为: '$this'")

        // 长度
        assertEquals(32, length, "$field 必须是 32 位 MD5，实际长度为: $length，'$this'")

        // 十六进制字符
        assertTrue(
            all { it in '0'..'9' || it in 'a'..'f' },
            "$field 必须是小写16进制字符，实际为: '$this'"
        )
    }

    /** URL 字段（非空时必须是 http/https）*/
    internal fun String.checkUrlIfNotEmpty(field: String) {
        if (isNotEmpty()) {
            checkUrl(field)
        }
    }


    /** URL 字段（必须是 http/https）*/
    internal fun String.checkUrl(field: String) {
        assertTrue(
            startsWith("http"),
            "$field 必须是 http(s) URL，实际为: $this",
        )
    }

    /** 文件名字段（必须是有效的文件名）*/
    internal fun String.checkFileName(field: String) {
        checkNotEmpty(field)
        assertTrue(isValidFileName(), "$field 不是一个有效的文件名，实际为: $this")
    }

    /** 数值范围 */
    internal fun Int.checkBounds(field: String) {
        toLong().checkBounds(field)
    }

    /** 数值范围 */
    internal fun Long.checkBounds(field: String) {
        assertTrue(this >= 0, "$field 应 >= 0，实际为: $this")
    }

    /** 文本字段（不允许为空）*/
    internal fun String.checkNotEmpty(field: String) {
        assertTrue(isNotEmpty(), "$field 不能为空，实际为: '$this'")
    }

    /** 文本字段（不允许为空）*/
    internal fun List<*>.checkNotEmpty(field: String) {
        assertTrue(isNotEmpty(), "$field 不能为空，实际为: '$this'")
    }

    /** 文本字段（允许为空，但不允许首尾多余空格）*/
    internal fun String.checkTrimmed(field: String) {
        assertEquals(this, this.trim(), "$field 首尾不应包含多余空格，实际为: '$this'")
    }
}