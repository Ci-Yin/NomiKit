package ciyin.video.player.player

import ciyin.video.player.data.DownloadSpeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.openani.mediamp.ExperimentalMediampApi
import org.openani.mediamp.InternalForInheritanceMediampApi
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.features.AudioLevelController
import org.openani.mediamp.features.Buffering
import org.openani.mediamp.features.MediaMetadata
import org.openani.mediamp.features.PlaybackSpeed
import org.openani.mediamp.features.PlayerFeatures
import org.openani.mediamp.features.Screenshots
import org.openani.mediamp.features.VideoAspectRatio
import org.openani.mediamp.features.buildPlayerFeatures
import org.openani.mediamp.vlc.VlcMediampPlayer
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import kotlin.time.Duration.Companion.seconds

/**
 * Desktop 平台专属播放器包装类，通过接口委托包装 [VlcMediampPlayer]。
 *
 * 仅覆写 [features] 以注入 [DownloadSpeed] Feature，其余行为完全委托给原始播放器。
 */
@OptIn(InternalForInheritanceMediampApi::class, ExperimentalMediampApi::class)
class VlcDownloadSpeedMediampPlayer(
    /** 被装饰的 VLC Mediamp 播放器。 */
    val delegate: VlcMediampPlayer,
    scope: CoroutineScope,
) : MediampPlayer by delegate {

    /** VLC 统计数据提供的下载速度能力。 */
    private val downloadSpeed = VlcDownloadSpeed(delegate.player, scope)

    /** 保留原功能并注入下载速度后的能力集合。 */
    override val features: PlayerFeatures = buildPlayerFeatures {
        delegate.features[Screenshots.Key]?.let { add(Screenshots.Key, it) }
        delegate.features[Buffering]?.let { add(Buffering, it) }
        delegate.features[AudioLevelController]?.let { add(AudioLevelController, it) }
        delegate.features[PlaybackSpeed]?.let { add(PlaybackSpeed, it) }
        delegate.features[MediaMetadata]?.let { add(MediaMetadata, it) }
        delegate.features[VideoAspectRatio]?.let { add(VideoAspectRatio, it) }
        add(DownloadSpeed, downloadSpeed)
    }

}

/**
 * Desktop 端下载速度 Feature 实现，基于 vlcj 媒体统计数据。
 *
 * 通过定期读取 inputBytesRead 的差值计算当前下载速度（字节/秒），采样间隔 1 秒。
 */
internal class VlcDownloadSpeed(
    /** 提供媒体统计数据的 VLC 播放器。 */
    private val vlcPlayer: EmbeddedMediaPlayer,
    scope: CoroutineScope,
) : DownloadSpeed {
    /** 内部下载速度状态。 */
    private val _speedBps = MutableStateFlow(-1L)

    /** 当前估算的下载速度。 */
    override val speedBps: StateFlow<Long> = _speedBps

    /** 上一次采样读取的字节数。 */
    private var lastDemuxBytes: Long = 0L

    init {
        scope.launch {
            while (isActive) {
                val stats = vlcPlayer.media()?.info()?.statistics()
                if (stats != null) {
                    // 使用 demuxBytesRead：对 HLS/m3u8 流比 inputBytesRead 更可靠
                    val currentBytes = stats.demuxBytesRead().toLong()
                    val delta = currentBytes - lastDemuxBytes
                    lastDemuxBytes = currentBytes
                    // delta < 0 表示切换了媒体源（计数器重置），忽略该采样
                    _speedBps.value = if (delta > 0) delta else -1L
                }
                delay(1.seconds)
            }
        }
    }
}
