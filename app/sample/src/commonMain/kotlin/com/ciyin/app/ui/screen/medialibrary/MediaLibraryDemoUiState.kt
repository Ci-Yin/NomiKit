package com.ciyin.app.ui.screen.medialibrary

import androidx.compose.runtime.Immutable

/** 单个媒体测试的页面状态。 */
@Immutable
internal data class MediaLibraryDemoItemState(
    /** 稳定测试标识。 */
    val sampleId: MediaLibraryDemoSampleId,
    /** 当前操作阶段。 */
    val phase: MediaLibraryDemoPhase = MediaLibraryDemoPhase.Ready,
    /** 内置源文件字节大小。 */
    val sourceSize: Long? = null,
    /** 最近一次发布成功的媒体信息。 */
    val published: MediaLibraryDemoPublishedModel? = null,
    /** 最近一次存在性检查结果。 */
    val exists: Boolean? = null,
    /** 最近一次结构化错误。 */
    val error: MediaLibraryDemoErrorModel? = null,
) {
    /** 当前项目是否正在执行平台媒体库操作。 */
    val isBusy: Boolean
        get() = phase == MediaLibraryDemoPhase.Publishing ||
            phase == MediaLibraryDemoPhase.Checking ||
            phase == MediaLibraryDemoPhase.Deleting

    /** 当前状态是否仍持有可操作的平台引用。 */
    val hasActiveReference: Boolean
        get() = published != null &&
            exists != false &&
            phase != MediaLibraryDemoPhase.Deleted &&
            phase != MediaLibraryDemoPhase.Missing
}

/** 四类媒体测试的初始状态。 */
internal val defaultMediaLibraryDemoItems = mediaLibraryDemoSamples.map { sample ->
    MediaLibraryDemoItemState(sampleId = sample.id)
}

/** 系统媒体库测试台页面状态。 */
@Immutable
internal data class MediaLibraryDemoUiState(
    /** 当前选中的测试。 */
    val selectedSampleId: MediaLibraryDemoSampleId = MediaLibraryDemoSampleId.Image,
    /** 四类媒体测试的独立状态。 */
    val items: List<MediaLibraryDemoItemState> = defaultMediaLibraryDemoItems,
    /** 当前批量操作状态。 */
    val batch: MediaLibraryDemoBatchState = MediaLibraryDemoBatchState.Idle,
) {
    /** 当前是否正在执行任意单项或批量操作。 */
    val isBusy: Boolean
        get() = batch is MediaLibraryDemoBatchState.Running || items.any { item -> item.isBusy }

    /** 当前选中测试的状态。 */
    val selectedItem: MediaLibraryDemoItemState
        get() = item(selectedSampleId)

    /** 返回指定测试的状态。 */
    fun item(sampleId: MediaLibraryDemoSampleId): MediaLibraryDemoItemState =
        items.first { item -> item.sampleId == sampleId }

    /** 替换指定测试状态并保持列表顺序不变。 */
    fun updateItem(
        sampleId: MediaLibraryDemoSampleId,
        transform: (MediaLibraryDemoItemState) -> MediaLibraryDemoItemState,
    ): MediaLibraryDemoUiState = copy(
        items = items.map { item ->
            if (item.sampleId == sampleId) transform(item) else item
        },
    )
}
