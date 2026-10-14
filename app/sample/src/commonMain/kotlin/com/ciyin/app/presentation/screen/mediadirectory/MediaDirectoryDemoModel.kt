package com.ciyin.app.presentation.screen.mediadirectory

import androidx.compose.runtime.Immutable
import ciyin.media.directory.MediaDirectoryAccess
import ciyin.media.directory.MediaDirectoryAccessToken
import ciyin.media.directory.MediaDirectoryEntry
import ciyin.media.directory.MediaDirectoryError

/** 用户媒体目录示例的状态标识：表示当前是否已经完成目录授权。 */
internal enum class MediaDirectoryDemoAccessState {
    /** 尚未授权任何目录。 */
    NotGranted,

    /** 已授权并能 commit。 */
    Granted,

    /** 最近一次操作发现目录失效。 */
    Revoked,
}

/** 用户媒体目录示例的当前阶段。 */
internal enum class MediaDirectoryDemoPhase {
    /** 页面空闲。 */
    Idle,

    /** 正在拉起系统目录选择器。 */
    Picking,

    /** 正在提交文件。 */
    Committing,

    /** 正在删除文件。 */
    Deleting,

    /** 正在验证目录。 */
    Validating,
}

/** 媒体目录示例中的错误分类。 */
internal enum class MediaDirectoryDemoErrorType {
    Cancelled,
    Unsupported,
    PermissionDenied,
    AccessRevoked,
    AlreadyExists,
    NoSpace,
    NotFound,
    Io,
}

/** 媒体目录错误展示模型。 */
@Immutable
internal data class MediaDirectoryDemoErrorModel(
    val type: MediaDirectoryDemoErrorType,
)

/** 媒体目录示例中最近一次提交得到的条目。 */
@Immutable
internal data class MediaDirectoryDemoEntryModel(
    val fileName: String,
    val location: String,
    val mimeType: String,
    val size: Long,
    val exists: Boolean,
)

/** 媒体目录示例当前授权的目录信息。 */
@Immutable
internal data class MediaDirectoryDemoAccessModel(
    val token: MediaDirectoryAccessToken,
    val displayName: String,
    val canonicalPath: String?,
) {
    companion object {
        /** 从平台返回值构造展示模型。 */
        fun from(access: MediaDirectoryAccess): MediaDirectoryDemoAccessModel =
            MediaDirectoryDemoAccessModel(
                token = access.accessToken,
                displayName = access.displayName,
                canonicalPath = access.canonicalPath,
            )
    }
}

/** 媒体目录示例完整 UI 状态。 */
@Immutable
internal data class MediaDirectoryDemoUiState(
    /** 当前授权状态。 */
    val accessState: MediaDirectoryDemoAccessState = MediaDirectoryDemoAccessState.NotGranted,
    /** 当前授权信息；当 accessState 不是 Granted 时为 null。 */
    val access: MediaDirectoryDemoAccessModel? = null,
    /** 当前阶段。 */
    val phase: MediaDirectoryDemoPhase = MediaDirectoryDemoPhase.Idle,
    /** 最近一次成功提交的文件引用。 */
    val lastEntry: MediaDirectoryDemoEntryModel? = null,
    /** 最近一次结构化错误。 */
    val error: MediaDirectoryDemoErrorModel? = null,
    /** 当前页面是否处于操作中。 */
    val isBusy: Boolean = false,
) {
    /** 当前是否存在已提交的条目。 */
    val hasEntry: Boolean get() = lastEntry != null

    /** 当前是否处于 Idle 状态。 */
    val isIdle: Boolean get() = !isBusy
}

/** 把 [MediaDirectoryError] 转换为页面错误展示模型。 */
internal fun MediaDirectoryError.toDemoModel(): MediaDirectoryDemoErrorModel {
    val type = when (this) {
        MediaDirectoryError.Cancelled -> MediaDirectoryDemoErrorType.Cancelled
        is MediaDirectoryError.Unsupported -> MediaDirectoryDemoErrorType.Unsupported
        is MediaDirectoryError.PermissionDenied -> MediaDirectoryDemoErrorType.PermissionDenied
        is MediaDirectoryError.AccessRevoked -> MediaDirectoryDemoErrorType.AccessRevoked
        is MediaDirectoryError.AlreadyExists -> MediaDirectoryDemoErrorType.AlreadyExists
        MediaDirectoryError.NoSpace -> MediaDirectoryDemoErrorType.NoSpace
        is MediaDirectoryError.NotFound -> MediaDirectoryDemoErrorType.NotFound
        is MediaDirectoryError.Io -> MediaDirectoryDemoErrorType.Io
    }
    return MediaDirectoryDemoErrorModel(type)
}

/** 把 [MediaDirectoryEntry] 转换为页面展示模型。 */
internal fun MediaDirectoryEntry.toDemoModel(exists: Boolean): MediaDirectoryDemoEntryModel =
    MediaDirectoryDemoEntryModel(
        fileName = fileName,
        location = location,
        mimeType = mimeType,
        size = size,
        exists = exists,
    )
