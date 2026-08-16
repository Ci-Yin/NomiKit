package ciyin.video.player.ui.pip

import android.R
import androidx.annotation.DrawableRes

/**
 * [AndroidPipIconResolver] 默认实现。
 *
 * 将 [PipActionIconKey] 映射为 Android 系统内置的媒体播放图标（`android.R.drawable.ic_media_*`）。
 * 当业务方未通过 [PipIconResolverRegistry.install] 注册自定义解析器时使用此默认映射。
 *
 * @see AndroidPipIconResolver
 * @see PipIconResolverRegistry
 */
internal class DefaultAndroidPipIconResolver : AndroidPipIconResolver {

    /**
     * 根据语义键返回对应的 Android 系统图标资源 ID。
     *
     * 映射关系：
     * - [PipActionIconKey.Rewind] → `android.R.drawable.ic_media_rew`
     * - [PipActionIconKey.Play] → `android.R.drawable.ic_media_play`
     * - [PipActionIconKey.Pause] → `android.R.drawable.ic_media_pause`
     * - [PipActionIconKey.FastForward] → `android.R.drawable.ic_media_ff`
     */
    @DrawableRes
    override fun resolve(key: PipActionIconKey): Int = when (key) {
        is PipActionIconKey.Rewind -> R.drawable.ic_media_rew
        is PipActionIconKey.Play -> R.drawable.ic_media_play
        is PipActionIconKey.Pause -> R.drawable.ic_media_pause
        is PipActionIconKey.FastForward -> R.drawable.ic_media_ff
    }
}
