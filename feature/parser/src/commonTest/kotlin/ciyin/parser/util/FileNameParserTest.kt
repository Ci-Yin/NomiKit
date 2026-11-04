package ciyin.parser.util

import ciyin.parser.model.FileNameInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 文件名解析与字符串 ID 契约测试。
 */
class FileNameParserTest {

    /** 数字文件名中的 ID 应以字符串形式解析，并可无损重建。 */
    @Test
    fun parsesAndBuildsStringId() {
        val fileName = "danbooru_123_abcd.jpg"

        val info = fileName.toFileName()

        assertEquals(
            FileNameInfo(
                site = "danbooru",
                id = "123",
                md5 = "abcd",
                fileExt = "jpg",
            ),
            info,
        )
        assertEquals(fileName, info?.buildFileName())
    }

    /** 超出 Int 范围的数字 ID 仍应作为字符串解析，避免数字转换溢出。 */
    @Test
    fun parsesLargeNumericIdAsString() {
        val info = "sankaku_999999999999999999999_abcd.jpg".toFileName()

        assertEquals("999999999999999999999", info?.id)
    }

    /** 字母数字组成的 opaque ID 应以字符串形式解析，并可无损重建。 */
    @Test
    fun parsesOpaqueStringId() {
        val fileName = "sankaku_abC123_9x.jpg"

        val info = fileName.toFileName()

        assertEquals("abC123", info?.id)
        assertEquals(fileName, info?.buildFileName())
    }

    /** 下划线属于 `\\w`，应允许出现在字符串 ID 中。 */
    @Test
    fun parsesStringIdWithUnderscore() {
        val fileName = "sankaku_abc_def_9x.jpg"
        val info = fileName.toFileName()

        assertEquals("abc_def", info?.id)
        assertEquals(fileName, info?.buildFileName())
    }

    /** 文件名协议仍拒绝不属于 `\\w` 的 ID 字符。 */
    @Test
    fun rejectsUnsupportedIdCharacters() {
        assertNull("sankaku_abc-def_9x.jpg".toFileName())
    }
}
