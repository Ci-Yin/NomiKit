package ciyin.video.player.ui.pip

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Rect
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import ciyin.feature.videoPlayer.R
import ciyin.platform.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.togglePause

/** PiP 控制按钮使用的应用内广播 Action。 */
private const val ACTION_PIP_CONTROL = "ciyin.video.player.PIP_CONTROL"

/** 广播中携带控制类型的键。 */
private const val EXTRA_CONTROL_TYPE = "control_type"

/** 快退控制类型。 */
private const val CONTROL_REWIND = 1

/** 播放暂停控制类型。 */
private const val CONTROL_PLAY_PAUSE = 2

/** 快进控制类型。 */
private const val CONTROL_FAST_FORWARD = 3

/** PiP 快进快退的固定跨度。 */
private const val SKIP_DURATION_MS = 15_000L

/** 创建 Android 画中画控制器。 */
actual fun createPipController(
    context: ciyin.platform.Context,
    player: MediampPlayer,
): PipController = AndroidPipController(context, player)

/**
 * Android 画中画控制器。
 *
 * 核心设计：
 * - 通过 [ComponentActivity.addOnPictureInPictureModeChangedListener] 监听 PiP 状态
 * - 使用 [RemoteAction] 实现 PiP 小窗内的快退/播放暂停/快进按钮
 * - [BroadcastReceiver] 动态注册，不需要 Manifest 声明
 * - 退出 PiP 时关闭小窗（`activity.finish()`）
 */
internal class AndroidPipController(
    /** 用于访问 Activity 与系统服务的上下文。 */
    private val context: Context,
    /** 由 PiP 控件操作的播放器。 */
    private val player: MediampPlayer,
) : PipController {

    /** PiP 控制器日志。 */
    private val logger = logger<AndroidPipController>()

    /** 内部 PiP 状态。 */
    private val _isInPipMode = MutableStateFlow(false)

    /** 系统回调驱动的 PiP 状态。 */
    override val isInPipMode: StateFlow<Boolean> = _isInPipMode.asStateFlow()

    /** 当前设备是否声明了 PiP 能力。 */
    override val isPipSupported: Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    /** 从上下文链解析出的宿主 Activity。 */
    private val activity: ComponentActivity?
        get() = context.findActivity()

    /** 广播接收器是否已经注册。 */
    private var receiverRegistered = false

    /** 观察播放状态的主线程作用域。 */
    private val playbackScope = CoroutineScope(Dispatchers.Main.immediate)

    /** 当前播放状态观察任务。 */
    private var playbackObserver: Job? = null

    /** PiP 小窗内按钮的广播接收器（动态注册）。 */
    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.action != ACTION_PIP_CONTROL) return
            when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                CONTROL_REWIND -> player.skip(-SKIP_DURATION_MS)
                CONTROL_PLAY_PAUSE -> player.togglePause()
                CONTROL_FAST_FORWARD -> player.skip(SKIP_DURATION_MS)
            }
            // 操作后刷新按钮图标（播放/暂停图标切换）
            updatePipActions()
        }
    }

    /** PiP 模式变化监听器引用（用于释放时移除）。 */
    private var pipModeChangedListener: Consumer<PictureInPictureModeChangedInfo>? = null

    init {
        setupPipModeListener()
    }

    /** 注册 Activity 的 PiP 模式变化监听器。 */
    private fun setupPipModeListener() {
        val act = activity ?: return
        val listener = Consumer<PictureInPictureModeChangedInfo> { info ->
            _isInPipMode.value = info.isInPictureInPictureMode
            if (info.isInPictureInPictureMode) {
                registerReceiver()
                startPlaybackObserver()
            } else {
                playbackObserver?.cancel()
                unregisterReceiver()
            }
        }
        pipModeChangedListener = listener
        act.addOnPictureInPictureModeChangedListener(listener)
    }

    /** 请求进入 Android 系统 PiP。 */
    override fun enterPip(sourceRect: Rect): Boolean {
        if (!isPipSupported) return false
        val act = activity ?: return false
        try {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .apply {
                    if (sourceRect != Rect.Zero) {
                        setSourceRectHint(sourceRect.toAndroidRect())
                    }
                }
                .setActions(buildRemoteActions())
                .build()
            return act.enterPictureInPictureMode(params)
        } catch (e: IllegalStateException) {
            logger.e(e) { "Failed to enter PiP mode" }
            return false
        }
    }

    /** 关闭当前 PiP Activity。 */
    override fun exitPip() {
        val act = activity ?: return
        try {
            if (_isInPipMode.value) {
                // 关闭小窗（finish Activity），而非将 Activity 带回前台
                act.finish()
            }
        } catch (e: Exception) {
            logger.e(e) { "Failed to exit PiP mode" }
        }
    }

    /** 配置 Android 12 及以上的自动进入 PiP。 */
    override fun setAutoEnterEnabled(enabled: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val act = activity ?: return
        try {
            val params = PictureInPictureParams.Builder()
                .setAutoEnterEnabled(enabled)
                .setAspectRatio(Rational(16, 9))
                .setActions(buildRemoteActions())
                .build()
            act.setPictureInPictureParams(params)
        } catch (e: IllegalStateException) {
            logger.e(e) { "Failed to set auto-enter PiP" }
        }
    }

    /** 释放监听器、广播与观察任务。 */
    override fun release() {
        playbackObserver?.cancel()
        unregisterReceiver()
        val act = activity
        val listener = pipModeChangedListener
        if (act != null && listener != null) {
            act.removeOnPictureInPictureModeChangedListener(listener)
        }
        pipModeChangedListener = null
        _isInPipMode.value = false
    }

    // ==================== 内部方法 ====================

    /** 注册 PiP 控制按钮的广播接收器。 */
    private fun registerReceiver() {
        if (receiverRegistered) return
        try {
            val filter = IntentFilter(ACTION_PIP_CONTROL)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(pipReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(pipReceiver, filter)
            }
            receiverRegistered = true
        } catch (e: Exception) {
            logger.e(e) { "Failed to register PiP receiver" }
        }
    }

    /** 注销 PiP 控制按钮的广播接收器。 */
    private fun unregisterReceiver() {
        if (!receiverRegistered) return
        try {
            context.unregisterReceiver(pipReceiver)
        } catch (e: Exception) {
            logger.e(e) { "Failed to unregister PiP receiver" }
        }
        receiverRegistered = false
    }

    /** 根据当前播放状态刷新 PiP 小窗按钮。 */
    private fun updatePipActions() {
        val act = activity ?: return
        try {
            val params = PictureInPictureParams.Builder()
                .setActions(buildRemoteActions())
                .build()
            act.setPictureInPictureParams(params)
        } catch (e: Exception) {
            logger.e(e) { "Failed to update PiP actions" }
        }
    }

    /** 进入 PiP 后观察播放状态变化，自动刷新按钮图标。 */
    private fun startPlaybackObserver() {
        playbackObserver?.cancel()
        playbackObserver = playbackScope.launch {
            player.playbackState
                .map { it.isPlaying }
                .collect { updatePipActions() }
        }
    }


    /** 按当前播放状态构造 PiP 小窗操作。 */
    private fun buildRemoteActions(): List<RemoteAction> {
        val isPlaying = player.playbackState.value.isPlaying
        val resolver = PipIconResolverRegistry
        return listOf(
            createRemoteAction(
                iconRes = resolver.resolve(PipActionIconKey.Rewind),
                title = context.getString(R.string.video_player_pip_rewind),
                controlType = CONTROL_REWIND,
                requestCode = 0,
            ),
            createRemoteAction(
                iconRes = if (isPlaying) resolver.resolve(PipActionIconKey.Pause)
                else resolver.resolve(PipActionIconKey.Play),
                title = context.getString(
                    if (isPlaying) R.string.video_player_pause else R.string.video_player_play,
                ),
                controlType = CONTROL_PLAY_PAUSE,
                requestCode = 1,
            ),
            createRemoteAction(
                iconRes = resolver.resolve(PipActionIconKey.FastForward),
                title = context.getString(R.string.video_player_pip_fast_forward),
                controlType = CONTROL_FAST_FORWARD,
                requestCode = 2,
            ),
        )
    }

    /** 构造一个发送应用内广播的 PiP 操作。 */
    private fun createRemoteAction(
        iconRes: Int,
        title: String,
        controlType: Int,
        requestCode: Int,
    ): RemoteAction {
        val intent = Intent(ACTION_PIP_CONTROL).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_CONTROL_TYPE, controlType)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return RemoteAction(
            Icon.createWithResource(context, iconRes),
            title,
            title,
            pendingIntent,
        )
    }

    /** 将 Compose 矩形转换为 Android 矩形。 */
    private fun Rect.toAndroidRect(): android.graphics.Rect = android.graphics.Rect(
        left.toInt(),
        top.toInt(),
        right.toInt(),
        bottom.toInt(),
    )
}

/** 从 Context 包装链中查找宿主 Activity。 */
private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
