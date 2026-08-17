package ciyin.media.library

import ciyin.platform.Context

/** 使用当前平台上下文创建系统媒体库实现。 */
expect fun createMediaLibrary(context: Context): MediaLibrary
