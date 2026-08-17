package com.ciyin.app.ui.screen.medialibrary

import androidx.compose.runtime.Immutable
import ciyin.media.library.MediaLibraryError
import ciyin.media.library.PublishedMedia
import ciyin.platform.Context

/** 系统媒体库测试台动作。 */
@Immutable
internal sealed interface MediaLibraryDemoAction {
    /** 返回 sample 首页。 */
    data object BackClick : MediaLibraryDemoAction

    /** 选择需要查看和操作的媒体测试。 */
    data class SampleSelect(
        /** 目标测试标识。 */
        val sampleId: MediaLibraryDemoSampleId,
    ) : MediaLibraryDemoAction

    /** 发布指定内置媒体。 */
    data class PublishClick(
        /** 当前平台上下文。 */
        val context: Context,
        /** 目标测试标识。 */
        val sampleId: MediaLibraryDemoSampleId,
    ) : MediaLibraryDemoAction

    /** 检查指定媒体引用是否仍存在。 */
    data class ExistsClick(
        /** 当前平台上下文。 */
        val context: Context,
        /** 目标测试标识。 */
        val sampleId: MediaLibraryDemoSampleId,
    ) : MediaLibraryDemoAction

    /** 删除指定媒体引用。 */
    data class DeleteClick(
        /** 当前平台上下文。 */
        val context: Context,
        /** 目标测试标识。 */
        val sampleId: MediaLibraryDemoSampleId,
    ) : MediaLibraryDemoAction

    /** 顺序发布全部可发布测试。 */
    data class PublishAllClick(
        /** 当前平台上下文。 */
        val context: Context,
    ) : MediaLibraryDemoAction

    /** 顺序检查全部活动媒体引用。 */
    data class ExistsAllClick(
        /** 当前平台上下文。 */
        val context: Context,
    ) : MediaLibraryDemoAction

    /** 顺序删除全部活动媒体引用。 */
    data class DeleteAllClick(
        /** 当前平台上下文。 */
        val context: Context,
    ) : MediaLibraryDemoAction

    /** 内置媒体资源大小已经加载。 */
    data class SampleSizesLoaded(
        /** 按测试标识索引的字节大小。 */
        val sizes: Map<MediaLibraryDemoSampleId, Long>,
    ) : MediaLibraryDemoAction

    /** 指定测试的平台操作已经开始。 */
    data class ItemOperationStarted(
        /** 目标测试标识。 */
        val sampleId: MediaLibraryDemoSampleId,
        /** 当前操作类型。 */
        val operation: MediaLibraryDemoBatchOperation,
    ) : MediaLibraryDemoAction

    /** 发布操作成功。 */
    data class Published(
        /** 目标测试标识。 */
        val sampleId: MediaLibraryDemoSampleId,
        /** 平台返回的持久化媒体引用。 */
        val media: PublishedMedia,
    ) : MediaLibraryDemoAction

    /** 存在性检查完成。 */
    data class ExistsLoaded(
        /** 目标测试标识。 */
        val sampleId: MediaLibraryDemoSampleId,
        /** 媒体当前是否存在。 */
        val exists: Boolean,
    ) : MediaLibraryDemoAction

    /** 删除操作完成。 */
    data class Deleted(
        /** 目标测试标识。 */
        val sampleId: MediaLibraryDemoSampleId,
    ) : MediaLibraryDemoAction

    /** 操作失败。 */
    data class OperationFailed(
        /** 目标测试标识。 */
        val sampleId: MediaLibraryDemoSampleId,
        /** 已归一化的技术错误。 */
        val error: MediaLibraryError,
    ) : MediaLibraryDemoAction

    /** 批量操作累计结果发生变化。 */
    data class BatchProgressed(
        /** 当前批量操作类型。 */
        val operation: MediaLibraryDemoBatchOperation,
        /** 最新累计结果。 */
        val summary: MediaLibraryDemoBatchSummary,
    ) : MediaLibraryDemoAction

    /** 批量操作完成或提前停止。 */
    data class BatchFinished(
        /** 已结束的批量操作类型。 */
        val operation: MediaLibraryDemoBatchOperation,
        /** 最终累计结果。 */
        val summary: MediaLibraryDemoBatchSummary,
        /** 提前停止批次的可选全局错误。 */
        val blocker: MediaLibraryError? = null,
    ) : MediaLibraryDemoAction
}
