package ciyin.media.directory

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** 用户媒体目录模块的 token / 错误契约测试。 */
class MediaDirectoryAccessTokenTest {
    /** value class 不应泄漏字符串拼接能力。 */
    @Test
    fun tokenHoldsRawString() {
        val token = MediaDirectoryAccessToken("content://tree/abc")
        assertEquals("content://tree/abc", token.value)
    }

    /** value class 的拷贝应当保留值语义。 */
    @Test
    fun tokenCopyKeepsValue() {
        val token = MediaDirectoryAccessToken("ios-bookmark")
        val copied = MediaDirectoryAccessToken(token.value)
        assertEquals(token, copied)
    }
}

/** MediaDirectoryError 公共契约测试。 */
class MediaDirectoryErrorTest {
    /** `Cancelled` 应当可与 `PermissionDenied` 区分。 */
    @Test
    fun cancelledIsDistinctFromPermissionDenied() {
        assertTrue(MediaDirectoryError.Cancelled.toString().isNotBlank())
        val denied = MediaDirectoryError.PermissionDenied("用户拒绝")
        assertIs<MediaDirectoryError.PermissionDenied>(denied)
    }

    /** `AlreadyExists` 携带的文件名应当可在异常中读到。 */
    @Test
    fun alreadyExistsCarriesFileName() {
        val error = MediaDirectoryError.AlreadyExists("demo.png")
        assertEquals("demo.png", error.fileName)
    }

    /** `MediaDirectoryException` 应当把 [MediaDirectoryError.Io.cause] 放进 suppressed 列表。 */
    @Test
    fun exceptionPropagatesIoCause() {
        val cause = IllegalStateException("底层")
        val exception = MediaDirectoryException(MediaDirectoryError.Io("失败", cause))
        assertEquals(1, exception.suppressed.size)
        assertEquals(cause, exception.suppressed.first())
        assertIs<MediaDirectoryError.Io>(exception.error)
    }

    /** `MediaDirectoryException` 应当把非 IO 错误的错误码以 message 形式抛出。 */
    @Test
    fun exceptionMessageContainsError() {
        val exception = assertFailsWith<MediaDirectoryException> {
            MediaDirectoryError.AccessRevoked("失效").raise()
        }
        assertIs<MediaDirectoryError.AccessRevoked>(exception.error)
    }
}
