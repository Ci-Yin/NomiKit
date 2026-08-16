package ciyin.permissions

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** 权限组与请求结果公共契约测试。 */
class PermissionGroupTest {
    /** 内置权限组的数量与声明顺序保持稳定。 */
    @Test
    fun builtInGroupsKeepDeclaredOrder() {
        assertContentEquals(
            listOf(
                PermissionGroup.Camera,
                PermissionGroup.Phone,
                PermissionGroup.Microphone,
                PermissionGroup.Sms,
                PermissionGroup.Location,
                PermissionGroup.Media,
                PermissionGroup.Sensors,
                PermissionGroup.Storage,
                PermissionGroup.Contacts,
                PermissionGroup.Calendar,
                PermissionGroup.Notifications,
                PermissionGroup.Bluetooth,
                PermissionGroup.Wifi,
                PermissionGroup.Internet,
            ),
            PermissionGroup.builtIn,
        )
    }

    /** 展开多个权限组时按首次出现位置去重。 */
    @Test
    fun groupsAreExpandedInOrderWithoutDuplicates() {
        val permissions = arrayOf(
            PermissionGroup.Location,
            PermissionGroup.Custom(Permission.LocationFine, Permission.Camera),
        ).toPermissions()

        assertContentEquals(
            arrayOf(Permission.LocationCoarse, Permission.LocationFine, Permission.Camera),
            permissions,
        )
    }

    /** 派生集合按状态正确归类。 */
    @Test
    fun resultDerivesStatusCollections() {
        val result = PermissionRequestResult(
            linkedMapOf(
                Permission.Camera to PermissionStatus.Granted,
                Permission.Microphone to PermissionStatus.Denied,
                Permission.LocationFine to PermissionStatus.PermanentlyDenied,
                Permission.Contacts to PermissionStatus.Restricted,
                Permission.Phone to PermissionStatus.Unsupported,
                Permission.Notifications to PermissionStatus.NotDetermined,
            ),
        )

        assertEquals(setOf(Permission.Camera), result.granted)
        assertEquals(
            setOf(Permission.Microphone, Permission.LocationFine, Permission.Contacts),
            result.denied,
        )
        assertEquals(setOf(Permission.LocationFine), result.permanentlyDenied)
        assertEquals(setOf(Permission.Contacts), result.restricted)
        assertEquals(setOf(Permission.Phone), result.unsupported)
        assertEquals(setOf(Permission.Notifications), result.notDetermined)
        assertFalse(result.allGranted)
        assertTrue(PermissionGroup.Location.permissions.contains(Permission.LocationFine))
    }

    /** 空结果具有稳定且便于组合的语义。 */
    @Test
    fun emptyResultIsGrantedAndHasNoDerivedPermissions() {
        val result = PermissionRequestResult(emptyMap())

        assertTrue(result.allGranted)
        assertTrue(result.granted.isEmpty())
        assertTrue(result.denied.isEmpty())
        assertTrue(result.unsupported.isEmpty())
    }
}
