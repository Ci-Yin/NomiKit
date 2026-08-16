package ciyin.video.player.data

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * 视频播放器进度条的缓存进度
 */
interface MediaCacheProgressProvider {
    /** 持续发布媒体缓存分块状态。 */
    val flow: Flow<MediaCacheProgressInfo>
}

/** 描述缓存分块的权重、状态与字节范围。 */
@Immutable
data class MediaCacheProgressInfo(
    /**
     * 区块的权重列表. 每个区块的宽度由权重决定.
     *
     * 所有 chunks 的 weight 之和应当 (约) 等于 1, 否则将会导致绘制超出进度条的区域 (即会被忽略).
     */
    val chunkWeights: List<Float>,
    /**
     * 区块的状态列表. [chunkStates] 和 [chunkWeights] 的长度应当相等.
     */
    val chunkStates: List<ChunkState>,
) {
    init {
        require(chunkWeights.size == chunkStates.size) {
            "chunkWeights.size (${chunkWeights.size}) != chunkStates.size (${chunkStates.size})"
        }
    }

    companion object {
        /** 不包含任何缓存分块的空值。 */
        val Empty = MediaCacheProgressInfo(
            chunkWeights = listOf(),
            chunkStates = listOf(),
        )
    }

    /** 分块数量。 */
    val size = chunkWeights.size

    /** 最后一个分块索引。 */
    val lastIndex get() = chunkWeights.size - 1

    /** 是否没有任何缓存分块。 */
    fun isEmpty(): Boolean = chunkWeights.isEmpty()
}

/** 单个缓存分块的可用状态。 */
enum class ChunkState {
    /**
     * 初始状态
     */
    NONE,

    /**
     * 正在下载
     */
    DOWNLOADING,

    /**
     * 下载完成
     */
    DONE,

    /**
     * 对应 BT 的没有任何 peer 有这个 piece 的状态
     */
    NOT_AVAILABLE
}

/** 未缓存媒体使用的共享静态提供器。 */
private val StaticMediaCacheProgressStateNone = StaticMediaCacheProgressProvider(ChunkState.NONE)

/** 已完整缓存媒体使用的共享静态提供器。 */
private val StaticMediaCacheProgressStateDone = StaticMediaCacheProgressProvider(ChunkState.DONE)

/** 创建固定缓存状态的进度提供器。 */
fun staticMediaCacheProgressState(
    chunkState: ChunkState
): MediaCacheProgressProvider {
    if (chunkState == ChunkState.NONE) return StaticMediaCacheProgressStateNone
    if (chunkState == ChunkState.DONE) return StaticMediaCacheProgressStateDone
    return StaticMediaCacheProgressProvider(chunkState)
}

/** 始终发布同一缓存状态的进度提供器。 */
private class StaticMediaCacheProgressProvider(chunkState: ChunkState) :
    MediaCacheProgressProvider {
    /** 固定缓存状态流。 */
    override val flow: Flow<MediaCacheProgressInfo> = flowOf(
        MediaCacheProgressInfo(
            chunkWeights = listOf(1f),
            chunkStates = listOf(chunkState),
        ),
    )
}
