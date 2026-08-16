package ciyin.video.player.ui.pip

import androidx.annotation.DrawableRes

/**
 * Android 平台 PiP 动作图标解析器接口。
 *
 * 用于将 commonMain 中的 [PipActionIconKey] 语义键映射为 Android [DrawableRes] 资源 ID。
 * 单抽象方法接口（SAM），支持 Lambda 方式实现。
 *
 * 业务方可通过 [PipIconResolverRegistry.install] 注册自定义解析器以替换默认图标。
 */
public fun interface AndroidPipIconResolver {

    /**
     * 根据语义键解析对应的 Android 图标资源 ID。
     *
     * @param key PiP 动作图标语义键
     * @return Android Drawable 资源 ID（`@DrawableRes`）
     */
    @DrawableRes
    public fun resolve(key: PipActionIconKey): Int
}
