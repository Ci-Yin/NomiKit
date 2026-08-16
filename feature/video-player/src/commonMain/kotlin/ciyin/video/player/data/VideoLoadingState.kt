package ciyin.video.player.data

import androidx.compose.runtime.Immutable


/** 描述视频加载流程所处阶段。 */
@Immutable
sealed interface VideoLoadingState {
    /** 表示仍在进行中的加载阶段。 */
    sealed interface Progressing : VideoLoadingState

    /**
     * 等待选择
     */
    data object Initial : VideoLoadingState

    /**
     * 在解析磁力链/寻找文件
     */
    data object ResolvingSource : VideoLoadingState, Progressing

    /**
     * WEB: 已经成功解析到 m3u8 链接
     * BT: 要解析磁力链, 查询元数据
     */
    data class DecodingData(
        val isBt: Boolean,
    ) : VideoLoadingState, Progressing

    /**
     * 文件成功找到
     */
    data class Succeed(
        val isBt: Boolean,
    ) : VideoLoadingState, Progressing

    /** 表示加载失败的基础状态。 */
    sealed class Failed : VideoLoadingState

    /** 媒体解析超时。 */
    data object ResolutionTimedOut : Failed()

    /** 网络请求失败。 */
    data object NetworkError : Failed()

    /** 加载被用户取消。 */
    data object Cancelled : Failed()

    /**
     * 不支持的媒体, 或者说是未启用支持该媒体的
     */
    data object UnsupportedMedia : Failed()

    /** 未找到可播放文件。 */
    data object NoMatchingFile : Failed()

    /** 未分类的加载异常。 */
    data class UnknownError(
        val cause: Throwable,
    ) : Failed()
}
