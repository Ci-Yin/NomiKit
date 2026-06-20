package ciyin.system.storage

import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 桌面应用身份到系统目录路径的映射测试。
 */
class AppInfoTest {

    /**
     * 验证完整应用 ID 模式在 Windows 下只追加单级应用目录。
     */
    @Test
    fun applicationIdUsesSingleWindowsDirectorySegment() {
        val baseDir = Paths.get("AppData", "Roaming")
        val directory = AppInfo.ApplicationId("com.ciyin.nomikit")
            .resolveWindowsAppDirectory(baseDir)

        assertEquals(
            baseDir.resolve("com.ciyin.nomikit"),
            directory,
        )
    }

    /**
     * 验证组织名和应用名模式在 Windows 下保留两级目录。
     */
    @Test
    fun organizationNameUsesOrganizationAndNameWindowsSegments() {
        val baseDir = Paths.get("AppData", "Roaming")
        val directory = AppInfo.OrganizationName(
            qualifier = "com",
            organization = "CiYin",
            name = "NomiKit",
        ).resolveWindowsAppDirectory(baseDir)

        assertEquals(
            baseDir.resolve("CiYin").resolve("NomiKit"),
            directory,
        )
    }
}
