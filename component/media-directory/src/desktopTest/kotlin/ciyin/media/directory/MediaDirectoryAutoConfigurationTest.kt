package ciyin.media.directory

import ciyin.io.File
import ciyin.koin.runKoinBoot
import ciyin.platform.Context
import ciyin.platform.DesktopContext
import ciyin.platform.context.CommonContextFiles
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/**
 * 用户媒体目录 KoinBoot 自动配置测试。
 */
class MediaDirectoryAutoConfigurationTest {
    /** 每个用例完成后停止全局 Koin，避免定义跨测试泄漏。 */
    @AfterTest
    fun stopKoin() {
        KoinPlatformTools.defaultContext().stopKoin()
    }

    /** 默认配置应从 Context 创建并缓存一个 MediaDirectorySession 单例。 */
    @Test
    fun autoConfigurationProvidesDefaultSession() {
        val context = testContext()
        val contextModule = module { single<Context> { context } }
        val koin = runKoinBoot {
            autoConfigurations(MediaDirectoryAutoConfiguration)
            modules(contextModule)
        }

        // 不实际调用 requestAccess，避免触发桌面选择器弹窗。
        val first = assertNotNull(koin.getOrNull<MediaDirectorySession>())
        val second = assertNotNull(koin.getOrNull<MediaDirectorySession>())
        // 自动配置注册的是 single，多次解析应拿到同一实例。
        assertSame(first, second)
    }

    /** 用户预先提供的 MediaDirectorySession 应覆盖默认平台实现。 */
    @Test
    fun customBindingWinsOverAutoConfiguration() {
        val custom = FakeMediaDirectorySession()
        val customModule = module { single<MediaDirectorySession> { custom } }
        val koin = runKoinBoot {
            autoConfigurations(MediaDirectoryAutoConfiguration)
            modules(customModule)
        }

        assertSame(custom, koin.get<MediaDirectorySession>())
    }

    /** 创建仅供 Koin 构造验证使用的 Desktop Context。 */
    private fun testContext(): Context {
        val root = File(System.getProperty("java.io.tmpdir"))
        return DesktopContext(
            CommonContextFiles(
                cacheDir = root,
                dataDir = root,
                defaultBaseMediaCacheDir = root,
            ),
        )
    }

    /** 测试替身：所有方法在测试范围内都不真正执行。 */
    private class FakeMediaDirectorySession : MediaDirectorySession {
        override suspend fun requestAccess(): MediaDirectoryAccess =
            error("测试不调用 requestAccess")

        override suspend fun validate(accessToken: MediaDirectoryAccessToken): Unit = Unit
        override suspend fun commit(
            accessToken: MediaDirectoryAccessToken,
            source: ciyin.io.File,
            fileName: String,
            mimeType: String,
        ): MediaDirectoryEntry = error("测试不调用 commit")

        override suspend fun delete(
            accessToken: MediaDirectoryAccessToken,
            location: String
        ): Unit = Unit

        override suspend fun exists(
            accessToken: MediaDirectoryAccessToken,
            location: String
        ): Boolean = false
    }
}
