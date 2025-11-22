package ciyin.io

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.yield
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import okio.HashingSink
import okio.Sink
import okio.Source
import okio.buffer


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/1 14:00
 */

/**
 * 读取所有数据到 HashingSink
 */
fun Source.readAll(hashingSink: HashingSink) {
    val bufferedSource = buffer()
    val bufferedSink = hashingSink.buffer()
    // ✅ 正确的读写方式：循环读取数据流
    while (true) {
        val read = bufferedSource.read(bufferedSink.buffer, 8 * 1024)
        if (read == -1L) break
        bufferedSink.emit()
    }
    bufferedSink.close()
}

/**
 * Copies this source to the given sink, returning the number of bytes copied
 *
 * @param sink 目标 Sink
 * @param bufferSize 每次读取的缓冲区大小
 * @param progressInterval 进度回调间隔（字节数），0 表示每次都回调
 * @param onProgress 进度回调，参数为已复制字节数，返回 false 可取消复制
 * @return 实际复制的字节数
 * @throws okio.IOException 如果读写过程中发生错误
 *
 * **注意** 调用者负责关闭 source 和 sink
 */
fun Source.copyTo(
    sink: Sink,
    bufferSize: Long = DEFAULT_BUFFER_SIZE.toLong(),
    progressInterval: Long = 0,
    onProgress: (Long) -> Boolean = { true }
): Long {
    require(bufferSize > 0) { "Buffer size must be positive: $bufferSize" }
    require(progressInterval >= 0) { "Progress interval must be non-negative: $progressInterval" }

    val buffer = Buffer()
    var bytesCopied = 0L
    var lastReportedBytes = 0L

    while (true) {
        val bytesRead = read(buffer, bufferSize)
        if (bytesRead == -1L) break

        sink.write(buffer, bytesRead)
        bytesCopied += bytesRead

        // 根据间隔决定是否回调
        val shouldReport = when {
            progressInterval == 0L -> true
            bytesCopied - lastReportedBytes >= progressInterval -> true
            else -> false
        }

        if (shouldReport) {
            if (!onProgress(bytesCopied)) {
                break // 取消复制
            }
            lastReportedBytes = bytesCopied
        }
    }

    // 确保最终进度被报告
    if (bytesCopied > lastReportedBytes && progressInterval > 0) {
        onProgress(bytesCopied)
    }

    return bytesCopied
}

/**
 * BufferedSource 版本 - 更高效
 */
fun BufferedSource.copyTo(
    sink: BufferedSink,
    bufferSize: Long = DEFAULT_BUFFER_SIZE.toLong(),
    progressInterval: Long = 0,
    onProgress: (Long) -> Boolean = { true }
): Long {
    require(bufferSize > 0) { "Buffer size must be positive: $bufferSize" }
    require(progressInterval >= 0) { "Progress interval must be non-negative: $progressInterval" }

    var bytesCopied = 0L
    var lastReportedBytes = 0L

    while (true) {
        val bytesRead = read(sink.buffer, bufferSize)
        if (bytesRead == -1L) break

        sink.emit()
        bytesCopied += bytesRead

        // 根据间隔决定是否回调
        val shouldReport = when {
            progressInterval == 0L -> true
            bytesCopied - lastReportedBytes >= progressInterval -> true
            else -> false
        }

        if (shouldReport) {
            if (!onProgress(bytesCopied)) {
                break // 取消复制
            }
            lastReportedBytes = bytesCopied
        }
    }

    // 确保最终进度被报告
    if (bytesCopied > lastReportedBytes && progressInterval > 0) {
        onProgress(bytesCopied)
    }

    return bytesCopied
}


/**
 * 协程版本 - 支持协程取消
 */
suspend fun Source.copyToSuspend(
    sink: Sink,
    bufferSize: Long = DEFAULT_BUFFER_SIZE.toLong(),
    progressInterval: Long = 1024 * 1024,
    onProgress: suspend (Long) -> Unit = {}
): Long {
    require(bufferSize > 0) { "Buffer size must be positive: $bufferSize" }
    require(progressInterval >= 0) { "Progress interval must be non-negative: $progressInterval" }

    val buffer = Buffer()
    var bytesCopied = 0L
    var lastReportedBytes = 0L

    while (true) {
        // 检查协程是否被取消
        currentCoroutineContext().ensureActive()

        val bytesRead = read(buffer, bufferSize)
        if (bytesRead == -1L) break

        sink.write(buffer, bytesRead)
        bytesCopied += bytesRead

        // 根据间隔决定是否回调
        val shouldReport = when {
            progressInterval == 0L -> true
            bytesCopied - lastReportedBytes >= progressInterval -> true
            else -> false
        }

        if (shouldReport) {
            onProgress(bytesCopied)
            lastReportedBytes = bytesCopied
            yield() // 让出执行权，允许其他协程运行
        }
    }

    // 确保最终进度被报告
    if (bytesCopied > lastReportedBytes && progressInterval > 0) {
        onProgress(bytesCopied)
    }

    return bytesCopied
}