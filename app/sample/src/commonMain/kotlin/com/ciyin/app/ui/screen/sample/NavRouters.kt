package com.ciyin.app.ui.screen.sample

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
internal sealed interface NavRouter : NavKey


/** 样例模块子栈根：样例入口列表。 */
@Serializable
internal object SampleHubRouter : NavRouter

@Serializable
internal object AiImageDemoRouter : NavRouter

@Serializable
internal object AiChatRouter : NavRouter

/** 文件下载能力完整示例页。 */
@Serializable
internal object FileDownloaderDemoRouter : NavRouter

/** 运行环境信息示例页。 */
@Serializable
internal object RuntimeInfoRouter : NavRouter

/** 权限管理示例页。 */
@Serializable
internal object PermissionsRouter : NavRouter

/** 视频播放器示例页。 */
@Serializable
internal object VideoPlayerDemoRouter : NavRouter

/** 占位样例子页 A。 */
@Serializable
internal object SampleExamplePlaceholderARouter : NavRouter

internal val NavSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(SampleHubRouter::class, SampleHubRouter.serializer())
            subclass(AiImageDemoRouter::class, AiImageDemoRouter.serializer())
            subclass(AiChatRouter::class, AiChatRouter.serializer())
            subclass(FileDownloaderDemoRouter::class, FileDownloaderDemoRouter.serializer())
            subclass(RuntimeInfoRouter::class, RuntimeInfoRouter.serializer())
            subclass(PermissionsRouter::class, PermissionsRouter.serializer())
            subclass(VideoPlayerDemoRouter::class, VideoPlayerDemoRouter.serializer())
            subclass(
                SampleExamplePlaceholderARouter::class,
                SampleExamplePlaceholderARouter.serializer()
            )
        }
    }
}
