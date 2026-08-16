@file:kotlin.OptIn(InternalMediampApi::class)

package ciyin.video.player.player

import android.content.Context
import android.util.Pair
import androidx.annotation.MainThread
import androidx.annotation.OptIn
import androidx.annotation.UiThread
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.trackselection.TrackSelection
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import ciyin.platform.logger
import ciyin.video.player.data.DownloadSpeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.openani.mediamp.AbstractMediampPlayer
import org.openani.mediamp.ExperimentalMediampApi
import org.openani.mediamp.InternalForInheritanceMediampApi
import org.openani.mediamp.InternalMediampApi
import org.openani.mediamp.PlaybackState
import org.openani.mediamp.features.AspectRatioMode
import org.openani.mediamp.features.Buffering
import org.openani.mediamp.features.MediaMetadata
import org.openani.mediamp.features.PlaybackSpeed
import org.openani.mediamp.features.PlayerFeatures
import org.openani.mediamp.features.VideoAspectRatio
import org.openani.mediamp.features.buildPlayerFeatures
import org.openani.mediamp.internal.MutableTrackGroup
import org.openani.mediamp.io.SeekableInput
import org.openani.mediamp.metadata.AudioTrack
import org.openani.mediamp.metadata.Chapter
import org.openani.mediamp.metadata.MediaProperties
import org.openani.mediamp.metadata.SubtitleTrack
import org.openani.mediamp.metadata.TrackLabel
import org.openani.mediamp.source.MediaData
import org.openani.mediamp.source.SeekableInputMediaData
import org.openani.mediamp.source.UriMediaData
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds
import androidx.media3.common.Player as Media3Player


/**
 * 基于 ExoPlayer 实现的 Mediamp 播放器适配器。
 *
 * 此类将 Android 平台的 [ExoPlayer] 封装为 Mediamp 框架的 [AbstractMediampPlayer]，
 * 提供统一的跨平台播放器接口。支持以下媒体数据源类型：
 *
 * - [UriMediaData]: 通过 HTTP/HTTPS URL 播放在线视频
 * - [SeekableInputMediaData]: 通过可寻址输入流播放本地或自定义来源的视频
 *
 * ## 核心功能
 *
 * - **播放控制**: 播放、暂停、seek、停止
 * - **字幕轨道管理**: 自动检测并支持切换字幕轨道
 * - **音频轨道管理**: 支持多音轨检测
 * - **播放速度调节**: 支持变速播放
 * - **视频宽高比**: 支持多种宽高比模式（适应、填充、拉伸等）
 * - **缓冲状态监控**: 实时报告缓冲进度
 *
 * ## 线程安全
 *
 * - 构造函数必须在 UI 线程调用（[UiThread] 注解）
 * - ExoPlayer 的所有操作都通过 [Dispatchers.Main] 调度
 * - 播放位置和缓冲进度通过后台协程每 100ms 更新一次
 *
 * ## 字幕选择机制
 *
 * 使用自定义的 [DefaultTrackSelector] 实现智能字幕轨道匹配，
 * 支持通过轨道 ID 或标签进行匹配。
 *
 * ## 使用示例
 *
 * ```kotlin
 * // 在 UI 线程创建播放器
 * val player = ExoPlayerMediampPlayer(context, coroutineScope.coroutineContext)
 *
 * // 设置媒体数据并开始播放
 * val mediaData = UriMediaData(uri = "https://example.com/video.mp4")
 * player.setVideoData(mediaData)
 *
 * // 控制播放
 * player.pause()
 * player.resume()
 * player.seekTo(30_000) // seek 到 30 秒
 *
 * // 释放资源
 * player.close()
 * ```
 *
 * @param context Android 上下文，用于创建 ExoPlayer 实例
 * @param parentCoroutineContext 父协程上下文，用于后台任务调度
 *
 * @see AbstractMediampPlayer
 * @see ExoPlayer
 * @see UriMediaData
 * @see SeekableInputMediaData
 */
@OptIn(UnstableApi::class)
@kotlin.OptIn(InternalMediampApi::class, InternalForInheritanceMediampApi::class)
class ExoPlayerMediampPlayer @UiThread constructor(
    context: Context,
    parentCoroutineContext: CoroutineContext,
) : AbstractMediampPlayer<ExoPlayerMediampPlayer.ExoPlayerData>(parentCoroutineContext) {

    companion object {
        /**
         * 播放器日志记录器，用于记录播放错误等信息。
         */
        val Logger = logger<ExoPlayerMediampPlayer>()
    }

    /**
     * ExoPlayer 专用的媒体数据封装类。
     *
     * 封装了媒体数据、资源释放回调以及设置媒体源的挂起函数。
     * 继承自 [Data]，遵循 Mediamp 框架的数据生命周期管理。
     *
     * @param mediaData 原始媒体数据
     * @param releaseResource 资源释放回调，在播放结束或切换媒体时调用
     * @param setMedia 设置媒体源的挂起函数，配置 ExoPlayer 的 MediaSource
     */
    class ExoPlayerData(
        mediaData: MediaData,
        releaseResource: () -> Unit,
        val setMedia: suspend () -> Unit,
    ) : Data(mediaData, releaseResource)

    /**
     * 启动播放器并开始播放指定的媒体数据。
     *
     * 在主线程上执行以下操作：
     * 1. 调用 [ExoPlayerData.setMedia] 配置媒体源
     * 2. 调用 [ExoPlayer.prepare] 准备播放
     * 3. 调用 [ExoPlayer.play] 开始播放
     *
     * @param data 已准备好的 ExoPlayer 数据，包含媒体源配置
     */
    override suspend fun startPlayer(data: ExoPlayerData) {
        withContext(Dispatchers.Main.immediate) {
            data.setMedia()
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    /**
     * 根据媒体数据类型创建对应的 [ExoPlayerData]。
     *
     * 支持两种媒体数据类型：
     *
     * ## UriMediaData（HTTP/HTTPS 流）
     * - 使用 [DefaultHttpDataSource] 处理网络请求
     * - 支持自定义 HTTP 头（如 User-Agent、认证信息等）
     * - 支持外挂字幕文件配置
     * - 连接超时设置为 30 秒
     *
     * ## SeekableInputMediaData（可寻址输入流）
     * - 对于 `file://` 协议，使用 [FileDataSource] 直接读取本地文件
     * - 对于其他来源，使用 [SeekableInputDataSource] 包装自定义输入流
     * - 适用于种子下载、自定义协议等场景
     *
     * @param data 媒体数据，可以是 [UriMediaData] 或 [SeekableInputMediaData]
     * @return 封装好的 [ExoPlayerData]，包含媒体源配置和资源释放逻辑
     */
    override suspend fun setDataImpl(data: MediaData): ExoPlayerData = when (data) {
        is UriMediaData -> {
            ExoPlayerData(
                data,
                releaseResource = {
                    data.close()
                },
                setMedia = {
                    val headers = data.headers
                    val item = MediaItem.Builder().apply {
                        setUri(data.uri)
                        setSubtitleConfigurations(
                            data.extraFiles.subtitles.map {
                                MediaItem.SubtitleConfiguration.Builder(
                                    it.uri.toUri(),
                                ).apply {
                                    it.label?.let { label -> setLabel(label) }
                                    it.mimeType?.let { mimeType -> setMimeType(mimeType) }
                                    it.language?.let { language -> setLanguage(language) }
                                }.build()
                            },
                        )
                    }.build()
                    withContext(Dispatchers.Main.immediate) {
                        exoPlayer.setMediaSource(
                            DefaultMediaSourceFactory(
                                DefaultHttpDataSource.Factory()
                                    .setUserAgent(
                                        headers["User-Agent"]
                                            ?: """Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3""",
                                    )
                                    .setDefaultRequestProperties(headers)
                                    .setAllowCrossProtocolRedirects(true)
                                    .setConnectTimeoutMs(30_000),
                            ).createMediaSource(item),
                        )
                    }
                },
            )
        }

        is SeekableInputMediaData -> {
            var file: SeekableInput? = null
            ExoPlayerData(
                data,
                releaseResource = {
                    file?.close()
                    data.close()
                },
                setMedia = {
                    if (data.uri.startsWith("file://")) {
                        val factory = DefaultMediaSourceFactory {
                            FileDataSource()
                        }

                        exoPlayer.setMediaSource(
                            factory.createMediaSource(
                                MediaItem.fromUri(data.uri),
                            ),
                        )
                    } else {
                        file = withContext(Dispatchers.IO) {
                            data.createInput()
                        }
                        val factory = ProgressiveMediaSource.Factory {
                            SeekableInputDataSource(data, file)
                        }

                        exoPlayer.setMediaSource(
                            factory.createMediaSource(
                                MediaItem.fromUri(data.uri),
                            ),
                        )
                    }
                },
            )
        }
    }

    /**
     * ExoPlayer 实例，负责实际的媒体播放。
     *
     * 在构造时创建，配置了以下特性：
     *
     * ## 自定义轨道选择器
     * 使用自定义的 [DefaultTrackSelector] 实现字幕轨道智能匹配：
     * - 优先匹配用户选择的字幕轨道（通过 [mediaMetadataFeature.subtitleTracks.selected]）
     * - 支持通过轨道 ID 或标签进行匹配
     * - 无匹配时回退到默认选择逻辑
     *
     * ## 播放器监听器
     * 注册了全面的事件监听器，处理：
     * - 媒体项切换（重置播放状态）
     * - 轨道变更（更新字幕/音频轨道列表）
     * - 播放错误（记录日志并更新状态）
     * - 视频尺寸变化（更新媒体属性）
     * - 播放状态变化（同步缓冲状态）
     * - 播放/暂停状态变化
     * - 播放速度变化
     */
    private val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()

    /** 实际执行 Android 媒体播放的 ExoPlayer。 */
    private val exoPlayer: ExoPlayer = run {
        ExoPlayer.Builder(context).apply {
            setBandwidthMeter(bandwidthMeter)
            setTrackSelector(
                // 自定义轨道选择器，实现字幕轨道的智能匹配
                object : DefaultTrackSelector(context) {
                    /**
                     * 选择字幕轨道的自定义实现。
                     *
                     * 匹配逻辑：
                     * 1. 检查是否有用户选择的首选字幕轨道
                     * 2. 如果有，遍历所有文本轨道组寻找匹配
                     * 3. 匹配规则：轨道 ID 相同，或标签值相同
                     * 4. 无匹配时回退到父类默认实现
                     */
                    override fun selectTextTrack(
                        mappedTrackInfo: MappedTrackInfo,
                        rendererFormatSupports: Array<out Array<IntArray>>,
                        params: Parameters,
                        selectedAudioLanguage: String?
                    ): Pair<ExoTrackSelection.Definition, Int>? {
                        val preferred = mediaMetadataFeature.subtitleTracks.selected.value
                            ?: return super.selectTextTrack(
                                mappedTrackInfo,
                                rendererFormatSupports,
                                params,
                                selectedAudioLanguage,
                            )

                        /**
                         * 检查 [SubtitleTrack] 是否与 ExoPlayer 的 [TrackGroup] 匹配。
                         *
                         * 匹配条件（满足任一即可）：
                         * - 轨道的 internalId 与 TrackGroup 的 id 相同
                         * - 轨道的标签值与 TrackGroup 中任一格式的首个标签值相同
                         */
                        infix fun SubtitleTrack.matches(group: TrackGroup): Boolean {
                            if (this.internalId == group.id) return true

                            if (this.labels.isEmpty()) return false
                            for (index in 0 until group.length) {
                                val format = group.getFormat(index)
                                if (format.labels.isEmpty()) {
                                    continue
                                }
                                if (this.labels.any { it.value == format.labels.first().value }) {
                                    return true
                                }
                            }
                            return false
                        }

                        // 备注: 这个实现可能并不好, 他只是恰好能跑
                        // 遍历所有渲染器，查找文本类型的渲染器
                        for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
                            if (C.TRACK_TYPE_TEXT != mappedTrackInfo.getRendererType(rendererIndex)) continue

                            // 在文本渲染器中查找匹配的轨道组
                            val groups = mappedTrackInfo.getTrackGroups(rendererIndex)
                            for (groupIndex in 0 until groups.length) {
                                val trackGroup = groups[groupIndex]
                                if (preferred matches trackGroup) {
                                    return Pair(
                                        ExoTrackSelection.Definition(
                                            trackGroup,
                                            IntArray(trackGroup.length) { it }, // 如果选择所有字幕会闪烁
                                            TrackSelection.TYPE_UNSET,
                                        ),
                                        rendererIndex,
                                    )
                                }
                            }
                        }
                        return super.selectTextTrack(
                            mappedTrackInfo,
                            rendererFormatSupports,
                            params,
                            selectedAudioLanguage,
                        )
                    }
                },
            )
            setRenderersFactory(
                DefaultRenderersFactory(context)
                    .setEnableDecoderFallback(true)
            )
        }.build().apply {
            // 启用自动播放，当媒体准备好后立即开始播放
            playWhenReady = true

            setHandleAudioBecomingNoisy(true)
            // 注册播放器事件监听器
            addListener(
                object : Media3Player.Listener {
                    /**
                     * 媒体项切换时的回调。
                     *
                     * 当切换到新的媒体项时，重置播放状态为 [PlaybackState.READY]，
                     * 并清除缓冲状态。
                     *
                     * @param mediaItem 新的媒体项，可能为 null
                     * @param reason 切换原因
                     */
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        this@ExoPlayerMediampPlayer.playbackState.value = PlaybackState.READY
                        buffering.isBuffering.value = false
                    }

                    /**
                     * 轨道信息变更时的回调。
                     *
                     * 解析并更新字幕轨道和音频轨道列表。当字幕轨道发生变化时，
                     * 自动选择第一个可用的字幕轨道作为默认选项。
                     *
                     * @param tracks 新的轨道信息
                     */
                    override fun onTracksChanged(tracks: Tracks) {
                        // 提取所有字幕轨道
                        val newSubtitleTracks =
                            tracks.groups.asSequence()
                                .filter { it.type == C.TRACK_TYPE_TEXT }
                                .flatMapIndexed { groupIndex: Int, group: Tracks.Group ->
                                    group.getSubtitleTracks()
                                }
                                .toList()
                        // 新的字幕轨道和原来不同时才会更改，同时将 current 设置为新字幕轨道列表的第一个
                        if (newSubtitleTracks != mediaMetadataFeature.subtitleTracks.candidates.value) {
                            mediaMetadataFeature.subtitleTracks.candidates.value = newSubtitleTracks
                            mediaMetadataFeature.subtitleTracks.selected.value =
                                newSubtitleTracks.firstOrNull()
                        }

                        // 提取所有音频轨道
                        mediaMetadataFeature.audioTracks.candidates.value =
                            tracks.groups.asSequence()
                                .filter { it.type == C.TRACK_TYPE_AUDIO }
                                .flatMapIndexed { groupIndex: Int, group: Tracks.Group ->
                                    group.getAudioTracks()
                                }
                                .toList()
                    }

                    /**
                     * 播放错误时的回调。
                     *
                     * 将播放状态设为 [PlaybackState.ERROR] 并记录错误日志。
                     *
                     * @param error 播放异常，包含错误码和详细信息
                     */
                    override fun onPlayerError(error: PlaybackException) {
                        this@ExoPlayerMediampPlayer.playbackState.value = PlaybackState.ERROR
                        Logger.e(error) { "ExoPlayer error: ${error.message}" }
                    }

                    /**
                     * 视频尺寸变化时的回调。
                     *
                     * 当视频解码器报告新的视频尺寸时触发，用于更新媒体属性。
                     *
                     * @param videoSize 新的视频尺寸信息
                     */
                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        super.onVideoSizeChanged(videoSize)
                        updateVideoProperties()
                    }

                    /**
                     * 更新媒体属性信息。
                     *
                     * 从 ExoPlayer 读取当前媒体的视频格式、音频格式、标题和时长，
                     * 并更新 [mediaProperties] 状态流。
                     *
                     * **注意**: 此方法必须在主线程调用，因为 ExoPlayer 的所有属性访问都需要在主线程。
                     *
                     * @return 如果成功获取并更新了属性返回 `true`，否则返回 `false`
                     */
                    @MainThread
                    private fun updateVideoProperties(): Boolean {
                        val video = videoFormat ?: return false
                        val audio = audioFormat ?: return false
                        val title = mediaMetadata.title
                        val duration = duration

                        // 注意, 要把所有 UI 属性全都读出来然后 captured 到 background -- ExoPlayer 所有属性都需要在主线程
                        mediaProperties.value = MediaProperties(
                            title = title?.toString(),
                            durationMillis = duration,
                        )
                        return true
                    }

                    /**
                     * ExoPlayer 播放状态变化时的回调。
                     *
                     * 处理以下状态：
                     * - [Media3Player.STATE_BUFFERING]: 正在缓冲
                     * - [Media3Player.STATE_ENDED]: 播放完成
                     * - [Media3Player.STATE_READY]: 缓冲完成，准备播放
                     *
                     * **重要说明**: ExoPlayer 的 STATE_READY 表示当前帧缓冲结束，
                     * 与 [PlaybackState.READY] 的语义不同。[PlaybackState.READY] 在
                     * [onMediaItemTransition] 中设置，表示媒体已准备好开始播放。
                     *
                     * @param playbackState ExoPlayer 的播放状态常量
                     */
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Media3Player.STATE_BUFFERING -> {
                                buffering.isBuffering.value = true
                            }

                            Media3Player.STATE_ENDED -> {
                                this@ExoPlayerMediampPlayer.playbackState.value =
                                    PlaybackState.FINISHED
                                buffering.isBuffering.value = false
                            }

                            Media3Player.STATE_READY -> {
                                buffering.isBuffering.value = false
                            }
                        }
                        updateVideoProperties()
                    }

                    /**
                     * 播放/暂停状态变化时的回调。
                     *
                     * 当 ExoPlayer 开始或停止实际播放时触发，
                     * 同步更新 [playbackState] 为 [PlaybackState.PLAYING] 或 [PlaybackState.PAUSED]。
                     *
                     * @param isPlaying `true` 表示正在播放，`false` 表示已暂停
                     */
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            this@ExoPlayerMediampPlayer.playbackState.value = PlaybackState.PLAYING
                            buffering.isBuffering.value = false
                        } else {
                            this@ExoPlayerMediampPlayer.playbackState.value = PlaybackState.PAUSED
                        }
                    }

                    /**
                     * 播放参数变化时的回调。
                     *
                     * 当播放速度等参数变化时触发，同步更新 [playbackSpeed] 状态流。
                     *
                     * @param playbackParameters 新的播放参数，包含速度、音调等信息
                     */
                    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                        super.onPlaybackParametersChanged(playbackParameters)
                        playbackSpeed.valueFlow.value = playbackParameters.speed
                    }
                },
            )
        }
    }

    /**
     * 获取底层 ExoPlayer 实例。
     *
     * 可用于直接访问 ExoPlayer 的高级功能，但应谨慎使用，
     * 优先通过 Mediamp 接口操作播放器。
     */
    override val impl: ExoPlayer get() = exoPlayer

    /**
     * 当前媒体的属性信息状态流。
     *
     * 包含媒体标题、时长等信息，在媒体加载完成后更新。
     * 初始值为 `null`，表示尚未加载媒体或属性未获取。
     */
    override val mediaProperties = MutableStateFlow<MediaProperties?>(null)

    /**
     * 缓冲状态特性实现。
     *
     * 提供缓冲进度和是否正在缓冲的状态信息。
     */
    private val buffering = ExoPlayerBuffering()

    /**
     * 下载速度 Feature 实现，注册到 [features] 中供 UI 通过 `features[DownloadSpeed]` 访问。
     */
    private val downloadSpeed = ExoPlayerDownloadSpeed()

    /**
     * 媒体元数据特性实现。
     *
     * 管理字幕轨道和音频轨道的列表及选择状态。
     */
    private val mediaMetadataFeature = ExoPlayerMediaMetadata()

    /**
     * 播放速度特性实现。
     *
     * 提供播放速度的获取和设置功能。
     */
    private val playbackSpeed = PlaybackSpeedImpl(exoPlayer)

    /**
     * 视频宽高比特性实现。
     *
     * 提供视频显示宽高比模式的获取和设置功能。
     */
    private val videoAspectRatio = ExoPlayerVideoAspectRatio()

    /**
     * 当前播放位置（毫秒）状态流。
     *
     * 每 100ms 从 ExoPlayer 同步一次，用于 UI 显示进度条等。
     * 在 seek 操作时会立即更新以提供即时反馈。
     */
    override val currentPositionMillis: MutableStateFlow<Long> = MutableStateFlow(0)

    /**
     * 播放器支持的特性集合。
     *
     * 包含以下特性：
     * - [PlaybackSpeed]: 播放速度控制
     * - [Buffering]: 缓冲状态监控
     * - [MediaMetadata]: 字幕/音频轨道管理
     * - [VideoAspectRatio]: 视频宽高比控制
     */
    @kotlin.OptIn(ExperimentalMediampApi::class)
    override val features: PlayerFeatures = buildPlayerFeatures {
        add(PlaybackSpeed, playbackSpeed)
        add(Buffering, buffering)
        add(MediaMetadata, mediaMetadataFeature)
        add(VideoAspectRatio, videoAspectRatio)
        add(DownloadSpeed, downloadSpeed)
    }

    /**
     * 获取当前媒体属性的快照。
     *
     * @return 当前媒体属性，如果尚未加载媒体则返回 `null`
     */
    override fun getCurrentMediaProperties(): MediaProperties? {
        return mediaProperties.value
    }

    /**
     * 获取当前播放状态的快照。
     *
     * @return 当前播放状态枚举值
     */
    override fun getCurrentPlaybackState(): PlaybackState {
        return playbackState.value
    }

    init {
        // 启动后台协程，定期同步播放位置和缓冲进度
        // 以 10 fps（每 100ms）的频率更新，平衡性能和响应性
        backgroundScope.launch(Dispatchers.Main) {
            while (currentCoroutineContext().isActive) {
                currentPositionMillis.value = exoPlayer.currentPosition
                buffering.bufferedPercentage.value = exoPlayer.bufferedPercentage
                val bps = bandwidthMeter.bitrateEstimate // bits/s
                downloadSpeed.speedBps.value = if (bps > 0) bps / 8 else -1L // → bytes/s
                delay(0.1.seconds) // 10 fps
            }
        }

        // 监听字幕轨道选择变化，同步到 ExoPlayer 的轨道选择参数
        backgroundScope.launch(Dispatchers.Main) {
            mediaMetadataFeature.subtitleTracks.selected.collect {
                exoPlayer.trackSelectionParameters =
                    exoPlayer.trackSelectionParameters.buildUpon().apply {
                        // 设置一个虚拟的语言偏好来触发轨道重新选择，实际匹配由自定义选择器完成
                        setPreferredTextLanguage(it?.internalId) // dummy value to trigger a select, we have custom selector
                        // 当用户选择关闭字幕时，禁用文本轨道
                        setTrackTypeDisabled(
                            C.TRACK_TYPE_TEXT,
                            it == null
                        ) // disable subtitle track
                    }.build()
            }
        }
    }

    /**
     * 跳转到指定播放位置。
     *
     * 立即更新 [currentPositionMillis] 状态流以提供即时 UI 反馈，
     * 然后调用 ExoPlayer 执行实际的 seek 操作。
     *
     * @param positionMillis 目标位置（毫秒）
     */
    override fun seekTo(positionMillis: Long) {
        currentPositionMillis.value = positionMillis
        exoPlayer.seekTo(positionMillis)
    }

    /**
     * 获取当前播放位置的快照。
     *
     * 直接从 ExoPlayer 读取，比 [currentPositionMillis] 状态流更精确。
     *
     * @return 当前播放位置（毫秒）
     */
    override fun getCurrentPositionMillis(): Long = exoPlayer.currentPosition

    /**
     * 暂停播放。
     *
     * 同时设置 `playWhenReady = false` 并调用 `pause()`，
     * 确保播放器在 seek 等操作后不会自动恢复播放。
     */
    override fun pause() {
        exoPlayer.playWhenReady = false
        exoPlayer.pause()
    }

    /**
     * 恢复播放。
     *
     * 同时设置 `playWhenReady = true` 并调用 `play()`，
     * 确保播放器在缓冲完成后自动继续播放。
     */
    override fun resume() {
        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }

    /**
     * 从 ExoPlayer 轨道组中提取字幕轨道信息。
     *
     * 将 ExoPlayer 的 [Tracks.Group] 转换为 Mediamp 的 [SubtitleTrack] 序列。
     * 每个轨道包含唯一 ID、内部 ID、显示名称和标签列表。
     *
     * @receiver ExoPlayer 的轨道组
     * @return [SubtitleTrack] 序列
     */
    private fun Tracks.Group.getSubtitleTracks() = sequence {
        repeat(length) { index ->
            val format = getTrackFormat(index)
            val firstLabel = format.labels.firstNotNullOfOrNull { it.value }
            format.metadata
            this.yield(
                SubtitleTrack(
                    "${mediaTrackGroup.id}-$index",
                    mediaTrackGroup.id,
                    firstLabel ?: mediaTrackGroup.id,
                    format.labels.map { TrackLabel(it.language, it.value) },
                ),
            )
        }
    }

    /**
     * 从 ExoPlayer 轨道组中提取音频轨道信息。
     *
     * 将 ExoPlayer 的 [Tracks.Group] 转换为 Mediamp 的 [AudioTrack] 序列。
     * 每个轨道包含唯一 ID、内部 ID、显示名称和标签列表。
     *
     * @receiver ExoPlayer 的轨道组
     * @return [AudioTrack] 序列
     */
    private fun Tracks.Group.getAudioTracks() = sequence {
        repeat(length) { index ->
            val format = getTrackFormat(index)
            val firstLabel = format.labels.firstNotNullOfOrNull { it.value }
            format.metadata
            this.yield(
                AudioTrack(
                    "${mediaTrackGroup.id}-$index",
                    mediaTrackGroup.id,
                    firstLabel ?: mediaTrackGroup.id,
                    format.labels.map { TrackLabel(it.language, it.value) },
                ),
            )
        }
    }

    /**
     * 停止当前播放并清理媒体项。
     *
     * 执行以下操作：
     * 1. 停止 ExoPlayer 播放
     * 2. 清除所有已加载的媒体项
     * 3. 重置播放位置为 0
     */
    override fun stopPlaybackImpl() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        currentPositionMillis.value = 0
    }

    /**
     * 关闭播放器并释放所有资源。
     *
     * 执行以下操作：
     * 1. 停止 ExoPlayer 播放
     * 2. 释放 ExoPlayer 实例及其持有的所有资源
     *
     * **注意**: 调用此方法后，播放器实例不可再使用。
     */
    override fun closeImpl() {
        exoPlayer.stop()
        exoPlayer.release()
    }
}

/**
 * 播放速度特性的 ExoPlayer 实现。
 *
 * 将 Mediamp 的 [PlaybackSpeed] 接口适配到 ExoPlayer 的播放速度控制 API。
 * 支持变速播放，速度值会被强制限制为非负数。
 *
 * @param exoPlayer ExoPlayer 实例，用于设置实际的播放速度
 */
@kotlin.OptIn(InternalForInheritanceMediampApi::class)
internal class PlaybackSpeedImpl(
    /** 实际应用倍速的 ExoPlayer。 */
    private val exoPlayer: ExoPlayer,
) : PlaybackSpeed {

    /**
     * 当前播放速度的状态流。
     *
     * 初始值为 1.0（正常速度），在调用 [set] 时更新。
     * 同时会被 ExoPlayer 的播放参数变化回调更新。
     */
    override val valueFlow: MutableStateFlow<Float> = MutableStateFlow(1f)

    /**
     * 获取当前播放速度的快照。
     */
    override val value: Float get() = valueFlow.value

    /**
     * 设置播放速度。
     *
     * 速度值会被强制限制为非负数（至少为 0）。
     * 同时更新状态流和 ExoPlayer 的实际播放速度。
     *
     * @param speed 目标播放速度，1.0 为正常速度，2.0 为两倍速等
     */
    override fun set(speed: Float) {
        valueFlow.value = speed.coerceAtLeast(0f)
        exoPlayer.setPlaybackSpeed(speed)
    }
}


/**
 * 媒体元数据特性的 ExoPlayer 实现。
 *
 * 管理媒体的字幕轨道、音频轨道和章节信息。
 * 轨道列表由 ExoPlayer 的轨道变更回调更新。
 */
@kotlin.OptIn(InternalForInheritanceMediampApi::class)
internal class ExoPlayerMediaMetadata : MediaMetadata {

    /**
     * 字幕轨道组。
     *
     * 包含可用字幕轨道列表（[MutableTrackGroup.candidates]）和当前选中的轨道（[MutableTrackGroup.selected]）。
     * 在 [ExoPlayerMediampPlayer] 的轨道变更回调中更新。
     */
    override val subtitleTracks: MutableTrackGroup<SubtitleTrack> = MutableTrackGroup()

    /**
     * 音频轨道组。
     *
     * 包含可用音频轨道列表和当前选中的轨道。
     * 在 [ExoPlayerMediampPlayer] 的轨道变更回调中更新。
     */
    override val audioTracks: MutableTrackGroup<AudioTrack> = MutableTrackGroup()

    /**
     * 章节列表状态流。
     *
     * 目前固定为空列表，暂不支持章节解析。
     * 未来可扩展以支持 MKV 章节或其他元数据来源。
     */
    override val chapters: StateFlow<List<Chapter>> = MutableStateFlow(listOf())
}

/**
 * 下载速度 Feature 的 ExoPlayer 实现。
 *
 * 通过 [DefaultBandwidthMeter] 的带宽估算值提供实时下载速度。
 * 状态由 [ExoPlayerMediampPlayer] 的后台协程每 100ms 同步。
 */
internal class ExoPlayerDownloadSpeed : DownloadSpeed {
    /** 当前估算的下载速度。 */
    override val speedBps: MutableStateFlow<Long> = MutableStateFlow(-1L)
}

/** 向 Mediamp 暴露 ExoPlayer 缓冲进度。 */
@kotlin.OptIn(ExperimentalMediampApi::class, InternalForInheritanceMediampApi::class)
internal class ExoPlayerBuffering : Buffering {

    /**
     * 是否正在缓冲的状态流。
     *
     * 当 ExoPlayer 处于 [Media3Player.STATE_BUFFERING] 状态时为 `true`。
     */
    override val isBuffering: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * 缓冲进度百分比状态流。
     *
     * 取值范围 0-100，由后台协程每 100ms 从 ExoPlayer 同步。
     */
    override val bufferedPercentage = MutableStateFlow(0)
}

/**
 * 视频宽高比特性的 ExoPlayer 实现。
 *
 * 提供视频显示宽高比模式的获取和设置功能。
 * 支持的模式包括适应（FIT）、填充（FILL）、拉伸（STRETCH）等。
 */
@kotlin.OptIn(InternalForInheritanceMediampApi::class)
internal class ExoPlayerVideoAspectRatio : VideoAspectRatio {

    /**
     * 当前宽高比模式的状态流。
     *
     * 初始值为 [AspectRatioMode.FIT]（保持宽高比并完整显示视频）。
     */
    override val mode: MutableStateFlow<AspectRatioMode> = MutableStateFlow(AspectRatioMode.FIT)

    /**
     * 设置视频显示宽高比模式。
     *
     * @param mode 目标宽高比模式
     *
     * @see AspectRatioMode
     */
    override fun setMode(mode: AspectRatioMode) {
        this.mode.value = mode
    }
}
