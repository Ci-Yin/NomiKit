package com.ciyin.app.presentation.screen.sample

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * 样例子导航栈的可序列化路由契约，作为 Navigation 3 的导航键。
 */
@Serializable
internal sealed interface NavRouter : NavKey


/**
 * 样例模块子栈根：样例入口列表。
 */
@Serializable
internal object SampleHubRouter : NavRouter

/**
 * AI 文生图集成示例页路由。
 */
@Serializable
internal object AiImageDemoRouter : NavRouter

/**
 * OpenAI 兼容 AI 聊天示例页路由。
 */
@Serializable
internal object AiChatRouter : NavRouter

/**
 * 文件下载能力完整示例页。
 */
@Serializable
internal object FileDownloaderDemoRouter : NavRouter

/**
 * 运行环境信息示例页。
 */
@Serializable
internal object RuntimeInfoRouter : NavRouter

/**
 * 权限管理示例页。
 */
@Serializable
internal object PermissionsRouter : NavRouter

/**
 * 系统分享示例页。
 */
@Serializable
internal object PlatformShareDemoRouter : NavRouter

/**
 * 视频播放器示例页。
 */
@Serializable
internal object VideoPlayerDemoRouter : NavRouter

/**
 * 系统媒体库示例页。
 */
@Serializable
internal object MediaLibraryDemoRouter : NavRouter

/**
 * 文件选择器示例页。
 */
@Serializable
internal object FilePickerDemoRouter : NavRouter

/**
 * 用户媒体目录示例页。
 */
@Serializable
internal object MediaDirectoryDemoRouter : NavRouter

/**
 * 中立设计系统组件展示页。
 */
@Serializable
internal object DesignSystemDemoRouter : NavRouter

/**
 * 样例子栈的多态路由保存配置，包含设计系统展示页。
 */
internal val NavSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(SampleHubRouter::class, SampleHubRouter.serializer())
            subclass(AiImageDemoRouter::class, AiImageDemoRouter.serializer())
            subclass(AiChatRouter::class, AiChatRouter.serializer())
            subclass(FileDownloaderDemoRouter::class, FileDownloaderDemoRouter.serializer())
            subclass(RuntimeInfoRouter::class, RuntimeInfoRouter.serializer())
            subclass(PermissionsRouter::class, PermissionsRouter.serializer())
            subclass(PlatformShareDemoRouter::class, PlatformShareDemoRouter.serializer())
            subclass(VideoPlayerDemoRouter::class, VideoPlayerDemoRouter.serializer())
            subclass(MediaLibraryDemoRouter::class, MediaLibraryDemoRouter.serializer())
            subclass(FilePickerDemoRouter::class, FilePickerDemoRouter.serializer())
            subclass(MediaDirectoryDemoRouter::class, MediaDirectoryDemoRouter.serializer())
            subclass(DesignSystemDemoRouter::class, DesignSystemDemoRouter.serializer())
        }
    }
}
