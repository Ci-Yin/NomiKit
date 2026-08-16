@file:androidx.annotation.OptIn(UnstableApi::class)

package ciyin.video.player.player

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import kotlinx.coroutines.runBlocking
import org.openani.mediamp.io.SeekableInput
import org.openani.mediamp.source.SeekableInputMediaData
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTimedValue

/**
 * 是否启用读取操作的日志输出。
 *
 * 开启后会在每次 [SeekableInputDataSource.read] 调用时打印日志，
 * 并监测慢读取操作（超过 100ms 的读取）。
 *
 * @see SeekableInputDataSource.read
 */
private const val ENABLE_READ_LOG = false

/**
 * 是否启用跟踪级别的日志输出。
 *
 * 开启后会在 [SeekableInputDataSource.open]、[SeekableInputDataSource.close] 等关键操作时打印详细日志，
 * 用于调试数据源的生命周期和 seek 操作。
 *
 * @see SeekableInputDataSource.open
 * @see SeekableInputDataSource.close
 */
private const val ENABLE_TRACE_LOG = false

/**
 * 将 Mediamp 的 [SeekableInputMediaData] 适配为 ExoPlayer [androidx.media3.datasource.DataSource] 的桥接类。
 *
 * 此类实现了 ExoPlayer 数据源接口，使得 ExoPlayer 能够从自定义的可寻址输入流（如种子文件、网络流等）中读取媒体数据。
 * 继承自 [BaseDataSource]，并将 `isNetwork` 参数设为 `true`，表示数据来源于网络或类似的远程源。
 *
 * ## 生命周期说明
 *
 * - **open**: ExoPlayer 在需要读取数据前调用，会执行 seek 操作定位到指定位置
 * - **read**: ExoPlayer 反复调用以读取媒体数据块
 * - **close**: ExoPlayer 在数据读取完成或切换媒体时调用
 *
 * ## 重要注意事项
 *
 * - 此类**不会**关闭传入的 [mediaData]，调用方需自行管理资源释放
 * - [read] 方法会被非常频繁调用，其性能直接影响视频首帧延迟和播放流畅度
 * - 支持重复 open 相同 URI，会跳过重复初始化以提升性能
 *
 * ## 使用示例
 *
 * ```kotlin
 * val mediaData: SeekableInputMediaData = ...
 * val seekableInput: SeekableInput = mediaData.createInput()
 * val dataSource = SeekableInputDataSource(mediaData, seekableInput)
 *
 * // 用于创建 ProgressiveMediaSource
 * val factory = ProgressiveMediaSource.Factory { dataSource }
 * ```
 *
 * @param mediaData 媒体数据源，提供文件长度等元信息
 * @param file 可寻址的输入流，用于实际的数据读取和 seek 操作
 *
 * @see SeekableInputMediaData
 * @see SeekableInput
 * @see BaseDataSource
 */
internal class SeekableInputDataSource(
    /** 原始可寻址媒体数据。 */
    private val mediaData: SeekableInputMediaData,
    /** 当前读取的可寻址输入。 */
    private val file: SeekableInput,
) : BaseDataSource(true) {

    /**
     * 当前打开的媒体资源 URI。
     *
     * 在 [open] 时设置，在 [close] 时清空。
     * 用于 [getUri] 返回值以及判断是否需要重新初始化。
     */
    private var uri: Uri? = null

    /**
     * 标记数据源是否已打开。
     *
     * 用于跟踪传输状态，确保 [transferEnded] 只在数据源确实打开过的情况下调用。
     */
    private var opened = false

    /**
     * 从媒体数据源读取数据到指定缓冲区。
     *
     * 此方法是 ExoPlayer 读取媒体数据的核心入口，会被非常频繁地调用（可能每个字节调用一次）。
     * 读取性能直接影响视频首帧延迟和整体播放体验。
     *
     * ## 性能考虑
     *
     * - 避免在此方法中进行耗时操作
     * - 当 [ENABLE_READ_LOG] 开启时会监测慢读取（超过 100ms）
     * - 返回值为 0 时 ExoPlayer 可能会重试，返回 [C.RESULT_END_OF_INPUT] 表示流结束
     *
     * @param buffer 目标缓冲区，读取的数据将写入此数组
     * @param offset 缓冲区中开始写入的偏移位置
     * @param length 期望读取的最大字节数
     * @return 实际读取的字节数；如果到达流末尾返回 [C.RESULT_END_OF_INPUT]
     *
     * @see SeekableInput.read
     */
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        // 性能提示: 这个函数会被非常频繁调用 (一个 byte 一次), 速度会直接影响视频首帧延迟

        if (length == 0) return 0

        if (ENABLE_READ_LOG) { // const val, optimized out
            log { "VideoDataDataSource read: offset=$offset, length=$length" }
        }

        val bytesRead = if (ENABLE_READ_LOG) {
            val (value, time) = measureTimedValue {
                file.read(buffer, offset, length)
            }
            if (time > 100.milliseconds) {
                log { "VideoDataDataSource slow read: read $offset for length $length took $time" }
            }
            value
        } else {
            file.read(buffer, offset, length)
        }
        if (bytesRead == -1) {
            return C.RESULT_END_OF_INPUT
        }
        bytesTransferred(bytesRead)
        return bytesRead
    }

    /**
     * 打开数据源并准备读取指定位置的数据。
     *
     * ExoPlayer 在开始读取媒体数据前调用此方法。方法会根据 [dataSpec] 中指定的位置
     * 执行 seek 操作，将读取指针定位到正确位置。
     *
     * ## 行为说明
     *
     * 1. 如果数据源已打开且 URI 相同，跳过重复初始化
     * 2. 调用 [transferInitializing] 通知传输监听器初始化开始
     * 3. 如果请求位置不为 0，执行 seek 操作
     * 4. 调用 [transferStarted] 通知传输监听器传输开始
     *
     * ## 注意事项
     *
     * - seek 操作使用 [runBlocking]，可能阻塞当前线程
     * - 如果请求位置超出文件长度，不会执行 seek（避免异常）
     *
     * @param dataSpec 数据规格，包含 URI、起始位置、长度等信息
     * @return 从当前位置到流末尾的剩余字节数
     * @throws IOException 如果打开操作失败
     *
     * @see DataSpec
     * @see SeekableInput.seekTo
     */
    @Throws(IOException::class)
    override fun open(dataSpec: DataSpec): Long {
        if (ENABLE_TRACE_LOG) log { "Opening dataSpec, offset=${dataSpec.position}, length=${dataSpec.length}, videoData=$mediaData" }

        val uri = dataSpec.uri
        if (opened && dataSpec.uri == this.uri) {
            if (ENABLE_TRACE_LOG) log { "Double open, will not start download." }
        } else {
            this.uri = uri
            transferInitializing(dataSpec)
            opened = true
        }

        val torrentLength = mediaData.fileLength() ?: 0

        if (ENABLE_TRACE_LOG) log { "torrentLength = $torrentLength" }

        if (dataSpec.position >= torrentLength) {
            if (ENABLE_TRACE_LOG) log { "dataSpec.position ${dataSpec.position} > torrentLength $torrentLength" }
        } else {
            if (dataSpec.position != -1L && dataSpec.position != 0L) {
                if (ENABLE_TRACE_LOG) log { "Seeking to ${dataSpec.position}" }
                runBlocking { file.seekTo(dataSpec.position) }
            }

            if (ENABLE_TRACE_LOG) log { "Open done, bytesRemaining = ${file.bytesRemaining}" }
        }

        transferStarted(dataSpec)
        return file.bytesRemaining
    }

    /**
     * 获取当前打开的媒体资源 URI。
     *
     * @return 当前 URI，如果数据源未打开或已关闭则返回 `null`
     */
    override fun getUri(): Uri? = uri

    /**
     * 关闭数据源并释放相关资源。
     *
     * 此方法**不会**关闭传入的 [mediaData] 和 [file]，
     * 调用方需要在适当的时机自行释放这些资源。
     *
     * 仅在数据源确实打开过（[opened] 为 `true`）的情况下调用 [transferEnded]。
     */
    override fun close() {
        if (ENABLE_TRACE_LOG) log { "Closing VideoDataDataSource" }
        uri = null
        if (opened) {
            transferEnded()
        }
    }

    /**
     * 内部日志输出辅助函数。
     *
     * 使用 inline 和 lambda 延迟求值，确保在日志禁用时不产生字符串拼接开销。
     *
     * @param message 日志消息生成函数
     */
    private inline fun log(message: () -> String) {
        if (ENABLE_TRACE_LOG) println(message())
    }
}
