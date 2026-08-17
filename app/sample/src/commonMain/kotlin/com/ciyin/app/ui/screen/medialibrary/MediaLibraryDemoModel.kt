package com.ciyin.app.ui.screen.medialibrary

import androidx.compose.runtime.Immutable
import ciyin.media.library.MediaCollection

/** 内置媒体测试标识。 */
internal enum class MediaLibraryDemoSampleId {
    /** 图片测试。 */
    Image,

    /** 视频测试。 */
    Video,

    /** 音频测试。 */
    Audio,

    /** 下载文件测试。 */
    Download,
}

/** 内置媒体测试定义。 */
@Immutable
internal data class MediaLibraryDemoSample(
    /** 稳定测试标识。 */
    val id: MediaLibraryDemoSampleId,
    /** 系统媒体库分类。 */
    val collection: MediaCollection,
    /** Compose 二进制资源路径。 */
    val resourcePath: String,
    /** 写入 cache 时使用的源文件名。 */
    val sourceFileName: String,
    /** 发布显示名称中的稳定片段。 */
    val displayNameStem: String,
    /** 媒体 MIME 类型。 */
    val mimeType: String,
)

/** 四类内置媒体测试定义，顺序同时作为批量执行顺序。 */
internal val mediaLibraryDemoSamples = listOf(
    MediaLibraryDemoSample(
        id = MediaLibraryDemoSampleId.Image,
        collection = MediaCollection.Images,
        resourcePath = "files/media_library_demo.png",
        sourceFileName = "media_library_demo.png",
        displayNameStem = "image",
        mimeType = "image/png",
    ),
    MediaLibraryDemoSample(
        id = MediaLibraryDemoSampleId.Video,
        collection = MediaCollection.Videos,
        resourcePath = "files/media_library_demo.mp4",
        sourceFileName = "media_library_demo.mp4",
        displayNameStem = "video",
        mimeType = "video/mp4",
    ),
    MediaLibraryDemoSample(
        id = MediaLibraryDemoSampleId.Audio,
        collection = MediaCollection.Audio,
        resourcePath = "files/media_library_demo.wav",
        sourceFileName = "media_library_demo.wav",
        displayNameStem = "audio",
        mimeType = "audio/wav",
    ),
    MediaLibraryDemoSample(
        id = MediaLibraryDemoSampleId.Download,
        collection = MediaCollection.Downloads,
        resourcePath = "files/media_library_demo.txt",
        sourceFileName = "media_library_demo.txt",
        displayNameStem = "download",
        mimeType = "text/plain",
    ),
)

/** 系统媒体库示例当前操作阶段。 */
internal enum class MediaLibraryDemoPhase {
    /** 尚未执行操作。 */
    Ready,

    /** 正在发布。 */
    Publishing,

    /** 已成功发布或确认存在。 */
    Published,

    /** 正在检查存在性。 */
    Checking,

    /** 平台引用指向的媒体已经不存在。 */
    Missing,

    /** 正在删除。 */
    Deleting,

    /** 已删除。 */
    Deleted,

    /** 当前平台不支持该测试。 */
    Unsupported,

    /** 最近操作失败。 */
    Failed,
}

/** 可稳定展示的已发布媒体信息。 */
@Immutable
internal data class MediaLibraryDemoPublishedModel(
    /** 平台稳定标识。 */
    val platformId: String,
    /** 平台稳定 URI。 */
    val uri: String?,
    /** 发布后的显示名称。 */
    val displayName: String,
    /** 发布时的 MIME 类型。 */
    val mimeType: String,
    /** 发布完成时的字节大小。 */
    val size: Long,
)

/** 页面可展示的媒体库错误分类。 */
internal enum class MediaLibraryDemoErrorType {
    /** 文件或媒体不存在。 */
    NotFound,

    /** 目标已经存在。 */
    AlreadyExists,

    /** 没有系统权限。 */
    PermissionDenied,

    /** 系统空间不足。 */
    NoSpace,

    /** 平台能力不支持。 */
    Unsupported,

    /** 其他输入输出错误。 */
    Io,
}

/** 页面错误展示模型。 */
@Immutable
internal data class MediaLibraryDemoErrorModel(
    /** 已归一化的错误分类。 */
    val type: MediaLibraryDemoErrorType,
)

/** 批量媒体库操作类型。 */
internal enum class MediaLibraryDemoBatchOperation {
    /** 发布全部可发布样本。 */
    Publish,

    /** 检查全部活动引用。 */
    Check,

    /** 删除全部活动引用。 */
    Delete,
}

/** 批量操作累计结果。 */
@Immutable
internal data class MediaLibraryDemoBatchSummary(
    /** 本批次固定样本总数。 */
    val total: Int = mediaLibraryDemoSamples.size,
    /** 已经处理或跳过的样本数。 */
    val processed: Int = 0,
    /** 成功完成的样本数。 */
    val succeeded: Int = 0,
    /** 平台明确不支持的样本数。 */
    val unsupported: Int = 0,
    /** 发生非 Unsupported 错误的样本数。 */
    val failed: Int = 0,
    /** 因缺少可操作引用而跳过的样本数。 */
    val skipped: Int = 0,
)

/** 批量媒体库操作状态。 */
@Immutable
internal sealed interface MediaLibraryDemoBatchState {
    /** 当前没有批量操作或结果。 */
    data object Idle : MediaLibraryDemoBatchState

    /** 批量操作正在按固定顺序执行。 */
    data class Running(
        /** 当前批量操作类型。 */
        val operation: MediaLibraryDemoBatchOperation,
        /** 当前累计结果。 */
        val summary: MediaLibraryDemoBatchSummary,
    ) : MediaLibraryDemoBatchState

    /** 批量操作已遍历全部样本。 */
    data class Completed(
        /** 已完成的批量操作类型。 */
        val operation: MediaLibraryDemoBatchOperation,
        /** 最终累计结果。 */
        val summary: MediaLibraryDemoBatchSummary,
    ) : MediaLibraryDemoBatchState

    /** 批量操作因全局阻塞错误提前停止。 */
    data class Stopped(
        /** 被停止的批量操作类型。 */
        val operation: MediaLibraryDemoBatchOperation,
        /** 停止时的累计结果。 */
        val summary: MediaLibraryDemoBatchSummary,
        /** 导致停止的结构化错误。 */
        val error: MediaLibraryDemoErrorModel,
    ) : MediaLibraryDemoBatchState
}

/** 返回测试标识对应的稳定定义。 */
internal fun MediaLibraryDemoSampleId.sample(): MediaLibraryDemoSample =
    mediaLibraryDemoSamples.first { sample -> sample.id == this }
