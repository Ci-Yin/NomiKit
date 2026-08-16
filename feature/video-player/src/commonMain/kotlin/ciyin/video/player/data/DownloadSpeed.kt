package ciyin.video.player.data

import kotlinx.coroutines.flow.StateFlow
import org.openani.mediamp.features.Feature
import org.openani.mediamp.features.FeatureKey

/**
 * 下载速度 Feature，作为 [Feature] 注册到播放器的 features 中。
 *
 * 各平台通过包装类（ExoPlayerMediampPlayer / VlcDownloadSpeedMediampPlayer / AvKitDownloadSpeedMediampPlayer）
 * 在 [buildPlayerFeatures] 中注入对应实现，UI 层通过 `player.features[DownloadSpeed]` 统一访问。
 */
interface DownloadSpeed : Feature {
    companion object : FeatureKey<DownloadSpeed>

    /** 下载速度（字节/秒），-1 表示未知 */
    val speedBps: StateFlow<Long>
}
