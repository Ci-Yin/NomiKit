package ciyin.file.downloader.core

import ciyin.platform.logger
import ciyin.file.downloader.fileDownloader
import ciyin.file.downloader.model.DownloadConfig
import ciyin.file.downloader.model.QueueTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

/**
 * 下载管理器，用于管理多个下载任务。
 *
 * @param maxConcurrentDownloads 最大并发下载数。
 * @param scope 用于启动下载任务的协程作用域。
 * @param downloaderFactory 按任务创建下载器的内部工厂。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DownloadManager internal constructor(
    private val maxConcurrentDownloads: Int,
    private val scope: CoroutineScope,
    private val downloaderFactory: (taskId: Int, scope: CoroutineScope) -> FileDownloader = { _, factoryScope ->
        fileDownloader(factoryScope)
    }
) {

    /** 日志记录器 */
    private val logger = logger("DownloadManager")

    /**
     * 全局下载并发闸门。
     *
     * 该闸门必须覆盖所有触发下载的入口（队列与手动调用），避免并发叠加超过上限。
     */
    private val downloadSemaphore: Semaphore

    /**
     * 用于跟踪“受控下载协程”的运行情况，避免同一任务重复占用并发名额。
     */
    private val managedJobsMutex = Mutex()

    /**
     * 记录每个任务当前正在运行的“受控下载协程”。
     */
    private val managedJobs = mutableMapOf<Int, Job>()

    /** 存储任务ID与对应下载器实例的映射 */
    private val downloaderMap = mutableMapOf<Int, FileDownloader>()

    /**
     * 每个任务的状态订阅 Job。
     */
    private val stateObserverJobs = mutableMapOf<Int, Job>()

    /** 下载任务队列 */
    private val downloadQueue = mutableListOf<QueueTask>()

    /** 当前下载任务事件的 SharedFlow */
    private val _taskEvents = MutableSharedFlow<DownloadTaskState>(
        replay = 0,
        extraBufferCapacity = 64, // 设置缓冲区
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** 当前下载任务事件的 SharedFlow */
    val taskEvents = _taskEvents.asSharedFlow()

    /** 内部可变的下载队列状态 */
    private val _queueState = MutableStateFlow<List<QueueTask>>(emptyList())

    /** 可供外部观察的下载队列状态 */
    val queueState = _queueState.asStateFlow()

    init {
        require(maxConcurrentDownloads >= 1) { "maxConcurrentDownloads 必须 >= 1" }
        downloadSemaphore = Semaphore(maxConcurrentDownloads)
    }

    /**
     * 发送下载任务事件。
     *
     * @param state 任务的状态。
     */
    private fun onTaskEvent(state: DownloadTaskState) {
        logger.i { "DownloadTaskEvent: $state" }
        scope.launch { _taskEvents.emit(state) }
    }

    /**
     * 创建或获取一个下载任务实例。
     * 如果已存在具有相同 `taskId` 的实例，则直接返回；否则，创建一个新的实例并存储。
     *
     * @param taskId 任务的唯一标识符。
     * @return 对应的 `FileDownloader` 实例。
     */
    private fun create(taskId: Int): FileDownloader {
        // 如果 downloaderMap 中已存在 taskId，则直接返回；否则创建一个新的并存入 map
        return downloaderMap.getOrPut(taskId) {
            logger.i { "创建新的 FileDownloader 实例: $taskId" }
            downloaderFactory(taskId, scope).also {
                onTaskEvent(DownloadTaskState.Create(taskId))
            }
        }
    }

    /**
     * 将一个下载任务添加到队列中。
     *
     * @param config 下载任务的配置。
     * @param priority 任务的优先级，数值越大优先级越高。
     */
    fun addToQueue(config: DownloadConfig, priority: Int = 0) {
        logger.i { "添加任务到队列: ${config.id}, 优先级: $priority" }
        // 添加到队列
        val queueTask = QueueTask(
            taskId = config.id,
            config = config,
            priority = priority
        )
        downloadQueue += queueTask
        // 根据优先级对队列进行降序排序
        downloadQueue.sortByDescending { it.priority }
        // 更新队列状态的 StateFlow
        _queueState.update { downloadQueue.toList() }
        onTaskEvent(DownloadTaskState.AddToQueue(queueTask))
    }


    /**
     * 处理下载队列中的任务。
     * 会按顺序依次执行队列中的下载任务。
     */
    fun startQueue() {
        downloadQueue.asFlow().onStart {
            logger.i { "开始处理下载队列，最大并发数: $maxConcurrentDownloads" }
        }.onEach { item ->
            logger.i { "准备处理队列任务: ${item.taskId}" }
            // 更新队列状态
            _queueState.update { downloadQueue.toList() }
        }.flatMapMerge(concurrency = maxConcurrentDownloads) { item ->
            flow {
                logger.i { "开始下载任务: ${item.taskId}" }
                // 创建或获取下载器实例
                // 执行下载
                ensureManagedDownload(
                    queueTask = item,
                    action = DownloadAction.Start,
                    policy = ActiveTaskPolicy.JoinExisting
                )
                emit(item)
            }
        }.catch { e ->
            logger.e(e) { "下载队列处理异常: ${e.message}" }
        }.onEach { item ->
            logger.i { "任务完成: ${item.taskId}" }
        }.onCompletion {
            logger.i { "下载队列处理完成。" }
            onTaskEvent(DownloadTaskState.AllTasksCompleted)
        }.launchIn(scope)
    }

    /**
     * 从队列中移除一个任务。
     *
     * @param taskId 要移除的任务的唯一标识符。
     */
    fun removeFromQueue(taskId: Int) {
        val queueTask = getTask(taskId) ?: return
        // 取消任务
        cancel(taskId)
        // 移除所有匹配 taskId 的项
        downloadQueue.remove(queueTask)
        // 更新队列状态
        _queueState.update { downloadQueue.toList() }
        onTaskEvent(DownloadTaskState.RemoveQueue(queueTask))
        logger.i { "从队列移除任务: $taskId" }
    }

    /**
     * 清空整个下载队列。
     */
    fun clearQueue() {
        logger.i { "清空下载队列" }
        cancelAll()
        downloadQueue.clear()
        _queueState.update { emptyList() }
        onTaskEvent(DownloadTaskState.ClearQueue)
    }

    /**
     * 启动一个下载任务。
     *
     * @param taskId 任务的唯一标识符。
     */
    fun start(taskId: Int) {
        val queueTask = getTask(taskId) ?: return
        launchManagedDownload(queueTask, DownloadAction.Start, ActiveTaskPolicy.CancelAndRestart)
        logger.i { "启动下载任务: $taskId" }
    }

    /**
     * 重新启动一个下载任务。
     *
     * @param taskId 任务的唯一标识符。
     */
    fun restart(taskId: Int) {
        val queueTask = getTask(taskId) ?: return
        launchManagedDownload(queueTask, DownloadAction.Restart, ActiveTaskPolicy.CancelAndRestart)
        logger.i { "重新启动下载任务: $taskId" }
    }

    /**
     * 暂停一个正在进行的下载任务。
     *
     * @param taskId 任务的唯一标识符。
     */
    fun pause(taskId: Int) {
        logger.i { "暂停下载任务: $taskId" }
        val queueTask = getTask(taskId) ?: return
        val downloader = getDownloader(taskId)
        downloader.pause()
        onTaskEvent(DownloadTaskState.Pause(queueTask))
        logger.i { "已暂停下载任务: $taskId" }
    }


    /**
     * 恢复一个已暂停的下载任务。
     *
     * @param taskId 任务的唯一标识符。
     */
    fun resume(taskId: Int) {
        logger.i { "恢复下载任务: $taskId" }
        val queueTask = getTask(taskId) ?: return
        onTaskEvent(DownloadTaskState.Resume(queueTask))
        launchManagedDownload(queueTask, DownloadAction.Resume, ActiveTaskPolicy.NoOp)
        logger.i { "已恢复下载任务: $taskId" }
    }

    /**
     * 取消一个下载任务。
     * 这会停止下载并从管理器中移除该任务。
     *
     * @param taskId 任务的唯一标识符。
     */
    fun cancel(taskId: Int) {
        logger.i { "取消下载任务: $taskId" }
        val queueTask = getTask(taskId) ?: return
        val downloader = getDownloader(taskId)
        if (downloader.state.value is DownloadState.Downloading) {
            downloader.cancel()
            onTaskEvent(DownloadTaskState.Cancel(queueTask))
            // 从 map 中移除
            //      downloaderMap.remove(taskId)
            logger.i { "已取消下载任务: $taskId" }
        } else {
            logger.i { "任务未在下载中，无法取消: $taskId" }
        }
    }

    /**
     * 等待下载器进入“终止态”。
     *
     * 终止态包含：完成、失败、取消、暂停。暂停也应视为本轮下载结束，以便释放并发名额。
     */
    private suspend fun awaitTerminalState(downloader: FileDownloader): DownloadState {
        val current = downloader.state.value
        if (current.isTerminal()) {
            return current
        }
        return downloader.state.first { it.isTerminal() }
    }

    /**
     * 判断下载状态是否为终端状态。
     *
     * 终端状态包含：完成、失败、取消、暂停。暂停也应视为本轮下载结束，以便释放并发名额。
     */
    private fun DownloadState.isTerminal(): Boolean = when (this) {
        is DownloadState.Complete,
        is DownloadState.Error,
        DownloadState.Cancelled,
        DownloadState.Paused -> true

        DownloadState.Idle,
        DownloadState.Start,
        is DownloadState.Downloading,
        DownloadState.Resumed -> false
    }

    /**
     * 启动一个异步的受控下载任务。
     *
     * 手动入口应使用该函数，避免阻塞调用线程；队列入口则应使用阻塞版本以维持队列语义。
     */
    private fun launchManagedDownload(
        queueTask: QueueTask,
        action: DownloadAction,
        policy: ActiveTaskPolicy
    ) {
        scope.launch {
            ensureManagedDownload(queueTask, action, policy)
        }
    }

    /**
     * 以“单任务单协程”的方式启动下载，确保同一任务不会重复占用并发名额。
     *
     * @param queueTask 队列任务。
     * @param action 本次触发的下载动作。
     * @param policy 当该任务已有受控下载协程在运行时的处理策略。
     */
    private suspend fun ensureManagedDownload(
        queueTask: QueueTask,
        action: DownloadAction,
        policy: ActiveTaskPolicy
    ) {
        val taskId = queueTask.taskId

        val existingJob = managedJobsMutex.withLock {
            managedJobs[taskId]?.takeIf { it.isActive }
        }

        if (existingJob != null) {
            when (policy) {
                ActiveTaskPolicy.JoinExisting -> {
                    logger.i { "任务已在运行，等待其结束: $taskId" }
                    existingJob.join()
                    return
                }

                ActiveTaskPolicy.CancelAndRestart -> {
                    logger.i { "任务已在运行，取消并重启: $taskId action=$action" }
                    existingJob.cancelAndJoin()
                }

                ActiveTaskPolicy.NoOp -> {
                    logger.i { "任务已在运行，忽略请求: $taskId action=$action" }
                    return
                }
            }
        }

        val currentJob = kotlinx.coroutines.currentCoroutineContext()[Job]
        if (currentJob == null) {
            // 理论上不会发生：本函数总在协程中调用。若发生则直接降级为一次性执行。
            runManagedDownload(queueTask, getDownloader(taskId), action)
            return
        }

        managedJobsMutex.withLock {
            managedJobs[taskId] = currentJob
        }
        try {
            runManagedDownload(queueTask, getDownloader(taskId), action)
        } finally {
            managedJobsMutex.withLock {
                if (managedJobs[taskId] == currentJob) {
                    managedJobs.remove(taskId)
                }
            }
        }
    }

    /**
     * 在全局并发闸门下执行一次下载动作，并等待进入终止态后再返回。
     *
     * 该函数是并发控制的唯一“源头”，所有入口（队列与手动）必须经由这里调度。
     */
    private suspend fun runManagedDownload(
        queueTask: QueueTask,
        downloader: FileDownloader,
        action: DownloadAction
    ) {
        val config = queueTask.config
        val taskId = queueTask.taskId

        if (action == DownloadAction.Resume && downloader.state.value != DownloadState.Paused) {
            logger.i { "任务不处于暂停态，忽略恢复请求: $taskId state=${downloader.state.value}" }
            return
        }

        downloadSemaphore.withPermit {
            logger.i { "开始受控下载任务: $taskId action=$action" }

            if (action != DownloadAction.Resume) {
                onTaskEvent(DownloadTaskState.Start(queueTask))
            }

            // 监听下载进度
            // 注意：同一 taskId 只允许存在一个状态监听，否则会导致事件重复派发
            stateObserverJobs.remove(taskId)?.cancel()
            stateObserverJobs[taskId] = downloader.state
                .dropWhile(1) { it !is DownloadState.Idle }
                .onEach { state ->
                    // 仅当状态是 Downloading 时才需要判断是否允许发送
                    val shouldEmit = if (state is DownloadState.Downloading) {
                        config.enableDownloadProgress   // 允许进度时才发送
                    } else {
                        true                            // 非下载进度状态全部允许发送
                    }
                    if (shouldEmit) {
                        onTaskEvent(DownloadTaskState.DownloadState(queueTask, state))
                    }
                }
                .launchIn(scope)

            when (action) {
                DownloadAction.Start -> {
                    if (downloader.state.value == DownloadState.Paused) {
                        downloader.resume()
                    } else {
                        downloader.download(config)
                    }
                }

                DownloadAction.Restart -> downloader.restart()
                DownloadAction.Resume -> downloader.resume()
            }

            when (val lastState = awaitTerminalState(downloader)) {
                is DownloadState.Error -> logger.e(lastState.exception) {
                    "下载失败: $taskId，错误信息: ${lastState.exception.message}"
                }

                is DownloadState.Complete -> logger.i { "下载完成: $taskId url: ${config.url}" }
                DownloadState.Cancelled -> logger.i { "下载取消: $taskId" }
                DownloadState.Paused -> logger.i { "下载暂停: $taskId" }
                else -> {}
            }
        }
    }

    fun <T> Flow<T>.dropWhile(
        count: Int,
        predicate: suspend (T) -> Boolean
    ): Flow<T> {
        require(count >= 0) { "Drop count should be non-negative, but had $count" }

        return flow {
            var index = 0

            collect { value ->
                if (index < count && predicate(value)) {
                    index++
                    return@collect
                }

                index++
                emit(value)
            }
        }
    }

    /**
     * 获取指定任务的下载状态。
     *
     * @param taskId 任务的唯一标识符。
     * @return 一个包含下载状态的 `StateFlow`，如果任务不存在则返回 `null`。
     */
    fun getDownloadState(taskId: Int): StateFlow<DownloadState>? {
        return downloaderMap[taskId]?.state
    }

    /**
     * 取消所有正在进行的下载任务并清空队列。
     */
    fun cancelAll() {
        logger.i { "取消所有下载任务" }
        // 遍历并取消所有下载器
        downloaderMap.values.forEach { it.cancel() }
    }

    /**
     * 清理并释放下载管理器占用的所有资源。
     * 这会取消所有任务并取消协程作用域。
     */
    fun cleanup() {
        logger.i { "清理下载管理器资源" }
        cancelAll()
        // 清空 map
        downloaderMap.clear()
        // 取消所有状态监听
        stateObserverJobs.values.forEach { it.cancel() }
        stateObserverJobs.clear()
        // 清空队列
        clearQueue()
        scope.cancel()
    }

    private fun getDownloader(taskId: Int): FileDownloader {
        return create(taskId)
    }

    private fun getTask(taskId: Int): QueueTask? {
        val queueTask = downloadQueue.find { it.taskId == taskId }
        if (queueTask == null) {
            logger.w { "队列任务不存在: $taskId" }
        }
        return queueTask
    }

    private enum class DownloadAction {
        Start,
        Restart,
        Resume
    }

    private enum class ActiveTaskPolicy {
        /**
         * 若任务已在运行，则等待其结束（不重复触发）。
         */
        JoinExisting,

        /**
         * 若任务已在运行，则取消并重启（保持“再次调用即重启”的语义）。
         */
        CancelAndRestart,

        /**
         * 若任务已在运行，则直接返回（不重复触发）。
         */
        NoOp
    }

}
