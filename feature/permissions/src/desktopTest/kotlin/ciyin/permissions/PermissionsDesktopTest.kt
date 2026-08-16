package ciyin.permissions

import ciyin.platform.Context
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Desktop 权限契约测试。 */
class PermissionsDesktopTest {
    /** Desktop 查询和请求都明确返回不支持。 */
    @Test
    fun allPermissionsAreUnsupported() = runBlocking {
        val context = TestContext()

        assertEquals(
            PermissionStatus.Unsupported,
            Permissions.getStatus(context, Permission.Camera),
        )
        val result = Permissions.request(
            context,
            Permission.Camera,
            Permission.Camera,
            Permission.Notifications,
        )
        assertEquals(
            mapOf(
                Permission.Camera to PermissionStatus.Unsupported,
                Permission.Notifications to PermissionStatus.Unsupported,
            ),
            result.statuses,
        )
        assertEquals(setOf(Permission.Camera, Permission.Notifications), result.unsupported)
    }

    /** 空请求和设置跳转均安全结束。 */
    @Test
    fun emptyRequestAndSettingsAreNoOp() = runBlocking {
        val context = TestContext()
        val result = Permissions.request(context, *emptyArray<Permission>())

        assertTrue(result.statuses.isEmpty())
        Permissions.openAppSettings(context)
    }

    /** 测试使用的最小 Desktop 上下文。 */
    private class TestContext : Context()
}
