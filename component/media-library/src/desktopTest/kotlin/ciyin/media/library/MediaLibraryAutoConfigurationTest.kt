package ciyin.media.library

import ciyin.MediaLibraryBootInitializer
import ciyin.io.File
import ciyin.koin.runKoinBoot
import ciyin.platform.Context
import ciyin.platform.DesktopContext
import ciyin.platform.context.CommonContextFiles
import org.koin.core.error.InstanceCreationException
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/** 系统媒体库 KoinBoot 自动配置测试。 */
class MediaLibraryAutoConfigurationTest {
    /** 每个用例完成后停止全局 Koin，避免定义跨测试泄漏。 */
    @AfterTest
    fun stopKoin() {
        KoinPlatformTools.defaultContext().stopKoin()
    }

    /** 默认配置应从 Context 创建并缓存一个 MediaLibrary 单例。 */
    @Test
    fun defaultConfigurationCreatesSingleton() {
        val context = testContext()
        val contextModule = module { single<Context> { context } }
        val koin = runKoinBoot {
            MediaLibraryBootInitializer()
            modules(contextModule)
        }

        assertSame(koin.get<MediaLibrary>(), koin.get<MediaLibrary>())
    }

    /** 用户预先注册的 MediaLibrary 应覆盖默认平台实现。 */
    @Test
    fun customBindingWinsOverAutoConfiguration() {
        val custom = FakeMediaLibrary()
        val customModule = module { single<MediaLibrary> { custom } }
        val koin = runKoinBoot {
            MediaLibraryBootInitializer()
            modules(customModule)
        }

        assertSame(custom, koin.get<MediaLibrary>())
    }

    /** 未注册 Context 时解析默认实现必须明确失败。 */
    @Test
    fun missingContextFailsResolution() {
        val koin = runKoinBoot {
            MediaLibraryBootInitializer()
        }

        assertFailsWith<InstanceCreationException> {
            koin.get<MediaLibrary>()
        }
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

    /** 用户自定义媒体库测试替身。 */
    private class FakeMediaLibrary : MediaLibrary {
        /** 测试不执行真实发布。 */
        override suspend fun publish(request: MediaPublishRequest): PublishedMedia =
            error("测试不执行发布")

        /** 测试不执行真实删除。 */
        override suspend fun delete(media: PublishedMedia) = Unit

        /** 测试替身始终报告不存在。 */
        override suspend fun exists(media: PublishedMedia): Boolean = false
    }
}
