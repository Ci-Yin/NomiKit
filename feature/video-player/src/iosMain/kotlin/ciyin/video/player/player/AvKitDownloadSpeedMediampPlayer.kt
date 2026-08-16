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
import org.openani.mediamp.avkit.AVKitMediampPlayer
import org.openani.mediamp.features.AudioLevelController
import org.openani.mediamp.features.Buffering
import org.openani.mediamp.features.PlaybackSpeed
import org.openani.mediamp.features.PlayerFeatures
import org.openani.mediamp.features.VideoAspectRatio
import org.openani.mediamp.features.buildPlayerFeatures
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItemAccessLogEvent
import platform.AVFoundation.accessLog
import platform.AVFoundation.currentItem
import kotlin.time.Duration.Companion.seconds

/**
 * iOS 平台专属播放器包装类，通过接口委托包装 [AVKitMediampPlayer]。
 *
 * 仅覆写 [features] 以注入 [DownloadSpeed] Feature，其余行为完全委托给原始播放器。
 */
@OptIn(InternalForInheritanceMediampApi::class, ExperimentalMediampApi::class)
class AvKitDownloadSpeedMediampPlayer(
    /** 被装饰的 AVKit Mediamp 播放器。 */
    val delegate: AVKitMediampPlayer,
    scope: CoroutineScope,
) : MediampPlayer by delegate {

    /** AVKit 日志提供的下载速度能力。 */
    private val downloadSpeed = AVKitDownloadSpeed(delegate.impl, scope)

    /** 保留原功能并注入下载速度后的能力集合。 */
    override val features: PlayerFeatures = buildPlayerFeatures {
        delegate.features[Buffering]?.let { add(Buffering, it) }
        delegate.features[AudioLevelController]?.let { add(AudioLevelController, it) }
        delegate.features[PlaybackSpeed]?.let { add(PlaybackSpeed, it) }
        delegate.features[VideoAspectRatio]?.let { add(VideoAspectRatio, it) }
        add(DownloadSpeed, downloadSpeed)
    }
}

/**
 * iOS 端下载速度 Feature 实现，基于 AVPlayer 的 accessLog 统计数据。
 *
 * 通过定期读取 observedBitrate（bits/s）转换为字节/秒，采样间隔 1 秒。
 */
internal class AVKitDownloadSpeed(
    /** 提供访问日志的原生 AVPlayer。 */
    private val avPlayer: AVPlayer,
    scope: CoroutineScope,
) : DownloadSpeed {
    /** 内部下载速度状态。 */
    private val _speedBps = MutableStateFlow(-1L)

    /** 当前估算的下载速度。 */
    override val speedBps: StateFlow<Long> = _speedBps

    init {
        scope.launch {
            while (isActive) {
                val event = avPlayer.currentItem?.accessLog()?.events?.lastOrNull()
                        as? AVPlayerItemAccessLogEvent
                val bitrate = event?.observedBitrate ?: -1.0 // bits/s
                _speedBps.value = if (bitrate > 0) (bitrate / 8).toLong() else -1L
                delay(1.seconds)
            }
        }
    }
}
