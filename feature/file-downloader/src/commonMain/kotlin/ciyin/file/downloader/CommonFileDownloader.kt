package ciyin.file.downloader

import ciyin.io.SystemFileSystem
import ciyin.file.downloader.core.DownloadState
import ciyin.file.downloader.core.FileDownloader
import ciyin.file.downloader.exceptions.ChunkDownloadException
import ciyin.file.downloader.exceptions.DownloadContentRangeMismatchException
import ciyin.file.downloader.exceptions.DownloadFileExistsException
import ciyin.file.downloader.exceptions.DownloadHttpStatusException
import ciyin.file.downloader.exceptions.DownloadOverwriteFailedException
import ciyin.file.downloader.model.DownloadConfig
import ciyin.file.downloader.util.deleteChunkTempDir
import ciyin.file.downloader.util.deleteSaveFile
import ciyin.file.downloader.util.deleteTempFile
import ciyin.file.downloader.util.toChunkTempDir
import ciyin.file.downloader.util.toTempPath
import ciyin.platform.logger
import ciyin.platform.time.currentTimeMillis
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.retry
import io.ktor.client.plugins.timeout
import io.ktor.client.request.head
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.yield
import okio.BufferedSink
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * 下载路径锁注册中心。
 *
 * 用于在进程内对同一路径的写入流程做串行化，避免并发下载写同一个目标文件导致的数据竞争。
 */
private object DownloadPathLockRegistry {

    /**
     * 路径锁条目。
     *
     * @property mutex 对应路径的互斥锁。
     * @property refCount 当前引用计数。
     */
    private data class PathLockEntry(
        val mutex: Mutex,
        var refCount: Int,
    )

    /** 路径锁注册表总锁。 */
    private val registryMutex = Mutex()

    /** 路径到锁条目的映射。 */
    private val lockMap = mutableMapOf<String, PathLockEntry>()

    /**
     * 在指定路径锁内执行代码块。
     *
     * @param path 需要互斥保护的文件路径。
     * @param block 需要执行的挂起代码块。
     * @return 代码块执行结果。
     */
    suspend fun <T> withPathLock(path: String, block: suspend () -> T): T {
        // 从注册表获取或创建路径锁，并增加引用计数。
        val lock = registryMutex.withLock {
            val entry = lockMap.getOrPut(path) {
                PathLockEntry(mutex = Mutex(), refCount = 0)
            }
            entry.refCount += 1
            entry.mutex
        }

        return try {
            // 在路径锁内执行目标逻辑。
            lock.withLock { block() }
        } finally {
            // 释放引用计数并在计数归零时移除锁条目。
            registryMutex.withLock {
                val entry = lockMap[path] ?: return@withLock
                entry.refCount -= 1
                if (entry.refCount <= 0) {
                    lockMap.remove(path)
                }
            }
        }
    }
}

/**
 * `Content-Range` 响应头解析结果。
 *
 * @property start 起始偏移。
 * @property end 结束偏移。
 * @property total 总大小。
 */
private data class ContentRangeInfo(
    val start: Long?,
    val end: Long?,
    val total: Long?,
)

/**
 * 单次响应执行计划。
 *
 * @property mode 响应处理模式。
 * @property append 是否以追加模式写入。
 * @property totalBytes 总字节数。
 * @property resetDownloadedBytes 是否重置已下载基线。
 */
private data class ResponsePlan(
    val mode: ResponseMode,
    val append: Boolean,
    val totalBytes: Long,
    val resetDownloadedBytes: Boolean,
)

/**
 * 响应处理模式。
 */
private enum class ResponseMode {
    Write,
    AlreadyComplete,
}

/**
 * 请求执行结果。
 */
private enum class AttemptResult {
    Success,
    AlreadyComplete,
}

/**
 * 下载进度跟踪器。
 *
 * @property downloadedBytes 当前已下载字节数。
 * @property lastProgressUpdateTime 上次进度上报时间。
 * @property speedCalculationStartTime 当前测速窗口起始时间。
 * @property speedCalculationStartBytes 当前测速窗口起始字节。
 */
private data class ProgressTracker(
    var downloadedBytes: Long,
    var lastProgressUpdateTime: Long,
    var speedCalculationStartTime: Long,
    var speedCalculationStartBytes: Long,
) {

    /**
     * 同步已下载字节并重置测速窗口。
     *
     * @param bytes 新的已下载字节数。
     */
    fun syncDownloadedBytes(bytes: Long) {
        // 获取当前时间作为新测速窗口起点。
        val now = currentTimeMillis()
        // 同步已下载字节值。
        downloadedBytes = bytes
        // 重置测速窗口开始时间。
        speedCalculationStartTime = now
        // 重置测速窗口开始字节。
        speedCalculationStartBytes = bytes
    }
}

/**
 * 基于令牌桶算法的限速器。
 *
 * @property bytesPerSecond 每秒允许通过的字节数。
 */
private class TokenBucketRateLimiter(
    private val bytesPerSecond: Long,
) {

    /** 当前可用令牌数。 */
    private var availableTokens = bytesPerSecond.toDouble()

    /** 上次补充令牌时间。 */
    private var lastRefillTime = currentTimeMillis()

    /** 保护令牌桶状态的互斥锁。 */
    private val mutex = Mutex()

    /**
     * 申请本次读写所需令牌，不足时挂起等待。
     *
     * @param bytes 本次需要通过的字节数。
     */
    suspend fun acquire(bytes: Int) {
        // 不限速或无有效字节时直接返回。
        if (bytesPerSecond <= 0L || bytes <= 0) {
            return
        }

        // 计算本次需要的令牌数量。
        val requiredTokens = bytes.toDouble()
        while (true) {
            var acquired = false
            val delayMillis = mutex.withLock {
                // 先按时间补充令牌。
                refill(bucketCapacity = max(bytesPerSecond.toDouble(), requiredTokens))
                // 令牌足够时直接消费并返回。
                if (availableTokens >= requiredTokens) {
                    availableTokens -= requiredTokens
                    acquired = true
                    0L
                } else {
                    // 计算缺失令牌对应的等待时间。
                    val missing = requiredTokens - availableTokens
                    ceil(missing * 1000.0 / bytesPerSecond.toDouble())
                        .toLong()
                        .coerceAtLeast(1L)
                }
            }
            if (acquired) {
                return
            }
            // 挂起等待令牌补足。
            delay(delayMillis)
        }
    }

    /**
     * 根据时间流逝补充令牌。
     *
     * @param bucketCapacity 令牌桶容量。
     */
    private fun refill(bucketCapacity: Double) {
        // 获取当前时间并计算距离上次补充的时间差。
        val now = currentTimeMillis()
        val elapsed = now - lastRefillTime
        // 时间差无效时不处理。
        if (elapsed <= 0L) {
            return
        }

        // 按时间差计算可补充令牌。
        val refillTokens = elapsed * bytesPerSecond.toDouble() / 1000.0
        // 令牌上限不超过桶容量。
        availableTokens = min(bucketCapacity, availableTokens + refillTokens)
        // 更新上次补充时间。
        lastRefillTime = now
    }
}

/**
 * 通用文件下载器实现。
 *
 * 支持大文件流式写盘、临时文件断点续传、进度速度上报以及限速控制。
 * 支持分块并行下载，提升网络不稳定场景下的下载成功率。
 *
 * @param ktorEngineFactory Ktor 客户端引擎工厂。
 * @param scope 下载任务作用域。
 * @param fileSystem 下载文件使用的文件系统。
 * @param httpClient 测试或宿主注入的 Ktor 客户端，未提供时按引擎创建。
 */
internal class CommonFileDownloader(
    ktorEngineFactory: HttpClientEngineFactory<HttpClientEngineConfig>,
    private val scope: CoroutineScope,
    private val fileSystem: FileSystem = SystemFileSystem,
    httpClient: HttpClient? = null,
) : FileDownloader {

    /** 日志器。 */
    private val logger = logger("FileDownloader")

    /** 内部状态流。 */
    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)

    /** 对外只读状态流。 */
    override val state = _state.asStateFlow()

    /** 生命周期控制互斥锁。 */
    private val lifecycleMutex = Mutex()

    /** 当前下载任务 Job。 */
    private var downloadJob: Job? = null

    /** 当前下载配置。 */
    private var currentConfig: DownloadConfig? = null

    /** 暂停标记。 */
    private var isPaused = false

    /** 资源清理标记。 */
    private var isCleanedUp = false

    /** 活跃会话编号。 */
    private val activeSessionId = MutableStateFlow(0L)

    /** Ktor 客户端。 */
    private val client = httpClient ?: HttpClient(ktorEngineFactory) {
        // 安装超时插件并设置请求级超时兜底为无限。
        install(HttpTimeout) {
            requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
        }
        // 安装重试插件，具体策略在请求级动态配置。
        install(HttpRequestRetry)
        // 安装日志插件并禁用 BODY 级日志。
        install(Logging) {
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    // 转发 Ktor 日志到项目日志器。
                    this@CommonFileDownloader.logger.d { message }
                }
            }
            level = LogLevel.HEADERS
        }
    }

    /**
     * 派发下载状态事件。
     *
     * @param event 要派发的下载状态。
     */
    private fun onDownloadEvent(event: DownloadState) {
        logger.d { "派发下载状态：$event" }
        // 更新内部状态流。
        _state.update { event }
    }

    /**
     * 将下载器恢复为空闲状态。
     */
    override fun idle() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            // 在互斥区内摘除当前任务并重置运行标记。
            val jobToCancel = lifecycleMutex.withLock {
                val job = downloadJob
                downloadJob = null
                currentConfig = null
                isPaused = false
                invalidateSessionLocked()
                job
            }
            // 等待旧任务取消完成。
            jobToCancel?.cancelAndJoin()
            // 对外发布空闲状态。
            onDownloadEvent(DownloadState.Idle)
        }
    }

    /**
     * 使用当前配置重新开始下载。
     */
    override fun restart(): Job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
        logger.d { "开始重新下载任务" }
        // 读取当前配置，无配置则直接返回。
        val config = lifecycleMutex.withLock { currentConfig }
        if (config == null) {
            logger.w { "当前配置不存在，无法开始重新下载任务" }
            return@launch
        }

        // 在互斥区摘除旧任务并使旧会话失效。
        val jobToCancel = lifecycleMutex.withLock {
            val job = downloadJob
            downloadJob = null
            isPaused = false
            invalidateSessionLocked()
            job
        }
        // 先确保旧任务结束。
        jobToCancel?.cancelAndJoin()
        // 删除旧目标文件。
        config.deleteSaveFile(fileSystem)
        // 删除旧临时文件。
        config.deleteTempFile(fileSystem)
        // 删除旧分块临时目录（如果有）。
        config.deleteChunkTempDir(fileSystem)
        // 重新触发下载。
        download(config).join()
    }

    /**
     * 暂停当前下载任务。
     */
    override fun pause() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            // 在互斥区获取可取消任务，并更新暂停标记。
            val jobToCancel = lifecycleMutex.withLock {
                val job = downloadJob
                if (job == null || job.isActive.not()) {
                    return@withLock null
                }
                downloadJob = null
                isPaused = true
                invalidateSessionLocked()
                job
            }
            // 若存在任务则执行取消并发布暂停状态。
            if (jobToCancel != null) {
                jobToCancel.cancelAndJoin()
                onDownloadEvent(DownloadState.Paused)
                logger.d { "暂停下载" }
            }
        }
    }

    /**
     * 从暂停状态恢复下载。
     */
    override fun resume(): Job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
        // 在互斥区校验是否可恢复并提取配置。
        val configToResume = lifecycleMutex.withLock {
            if (isPaused.not()) {
                return@withLock null
            }
            val config = currentConfig ?: return@withLock null
            isPaused = false
            config
        }
        // 有可恢复配置时发布恢复状态并重新下载。
        if (configToResume != null) {
            onDownloadEvent(DownloadState.Resumed)
            download(configToResume)
            logger.d { "继续下载" }
        }
    }

    /**
     * 取消当前下载。
     */
    override fun cancel() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            // 在互斥区摘除任务并使当前会话失效。
            val (jobToCancel, configSnapshot) = lifecycleMutex.withLock {
                val job = downloadJob
                val config = currentConfig
                downloadJob = null
                isPaused = false
                invalidateSessionLocked()
                job to config
            }
            // 取消旧任务并等待退出。
            jobToCancel?.cancelAndJoin()
            // 删除临时文件，保留正式目标文件。
            configSnapshot?.deleteTempFile(fileSystem)
            // 删除分块临时目录（如果有）。
            configSnapshot?.deleteChunkTempDir(fileSystem)
            // 发布取消状态。
            onDownloadEvent(DownloadState.Cancelled)
            logger.d { "取消下载" }
        }
    }

    /**
     * 清理下载器资源。
     */
    override fun cleanup() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            // 在互斥区标记清理并摘除运行任务。
            val (jobToCancel, configSnapshot, shouldClose) = lifecycleMutex.withLock {
                if (isCleanedUp) {
                    return@withLock Triple(null, null, false)
                }
                val job = downloadJob
                val config = currentConfig
                isCleanedUp = true
                downloadJob = null
                currentConfig = null
                isPaused = false
                invalidateSessionLocked()
                Triple(job, config, true)
            }
            // 取消旧任务并等待完成。
            jobToCancel?.cancelAndJoin()
            // 删除残留临时文件。
            configSnapshot?.deleteTempFile(fileSystem)
            // 删除残留分块临时目录。
            configSnapshot?.deleteChunkTempDir(fileSystem)
            // 关闭客户端并停止控制作用域。
            if (shouldClose) {
                client.close()
                scope.cancel()
                logger.d { "清理资源" }
            }
        }
    }

    /**
     * 启动下载任务。
     *
     * @param config 下载配置。
     */
    override fun download(config: DownloadConfig): Job = scope.launch(
        start = CoroutineStart.UNDISPATCHED
    ) {
        // 先验证配置合法性。
        try {
            validateConfig(config)
        } catch (e: IllegalArgumentException) {
            onDownloadEvent(DownloadState.Error(e.message ?: "下载配置非法", e))
            return@launch
        }

        // 构造目标路径与临时路径。
        val savePath = config.savePath.toPath()
        val tempPath = config.savePath.toTempPath()

        // 确保目标目录存在。
        createSavePathParentDir(savePath)

        // 在互斥区校验生命周期并摘除旧任务。
        val runningJob = lifecycleMutex.withLock {
            if (isCleanedUp) {
                val exception = IllegalStateException("下载器已清理，不能继续下载")
                onDownloadEvent(DownloadState.Error(exception.message, exception))
                return@launch
            }
            currentConfig = config
            isPaused = false
            downloadJob.apply {
                downloadJob = null
                invalidateSessionLocked()
            }
        }

        // 等待旧任务退出。
        runningJob?.cancelAndJoin()

        // 预留会话号变量供任务闭包捕获。
        var sessionId = 0L

        // 创建新的会话任务。
        val sessionJob = scope.launch(start = CoroutineStart.LAZY) {
            runDownloadSession(
                sessionId = sessionId,
                config = config,
                tempPath = tempPath,
                savePath = savePath,
            )
        }

        // 在互斥区登记新会话和新任务。
        lifecycleMutex.withLock {
            sessionId = invalidateSessionLocked()
            downloadJob = sessionJob
        }

        // 发布开始状态。
        emitStateForSession(sessionId, DownloadState.Start)
        logger.d { "开始下载: ${config.url} 保存路径: ${config.savePath}" }

        // 启动并等待会话任务完成。
        sessionJob.start()
        sessionJob.join()
    }

    /**
     * 执行一次完整下载会话。
     *
     * @param sessionId 会话编号。
     * @param config 下载配置。
     * @param tempPath 临时文件路径。
     * @param savePath 目标文件路径。
     */
    private suspend fun runDownloadSession(
        sessionId: Long,
        config: DownloadConfig,
        tempPath: Path,
        savePath: Path,
    ) {
        // 记录当前任务引用，便于 finally 阶段比对清理。
        val currentJob = currentCoroutineContext()[Job]

        try {
            // 对目标路径加锁，避免同路径并发写入。
            DownloadPathLockRegistry.withPathLock(savePath.toString()) {
                // 覆盖关闭时若目标已存在则直接失败。
                if (config.overwriteExisting.not() && fileSystem.exists(savePath)) {
                    throw DownloadFileExistsException(savePath.toString())
                }

                // 仅启用分块下载时探测远程文件信息，避免单连接下载引入额外 HEAD 请求。
                val remoteInfo = if (config.enableChunkedDownload) {
                    fetchRemoteFileInfo(config.url, config.headers)
                } else {
                    null
                }
                if (remoteInfo != null) {
                    logger.d { "远程文件信息: size=${remoteInfo.totalSize}, supportsRange=${remoteInfo.supportsRange}" }
                }

                if (remoteInfo != null && ChunkCalculator.shouldUseChunkedDownload(remoteInfo, config)) {
                    // 使用分块下载模式。
                    runChunkedDownloadSession(sessionId, config, tempPath, savePath, remoteInfo)
                } else {
                    // 使用单连接下载模式。
                    runSingleConnectionDownload(sessionId, config, tempPath, savePath)
                }

                // 在互斥区修正暂停标记。
                lifecycleMutex.withLock {
                    if (isSessionActive(sessionId)) {
                        isPaused = false
                    }
                }

                // 发布完成状态。
                emitStateForSession(sessionId, DownloadState.Complete(savePath))
                logger.d { "下载完成: $savePath" }
            }
        } catch (_: CancellationException) {
            // 任务取消属于预期分支，仅记录日志。
            logger.d { "下载任务被取消，session=$sessionId" }
        } catch (e: Exception) {
            // 其他异常统一发布错误状态。
            logger.e(e) { "下载失败: ${e.message}" }
            onDownloadEvent(DownloadState.Error(e.message ?: "下载失败", e))
        } finally {
            // 会话结束时仅在仍指向当前任务时清空引用。
            lifecycleMutex.withLock {
                if (downloadJob == currentJob) {
                    downloadJob = null
                }
            }
        }
    }

    /**
     * 执行单次下载请求，并通过 Ktor 重试插件处理失败重试。
     *
     * @param sessionId 会话编号。
     * @param config 下载配置。
     * @param tempPath 临时文件路径。
     * @param progressTracker 进度跟踪器。
     */
    private suspend fun executeDownloadWithRetry(
        sessionId: Long,
        config: DownloadConfig,
        tempPath: Path,
        progressTracker: ProgressTracker,
    ) {
        // 初始化请求结果标记。
        var attemptResult = AttemptResult.Success
        // 计算首次请求的断点偏移。
        val initialResumeOffset = resolveResumeOffset(config, tempPath)
        // 将跟踪器同步到首次偏移基线。
        progressTracker.syncDownloadedBytes(initialResumeOffset)

        client.prepareGet(config.url) {
            // 设置请求头（业务头 + Range 头）。
            headers {
                config.headers.forEach { (key, value) ->
                    append(key, value)
                }
                if (initialResumeOffset > 0L) {
                    append(HttpHeaders.Range, "bytes=$initialResumeOffset-")
                }
            }

            // 设置本次请求连接与读写超时。
            timeout {
                connectTimeoutMillis = config.timeout
                socketTimeoutMillis = config.timeout
            }

            // 按配置设置 Ktor 重试策略。
            retry {
                // 配置最大重试次数。
                maxRetries = config.maxRetries

                // 配置重试延迟。
                if (config.retryDelayMs > 0L) {
                    delayMillis { config.retryDelayMs }
                }

                // 按响应状态码决定是否重试。
                retryIf { _, response ->
                    isRetryableStatusCode(response.status.value)
                }

                // 按异常类型决定是否重试。
                retryOnExceptionIf { _, cause ->
                    when (cause) {
                        is CancellationException -> false
                        is DownloadContentRangeMismatchException -> {
                            // 遇到偏移不匹配时先清临时文件后重试。
                            logger.w { "断点续传偏移异常，准备清理临时文件后重试: ${cause.message}" }
                            config.deleteTempFile(fileSystem)
                            true
                        }

                        is DownloadHttpStatusException -> {
                            isRetryableStatusCode(cause.statusCode)
                        }

                        is DownloadFileExistsException,
                        is DownloadOverwriteFailedException,
                        is IllegalArgumentException -> false

                        else -> true
                    }
                }

                // 每次重试前动态刷新 Range 头，确保续传偏移正确。
                modifyRequest { request ->
                    request.headers.remove(HttpHeaders.Range)
                    val latestResumeOffset = resolveResumeOffset(config, tempPath)
                    if (latestResumeOffset > 0L) {
                        request.headers.append(HttpHeaders.Range, "bytes=$latestResumeOffset-")
                    }
                }
            }
        }.execute { response ->
            // 从实际请求头反解析本次请求使用的续传偏移。
            val resumeOffsetFromRequest = parseResumeOffsetFromRequest(response)
            // 将进度跟踪器同步到本次请求偏移基线。
            progressTracker.syncDownloadedBytes(resumeOffsetFromRequest)

            // 基于响应头和状态码构建响应处理计划。
            val responsePlan = buildResponsePlan(
                config = config,
                response = response,
                resumeOffset = resumeOffsetFromRequest,
            )

            // 已完成场景直接结束本次执行。
            if (responsePlan.mode == ResponseMode.AlreadyComplete) {
                attemptResult = AttemptResult.AlreadyComplete
                return@execute
            }

            // 服务端忽略 Range 时重置已下载基线。
            if (responsePlan.resetDownloadedBytes) {
                progressTracker.syncDownloadedBytes(0L)
            }

            // 打开临时文件并按流式方式持续写入。
            val sink = if (responsePlan.append) {
                fileSystem.appendingSink(tempPath)
            } else {
                fileSystem.sink(tempPath)
            }
            sink.buffer()
                .use { sink ->
                    sink.writeToSink(
                        response = response,
                        config = config,
                        totalBytes = responsePlan.totalBytes,
                        progressTracker = progressTracker,
                        sessionId = sessionId,
                    )
                }
        }

        // 防御性检查：当前只允许两种合法结果。
        if (attemptResult != AttemptResult.Success && attemptResult != AttemptResult.AlreadyComplete) {
            throw IllegalStateException("下载状态异常: $attemptResult")
        }
    }

    /**
     * 将响应体按流式方式写入当前缓冲 Sink。
     *
     * @param response HTTP 响应。
     * @param config 下载配置。
     * @param totalBytes 总字节数。
     * @param progressTracker 进度跟踪器。
     * @param sessionId 会话编号。
     */
    private suspend fun BufferedSink.writeToSink(
        response: HttpResponse,
        config: DownloadConfig,
        totalBytes: Long,
        progressTracker: ProgressTracker,
        sessionId: Long,
    ) {
        // 获取响应体字节通道。
        val channel = response.bodyAsChannel()
        // 按配置创建读缓冲区。
        val buffer = ByteArray(config.bufferSize)
        // 创建限速器。
        val rateLimiter = TokenBucketRateLimiter(config.speedLimitBytesPerSecond)

        while (currentCoroutineContext().isActive) {
            // 从通道读取一批字节。
            val bytesRead = channel.readAvailable(buffer)
            when {
                // 小于 0 表示流结束。
                bytesRead < 0 -> break
                // 读到 0 字节时主动让出调度避免空转。
                bytesRead == 0 -> yield()
                else -> {
                    // 将本批数据写入目标 Sink。
                    write(buffer, 0, bytesRead)
                    // 更新已下载字节统计。
                    progressTracker.downloadedBytes += bytesRead
                    // 按限速器节奏控制吞吐。
                    rateLimiter.acquire(bytesRead)

                    // 若开启进度上报则按窗口上报下载状态。
                    if (config.enableDownloadProgress) {
                        emitProgressIfNeeded(
                            sessionId = sessionId,
                            config = config,
                            totalBytes = totalBytes,
                            progressTracker = progressTracker,
                            force = false,
                        )
                    }
                }
            }
        }

        // 刷新 Sink，确保缓冲写盘。
        flush()

        // 下载结束后强制发送一次最终进度。
        if (config.enableDownloadProgress) {
            emitProgressIfNeeded(
                sessionId = sessionId,
                config = config,
                totalBytes = totalBytes,
                progressTracker = progressTracker,
                force = true,
            )
        }
    }

    /**
     * 在满足频率条件时发送进度状态。
     *
     * @param sessionId 会话编号。
     * @param config 下载配置。
     * @param totalBytes 总字节数。
     * @param progressTracker 进度跟踪器。
     * @param force 是否忽略时间窗口强制上报。
     */
    private fun emitProgressIfNeeded(
        sessionId: Long,
        config: DownloadConfig,
        totalBytes: Long,
        progressTracker: ProgressTracker,
        force: Boolean,
    ) {
        // 获取当前时间并计算距离上次上报的间隔。
        val currentTime = currentTimeMillis()
        val elapsedSinceLastUpdate = currentTime - progressTracker.lastProgressUpdateTime
        // 非强制场景下若未达到上报间隔则直接返回。
        if (force.not() && elapsedSinceLastUpdate < config.progressUpdateInterval) {
            return
        }

        // 按总字节计算当前进度比例。
        val progress = if (totalBytes > 0L) {
            (progressTracker.downloadedBytes.toDouble() / totalBytes.toDouble())
                .coerceIn(0.0, 1.0)
                .toFloat()
        } else {
            0f
        }

        // 计算当前测速窗口时间差。
        val timeDiff = (currentTime - progressTracker.speedCalculationStartTime).coerceAtLeast(1L)
        // 计算当前测速窗口字节差。
        val bytesDiff =
            (progressTracker.downloadedBytes - progressTracker.speedCalculationStartBytes)
                .coerceAtLeast(0L)
        // 计算当前速度（字节/秒）。
        val speed = bytesDiff * 1000L / timeDiff

        // 发送下载中状态。
        emitStateForSession(
            sessionId,
            DownloadState.Downloading(
                progress = progress,
                downloaded = progressTracker.downloadedBytes,
                total = totalBytes,
                speed = speed,
            ),
        )

        // 记录本次上报时间。
        progressTracker.lastProgressUpdateTime = currentTime
        // 达到测速窗口阈值或强制上报时重置测速基线。
        if (timeDiff >= 1000L || force) {
            progressTracker.speedCalculationStartTime = currentTime
            progressTracker.speedCalculationStartBytes = progressTracker.downloadedBytes
        }
    }

    /**
     * 仅在会话仍活跃时派发状态。
     *
     * @param sessionId 会话编号。
     * @param state 状态对象。
     */
    private fun emitStateForSession(sessionId: Long, state: DownloadState) {
        // 仅当前会话仍活跃时更新状态。
        if (isSessionActive(sessionId)) {
            onDownloadEvent(state)
        }
    }

    /**
     * 判断会话是否活跃。
     *
     * @param sessionId 会话编号。
     * @return `true` 表示会话仍是当前活跃会话。
     */
    private fun isSessionActive(sessionId: Long): Boolean {
        // 对比活跃会话号。
        return activeSessionId.value == sessionId
    }

    /**
     * 在持锁上下文中使当前会话失效并生成新会话号。
     *
     * @return 新的会话号。
     */
    private fun invalidateSessionLocked(): Long {
        // 自增活跃会话号。
        activeSessionId.update { it + 1L }
        // 返回最新会话号。
        return activeSessionId.value
    }

    /**
     * 构建响应处理计划。
     *
     * @param config 下载配置。
     * @param response HTTP 响应。
     * @param resumeOffset 本次请求偏移。
     * @return 响应处理计划。
     */
    private fun buildResponsePlan(
        config: DownloadConfig,
        response: HttpResponse,
        resumeOffset: Long,
    ): ResponsePlan {
        // 读取状态码。
        val status = response.status
        // 解析 Content-Length。
        val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: 0L
        // 解析 Content-Range。
        val contentRange = parseContentRange(response.headers[HttpHeaders.ContentRange])

        // 续传场景的分支处理。
        if (resumeOffset > 0L) {
            return when (status) {
                HttpStatusCode.PartialContent -> {
                    // 读取服务端返回起始偏移。
                    val actualStart = contentRange?.start
                        ?: throw DownloadHttpStatusException(
                            statusCode = status.value,
                            url = config.url,
                            details = "断点续传响应缺少有效 Content-Range 起始偏移",
                        )

                    // 校验起始偏移必须与请求偏移一致。
                    if (actualStart != resumeOffset) {
                        throw DownloadContentRangeMismatchException(
                            expectedStart = resumeOffset,
                            actualStart = actualStart,
                        )
                    }

                    // 计算总大小并返回续传写入计划。
                    val totalBytes = contentRange.total ?: (resumeOffset + contentLength)
                    ResponsePlan(
                        mode = ResponseMode.Write,
                        append = true,
                        totalBytes = totalBytes,
                        resetDownloadedBytes = false,
                    )
                }

                HttpStatusCode.OK -> {
                    // 服务端忽略 Range，回退为全量覆盖写入。
                    ResponsePlan(
                        mode = ResponseMode.Write,
                        append = false,
                        totalBytes = contentLength,
                        resetDownloadedBytes = true,
                    )
                }

                HttpStatusCode.RequestedRangeNotSatisfiable -> {
                    // 解析服务端总大小。
                    val totalBytes = contentRange?.total
                    // 本地偏移恰好等于总大小时可视为已完成。
                    if (totalBytes != null && totalBytes == resumeOffset) {
                        ResponsePlan(
                            mode = ResponseMode.AlreadyComplete,
                            append = false,
                            totalBytes = totalBytes,
                            resetDownloadedBytes = false,
                        )
                    } else {
                        // 其他 416 视为异常。
                        throw DownloadHttpStatusException(
                            statusCode = status.value,
                            url = config.url,
                            details = "Range 不可满足，已下载=$resumeOffset，总大小=$totalBytes",
                        )
                    }
                }

                else -> {
                    // 续传请求的其他状态码直接失败。
                    throw DownloadHttpStatusException(
                        statusCode = status.value,
                        url = config.url,
                        details = "断点续传请求失败",
                    )
                }
            }
        }

        // 非续传场景下先校验状态码区间。
        if (status.value !in 200..299) {
            throw DownloadHttpStatusException(
                statusCode = status.value,
                url = config.url,
                details = "下载请求失败",
            )
        }

        // 非续传却返回 206 时，要求起始偏移必须为 0。
        if (status == HttpStatusCode.PartialContent) {
            val actualStart = contentRange?.start ?: 0L
            if (actualStart != 0L) {
                throw DownloadContentRangeMismatchException(
                    expectedStart = 0L,
                    actualStart = actualStart,
                )
            }

            // 返回覆盖写入计划。
            return ResponsePlan(
                mode = ResponseMode.Write,
                append = false,
                totalBytes = contentRange?.total ?: contentLength,
                resetDownloadedBytes = true,
            )
        }

        // 普通 200 成功场景返回覆盖写入计划。
        return ResponsePlan(
            mode = ResponseMode.Write,
            append = false,
            totalBytes = contentLength,
            resetDownloadedBytes = false,
        )
    }

    /**
     * 解析 `Content-Range` 响应头。
     *
     * @param contentRangeValue 响应头原始值。
     * @return 解析结果，无法解析返回 `null`。
     */
    private fun parseContentRange(contentRangeValue: String?): ContentRangeInfo? {
        // 空值直接返回。
        if (contentRangeValue.isNullOrBlank()) {
            return null
        }

        // 规范化原始字符串。
        val rawValue = contentRangeValue.trim()
        // 校验前缀必须为 bytes。
        if (rawValue.startsWith("bytes ").not()) {
            return null
        }

        // 去掉前缀并分离 range 与 total。
        val payload = rawValue.removePrefix("bytes ").trim()
        val parts = payload.split("/", limit = 2)
        if (parts.size != 2) {
            return null
        }

        // 提取范围部分和总大小部分。
        val rangePart = parts[0].trim()
        val totalPart = parts[1].trim()
        val total = totalPart.takeIf { it != "*" }?.toLongOrNull()

        // 处理 `bytes */total` 形式。
        if (rangePart == "*") {
            return ContentRangeInfo(start = null, end = null, total = total)
        }

        // 拆分 start-end。
        val bounds = rangePart.split("-", limit = 2)
        if (bounds.size != 2) {
            return null
        }

        // 解析 start/end 并返回。
        val start = bounds[0].toLongOrNull()
        val end = bounds[1].toLongOrNull()
        return ContentRangeInfo(start = start, end = end, total = total)
    }

    /**
     * 将临时文件移动为正式目标文件。
     *
     * @param config 下载配置。
     * @param tempPath 临时文件路径。
     * @param savePath 目标文件路径。
     */
    private fun moveTempToSavePath(config: DownloadConfig, tempPath: Path, savePath: Path) {
        // 临时文件不存在时直接失败。
        if (fileSystem.exists(tempPath).not()) {
            throw DownloadOverwriteFailedException(
                path = savePath.toString(),
                cause = IllegalStateException("临时文件不存在: $tempPath"),
            )
        }

        // 目标存在时按覆盖策略处理。
        if (fileSystem.exists(savePath)) {
            if (config.overwriteExisting.not()) {
                throw DownloadFileExistsException(savePath.toString())
            }
            fileSystem.delete(savePath)
        }

        // 执行原子移动。
        try {
            fileSystem.atomicMove(tempPath, savePath)
        } catch (e: Exception) {
            throw DownloadOverwriteFailedException(savePath.toString(), e)
        }
    }

    /**
     * 计算当前应使用的续传偏移。
     *
     * @param config 下载配置。
     * @param tempPath 临时文件路径。
     * @return 续传偏移字节数。
     */
    private fun resolveResumeOffset(config: DownloadConfig, tempPath: Path): Long {
        // 未开启断点续传时返回 0。
        if (config.enableResume.not()) {
            return 0L
        }

        // 临时文件不存在时返回 0。
        if (fileSystem.exists(tempPath).not()) {
            return 0L
        }

        // 返回临时文件当前大小作为偏移。
        return fileSystem.metadataOrNull(tempPath)?.size ?: 0L
    }

    /**
     * 从响应关联请求中解析 Range 偏移。
     *
     * @param response HTTP 响应。
     * @return 解析出的偏移值。
     */
    private fun parseResumeOffsetFromRequest(response: HttpResponse): Long {
        // 读取响应关联请求中的 Range 头。
        val rangeHeaderValue = response.call.request.headers[HttpHeaders.Range] ?: return 0L
        // 校验 Range 前缀格式。
        if (rangeHeaderValue.startsWith("bytes=").not()) {
            return 0L
        }
        // 解析 `bytes={start}-` 中的 start。
        return rangeHeaderValue
            .substringAfter("bytes=", missingDelimiterValue = "")
            .substringBefore("-", missingDelimiterValue = "")
            .toLongOrNull()
            ?: 0L
    }

    /**
     * 判断状态码是否可重试。
     *
     * @param statusCode 状态码。
     * @return `true` 表示可重试。
     */
    private fun isRetryableStatusCode(statusCode: Int): Boolean {
        // 仅对超时、限流和 5xx 场景放开重试。
        return statusCode == 408 || statusCode == 429 || statusCode in 500..599
    }

    /**
     * 校验下载配置合法性。
     *
     * @param config 下载配置。
     */
    private fun validateConfig(config: DownloadConfig) {
        // 校验下载地址。
        require(config.url.isNotBlank()) { "下载地址不能为空" }
        // 校验保存路径。
        require(config.savePath.isNotBlank()) { "保存路径不能为空" }
        // 校验超时参数。
        require(config.timeout > 0L) { "超时时间必须大于 0" }
        // 校验缓冲区大小。
        require(config.bufferSize > 0) { "缓冲区大小必须大于 0" }
        // 校验最大重试次数。
        require(config.maxRetries >= 0) { "最大重试次数不能小于 0" }
        // 校验重试延迟。
        require(config.retryDelayMs >= 0L) { "重试延迟不能小于 0" }
        // 校验限速值。
        require(config.speedLimitBytesPerSecond >= 0L) { "限速值不能小于 0" }
        // 校验进度上报间隔。
        require(config.progressUpdateInterval > 0L) { "进度更新间隔必须大于 0" }
        // 校验分块下载配置。
        if (config.enableChunkedDownload) {
            require(config.chunkSize > 0L) { "分块大小必须大于 0" }
            require(config.maxConcurrentChunks > 0) { "最大并发分块数必须大于 0" }
            require(config.chunkMaxRetries >= 0) { "分块最大重试次数不能小于 0" }
            require(config.chunkRetryDelayMs >= 0L) { "分块重试延迟不能小于 0" }
        }
    }

    /**
     * 确保保存路径父目录存在。
     *
     * @param savePath 目标保存路径。
     */
    private fun createSavePathParentDir(savePath: Path) {
        // 获取父目录并在不存在时创建。
        savePath.parent?.let {
            if (!fileSystem.exists(it)) {
                fileSystem.createDirectories(it)
            }
        }
    }

    /**
     * 获取远程文件信息。
     *
     * 通过 HEAD 请求获取文件大小和 Range 支持情况。
     * 如果 HEAD 请求失败，返回未知大小且不支持 Range 的信息，以便降级到单连接下载。
     *
     * @param url 下载地址。
     * @param headers 自定义请求头。
     * @return 远程文件信息。
     */
    private suspend fun fetchRemoteFileInfo(
        url: String,
        headers: Map<String, String>,
    ): RemoteFileInfo {
        return try {
            val response = client.head(url) {
                headers {
                    headers.forEach { (key, value) ->
                        append(key, value)
                    }
                }
                timeout {
                    connectTimeoutMillis = 10_000L
                    socketTimeoutMillis = 10_000L
                }
            }

            val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: -1L
            val acceptRanges = response.headers[HttpHeaders.AcceptRanges]
            val supportsRange = acceptRanges?.contains("bytes") == true ||
                    response.status == HttpStatusCode.PartialContent

            RemoteFileInfo(
                totalSize = contentLength,
                supportsRange = supportsRange,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // HEAD 请求失败时，返回未知信息，降级到单连接下载
            logger.w { "HEAD 请求失败，将降级到单连接下载: ${e.message}" }
            RemoteFileInfo(
                totalSize = -1L,
                supportsRange = false,
            )
        }
    }

    /**
     * 下载单个分块（带重试）。
     *
     * @param chunk 分块信息。
     * @param config 下载配置。
     * @param rateLimiter 下载限速器。
     * @param onProgress 进度回调，每读取一批字节或重试重置分块进度时调用。
     * @return `true` 表示下载成功。
     */
    private suspend fun downloadChunk(
        chunk: ChunkInfo,
        config: DownloadConfig,
        rateLimiter: TokenBucketRateLimiter,
        onProgress: suspend (chunk: ChunkInfo, downloadedBytes: Long) -> Unit,
    ): Boolean {
        var lastException: Exception? = null

        repeat(config.chunkMaxRetries + 1) { attempt ->
            try {
                if (attempt > 0) {
                    logger.w { "分块 ${chunk.index} 重试 (${attempt}/${config.chunkMaxRetries})" }
                    // 重试前将该分块的有效进度重置为 0，避免进度重复累加。
                    onProgress(chunk, 0L)
                    delay(config.chunkRetryDelayMs)
                }

                client.prepareGet(config.url) {
                    headers {
                        config.headers.forEach { (key, value) ->
                            append(key, value)
                        }
                        append(HttpHeaders.Range, "bytes=${chunk.start}-${chunk.end}")
                    }
                    timeout {
                        connectTimeoutMillis = config.timeout
                        socketTimeoutMillis = config.timeout
                    }
                }.execute { response ->
                    // 校验响应状态
                    if (response.status != HttpStatusCode.PartialContent && response.status != HttpStatusCode.OK) {
                        throw DownloadHttpStatusException(
                            statusCode = response.status.value,
                            url = config.url,
                            details = "分块 ${chunk.index} 下载失败",
                        )
                    }

                    // 流式写入分块临时文件
                    val channel = response.bodyAsChannel()
                    val buffer = ByteArray(config.bufferSize)
                    var downloadedBytes = 0L

                    fileSystem.sink(chunk.tempPath).buffer().use { sink ->
                        while (currentCoroutineContext().isActive) {
                            val bytesRead = channel.readAvailable(buffer)
                            when {
                                bytesRead < 0 -> break
                                bytesRead == 0 -> yield()
                                else -> {
                                    sink.write(buffer, 0, bytesRead)
                                    // 按限速器节奏控制吞吐
                                    rateLimiter.acquire(bytesRead)
                                    downloadedBytes += bytesRead
                                    onProgress(chunk, downloadedBytes)
                                }
                            }
                        }
                        sink.flush()
                    }
                }

                // 下载成功
                logger.d { "分块 ${chunk.index} 下载完成，大小=${chunk.downloadedBytes}" }
                return true

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
                logger.w { "分块 ${chunk.index} 下载失败: ${e.message}" }
            }
        }

        logger.e { "分块 ${chunk.index} 下载失败，已重试 ${config.chunkMaxRetries} 次: ${lastException?.message}" }
        return false
    }

    /**
     * 并发下载所有分块。
     *
     * @param chunks 分块信息列表。
     * @param config 下载配置。
     * @param progressTracker 进度跟踪器。
     * @param rateLimiter 下载限速器。
     * @param sessionId 会话编号。
     * @param totalSize 文件总大小。
     * @throws ChunkDownloadException 当部分分块下载失败时抛出。
     */
    private suspend fun downloadChunksConcurrently(
        chunks: List<ChunkInfo>,
        config: DownloadConfig,
        progressTracker: ProgressTracker,
        rateLimiter: TokenBucketRateLimiter,
        sessionId: Long,
        totalSize: Long,
    ) {
        val semaphore = Semaphore(config.maxConcurrentChunks)
        val progressMutex = Mutex()

        coroutineScope {
            val jobs = chunks.map { chunk ->
                async {
                    semaphore.withPermit {
                        downloadChunk(
                            chunk = chunk,
                            config = config,
                            rateLimiter = rateLimiter,
                        ) { updatedChunk, downloadedBytes ->
                            progressMutex.withLock {
                                updatedChunk.downloadedBytes = downloadedBytes
                                progressTracker.downloadedBytes = chunks.sumOf { it.downloadedBytes }
                                if (config.enableDownloadProgress) {
                                    emitProgressIfNeeded(
                                        sessionId = sessionId,
                                        config = config,
                                        totalBytes = totalSize,
                                        progressTracker = progressTracker,
                                        force = false,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 等待所有分块完成
            val results = jobs.awaitAll()

            // 检查是否有失败的分块。
            ChunkCalculator.validateChunkResults(results)
        }

        // 强制发送一次最终进度
        if (config.enableDownloadProgress) {
            emitProgressIfNeeded(
                sessionId = sessionId,
                config = config,
                totalBytes = totalSize,
                progressTracker = progressTracker,
                force = true,
            )
        }
    }

    /**
     * 合并所有分块到临时文件。
     *
     * 合并前会校验每个分块文件的完整性（文件大小是否与预期一致）。
     *
     * @param chunks 分块信息列表（按索引排序）。
     * @param tempPath 目标临时文件路径。
     * @throws IllegalStateException 当分块文件缺失或大小不匹配时抛出。
     */
    private suspend fun mergeChunks(chunks: List<ChunkInfo>, tempPath: Path) {
        val sortedChunks = chunks.sortedBy { it.index }

        // 校验分块完整性
        sortedChunks.forEach { chunk ->
            if (!fileSystem.exists(chunk.tempPath)) {
                throw IllegalStateException("分块 ${chunk.index} 文件缺失: ${chunk.tempPath}")
            }
            val actualSize = fileSystem.metadataOrNull(chunk.tempPath)?.size ?: 0L
            if (actualSize != chunk.size) {
                throw IllegalStateException(
                    "分块 ${chunk.index} 大小不匹配: 预期=${chunk.size}, 实际=$actualSize"
                )
            }
        }

        // 合并分块
        fileSystem.sink(tempPath).buffer().use { outputSink ->
            val buffer = ByteArray(8192)
            sortedChunks.forEach { chunk ->
                fileSystem.source(chunk.tempPath).buffer().use { inputSource ->
                    while (true) {
                        val bytesRead = inputSource.read(buffer)
                        if (bytesRead == -1) break
                        outputSink.write(buffer, 0, bytesRead)
                    }
                }
            }
            outputSink.flush()
        }
    }

    /**
     * 执行分块下载会话。
     *
     * @param sessionId 会话编号。
     * @param config 下载配置。
     * @param tempPath 临时文件路径。
     * @param savePath 目标文件路径。
     * @param remoteInfo 远程文件信息。
     */
    private suspend fun runChunkedDownloadSession(
        sessionId: Long,
        config: DownloadConfig,
        tempPath: Path,
        savePath: Path,
        remoteInfo: RemoteFileInfo,
    ) {
        logger.d { "使用分块下载模式，文件大小=${remoteInfo.totalSize}，分块大小=${config.chunkSize}" }

        // 创建分块临时目录
        val chunkDir = config.savePath.toChunkTempDir()
        if (!fileSystem.exists(chunkDir)) {
            fileSystem.createDirectories(chunkDir)
        }

        // 计算分块
        val chunks = ChunkCalculator.calculateChunks(remoteInfo.totalSize, config.chunkSize, chunkDir)
        // 分块下载共享同一个限速器，确保限制的是整体下载速度。
        val rateLimiter = TokenBucketRateLimiter(config.speedLimitBytesPerSecond)

        // 初始化进度跟踪器
        val progressTracker = ProgressTracker(
            downloadedBytes = 0L,
            lastProgressUpdateTime = 0L,
            speedCalculationStartTime = currentTimeMillis(),
            speedCalculationStartBytes = 0L,
        )

        try {
            // 并发下载所有分块
            downloadChunksConcurrently(
                chunks = chunks,
                config = config,
                progressTracker = progressTracker,
                rateLimiter = rateLimiter,
                sessionId = sessionId,
                totalSize = remoteInfo.totalSize,
            )

            // 合并分块到临时文件
            mergeChunks(chunks, tempPath)

            // 清理分块临时目录
            config.deleteChunkTempDir(fileSystem)

            // 移动到目标路径
            moveTempToSavePath(config, tempPath, savePath)

            logger.d { "分块下载完成: $savePath" }

        } catch (e: Exception) {
            // 清理分块临时目录
            config.deleteChunkTempDir(fileSystem)
            throw e
        }
    }

    /**
     * 执行单连接下载会话（原有逻辑）。
     *
     * @param sessionId 会话编号。
     * @param config 下载配置。
     * @param tempPath 临时文件路径。
     * @param savePath 目标文件路径。
     */
    private suspend fun runSingleConnectionDownload(
        sessionId: Long,
        config: DownloadConfig,
        tempPath: Path,
        savePath: Path,
    ) {
        logger.d { "使用单连接下载模式" }

        // 初始化本会话的进度跟踪器。
        val progressTracker = ProgressTracker(
            downloadedBytes = 0L,
            lastProgressUpdateTime = 0L,
            speedCalculationStartTime = currentTimeMillis(),
            speedCalculationStartBytes = 0L,
        )

        // 执行带重试的下载流程。
        executeDownloadWithRetry(
            sessionId = sessionId,
            config = config,
            tempPath = tempPath,
            progressTracker = progressTracker,
        )

        // 下载成功后将临时文件原子移动到目标路径。
        moveTempToSavePath(config, tempPath, savePath)
    }
}
