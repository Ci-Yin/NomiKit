package ciyin.video.player.ui.pip

import androidx.annotation.DrawableRes

/**
 * PiP 图标解析器注册表（单例）。
 *
 * 业务方应用在 Application.onCreate（或 Koin 模块初始化时）调用 [install] 注入自定义图标解析器，
 * 即可替换 PiP 小窗按钮的默认图标资源（android.R.drawable.ic_media_*）为业务自有资源。
 *
 * 使用示例：
 * ```kotlin
 * // 在 Application.onCreate 或 DI 初始化时
 * PipIconResolverRegistry.install { key ->
 *     when (key) {
 *         PipActionIconKey.Rewind -> R.drawable.pip_rewind
 *         PipActionIconKey.Play -> R.drawable.pip_play
 *         PipActionIconKey.Pause -> R.drawable.pip_pause
 *         PipActionIconKey.FastForward -> R.drawable.pip_ff
 *     }
 * }
 * ```
 *
 * 若未调用 [install]，默认使用 [DefaultAndroidPipIconResolver]，
 * 映射到 Android 系统内置媒体图标，与改动前行为一致。
 *
 * @see AndroidPipIconResolver
 * @see DefaultAndroidPipIconResolver
 */
public object PipIconResolverRegistry {

    /** 当前生效的 Android PiP 图标解析器。 */
    @Volatile
    private var resolver: AndroidPipIconResolver = DefaultAndroidPipIconResolver()

    /**
     * 注册自定义 [AndroidPipIconResolver] 替换默认解析器。
     *
     * 线程安全：通过 `@Volatile` 保证后续 [resolve] 调用能立即读到新值。
     *
     * @param custom 自定义解析器，支持 SAM Lambda
     */
    public fun install(custom: AndroidPipIconResolver) {
        resolver = custom
    }

    /**
     * 根据 [PipActionIconKey] 解析 Android Drawable 资源 ID。
     *
     * @param key 图标语义键
     * @return Android Drawable 资源 ID
     */
    @DrawableRes
    public fun resolve(key: PipActionIconKey): Int = resolver.resolve(key)
}
