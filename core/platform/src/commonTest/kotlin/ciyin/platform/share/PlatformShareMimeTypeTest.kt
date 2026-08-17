package ciyin.platform.share

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** 系统分享 MIME 类型规则测试。 */
class PlatformShareMimeTypeTest {

    /** 显式标题会去除首尾空白。 */
    @Test
    fun explicitTitleIsTrimmed() {
        val payload = PlatformSharePayload.Text(
            value = "content",
            title = "  title  ",
        )

        assertEquals("title", payload.validatedPlatformShareTitleOrNull())
    }

    /** 显式空白标题必须报告无效载荷。 */
    @Test
    fun blankTitleReportsInvalidPayload() {
        val payload = PlatformSharePayload.Text(
            value = "content",
            title = "  ",
        )

        val exception = assertFailsWith<PlatformShareException> {
            payload.validatedPlatformShareTitleOrNull()
        }

        assertEquals(PlatformShareFailureReason.InvalidPayload, exception.reason)
    }

    /** 空白分享文本必须报告无效载荷。 */
    @Test
    fun blankTextReportsInvalidPayload() {
        val exception = assertFailsWith<PlatformShareException> {
            "\n  ".validatedPlatformShareText()
        }

        assertEquals(PlatformShareFailureReason.InvalidPayload, exception.reason)
    }

    /** 单一 MIME 类型保持不变。 */
    @Test
    fun singleMimeTypeIsPreserved() {
        assertEquals(
            expected = "image/png",
            actual = listOf("image/png").resolveCommonPlatformShareMimeType(),
        )
    }

    /** 同主类型的多个 MIME 类型收敛为主类型通配符。 */
    @Test
    fun sameTopLevelTypesUseTopLevelWildcard() {
        assertEquals(
            expected = "image/*",
            actual = listOf("image/png", "image/jpeg").resolveCommonPlatformShareMimeType(),
        )
    }

    /** 不同主类型的多个 MIME 类型收敛为任意类型通配符。 */
    @Test
    fun mixedTopLevelTypesUseAnyWildcard() {
        assertEquals(
            expected = "*/*",
            actual = listOf("image/png", "text/plain").resolveCommonPlatformShareMimeType(),
        )
    }

    /** 缺少子类型的 MIME 类型必须报告无效载荷。 */
    @Test
    fun invalidMimeTypeReportsInvalidPayload() {
        val exception = assertFailsWith<PlatformShareException> {
            listOf("image").resolveCommonPlatformShareMimeType()
        }

        assertEquals(PlatformShareFailureReason.InvalidPayload, exception.reason)
    }
}
