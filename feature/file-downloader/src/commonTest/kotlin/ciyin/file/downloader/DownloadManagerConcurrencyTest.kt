package ciyin.file.downloader

import ciyin.file.downloader.core.DownloadManager
import ciyin.file.downloader.core.DownloadState
import ciyin.file.downloader.core.DownloadTaskState
import ciyin.file.downloader.core.FileDownloader
import ciyin.file.downloader.model.DownloadConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import okio.Path.Companion.toPath
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertTrue

/** 下载管理器跨入口并发上限测试。 */
@OptIn(ExperimentalCoroutinesApi::class)
class DownloadManagerConcurrencyTest {

    /** 仅用于标识测试任务且不会发出网络请求的地址。 */
    private val fileUrl = "https://example.test/file"

    /** 测试任务保存目录。 */
    private val saveDir = "/downloads"

    /** 队列与手动启动叠加时，活跃下载数不超过上限 */
    @Test
    fun queueAndManualLaunchDoNotExceedActiveDownloadLimit() = runTest {
        val maxConcurrentDownloads = 2
        val controlledDownloaders = mutableMapOf<Int, ControlledFakeDownloader>()

        val manager = DownloadManager(
            maxConcurrentDownloads = maxConcurrentDownloads,
            scope = backgroundScope,
            downloaderFactory = { taskId, scope ->
                ControlledFakeDownloader(taskId, scope).also { controlledDownloaders[taskId] = it }
            }
        )

        val configs = (1..4).map { id ->
            DownloadConfig(
                id = id,
                url = fileUrl,
                savePath = "$saveDir/test-$id.zim",
                maxRetries = 0
            )
        }

        configs.forEach { manager.addToQueue(it) }

        val activeTaskIds = linkedSetOf<Int>()
        var peakActive = 0
        var everActiveTask4 = false

        manager.taskEvents.onEach { event ->
            if (event is DownloadTaskState.DownloadState) {
                val taskId = event.queueTask.taskId
                when (event.downloadState) {
                    DownloadState.Start,
                    DownloadState.Resumed,
                    is DownloadState.Downloading -> {
                        activeTaskIds += taskId
                        peakActive = max(peakActive, activeTaskIds.size)
                        if (taskId == 4) {
                            everActiveTask4 = true
                        }
                    }

                    DownloadState.Paused,
                    DownloadState.Cancelled,
                    is DownloadState.Error,
                    is DownloadState.Complete -> {
                        activeTaskIds -= taskId
                    }

                    DownloadState.Idle -> Unit
                }
            }
        }.launchIn(backgroundScope)

        manager.startQueue()
        manager.start(taskId = 4)
        runCurrent()

        awaitCondition { activeTaskIds.size == maxConcurrentDownloads }
        assertTrue(peakActive <= maxConcurrentDownloads, "峰值活跃数应不超过 $maxConcurrentDownloads，但实际为 $peakActive")

        // 逐个放行完成，确保队列与手动入口交错推进时也不会超限。
        repeat(4) {
            val toComplete = activeTaskIds.firstOrNull() ?: return@repeat
            controlledDownloaders.getValue(toComplete).completeSuccessfully()
            runCurrent()
            awaitCondition { activeTaskIds.size <= maxConcurrentDownloads }
            peakActive = max(peakActive, activeTaskIds.size)
        }

        // 收尾：确保所有任务都能走到终止态
        controlledDownloaders.values.forEach { it.completeSuccessfully() }
        runCurrent()

        assertTrue(everActiveTask4, "手动启动的任务 4 应至少进入一次活跃态")
        assertTrue(peakActive <= maxConcurrentDownloads, "峰值活跃数应不超过 $maxConcurrentDownloads，但实际为 $peakActive")
    }

    /** 暂停会释放并发名额，允许后续任务开始 */
    @Test
    fun pauseFreesConcurrentSlotAndAllowsNextTaskToStart() = runTest {
        val maxConcurrentDownloads = 1
        val controlledDownloaders = mutableMapOf<Int, ControlledFakeDownloader>()

        val manager = DownloadManager(
            maxConcurrentDownloads = maxConcurrentDownloads,
            scope = backgroundScope,
            downloaderFactory = { taskId, scope ->
                ControlledFakeDownloader(taskId, scope).also { controlledDownloaders[taskId] = it }
            }
        )

        val config1 = DownloadConfig(id = 1, url = fileUrl, savePath = "$saveDir/test-1.zim", maxRetries = 0)
        val config2 = DownloadConfig(id = 2, url = fileUrl, savePath = "$saveDir/test-2.zim", maxRetries = 0)
        manager.addToQueue(config1)
        manager.addToQueue(config2)

        val activeTaskIds = linkedSetOf<Int>()
        manager.taskEvents.onEach { event ->
            if (event is DownloadTaskState.DownloadState) {
                val taskId = event.queueTask.taskId
                when (event.downloadState) {
                    DownloadState.Start,
                    DownloadState.Resumed,
                    is DownloadState.Downloading -> activeTaskIds += taskId

                    DownloadState.Paused,
                    DownloadState.Cancelled,
                    is DownloadState.Error,
                    is DownloadState.Complete -> activeTaskIds -= taskId

                    DownloadState.Idle -> Unit
                }
            }
        }.launchIn(backgroundScope)

        manager.start(taskId = 1)
        runCurrent()
        awaitCondition { activeTaskIds.contains(1) }

        manager.pause(taskId = 1)
        runCurrent()
        awaitCondition { activeTaskIds.isEmpty() }

        manager.start(taskId = 2)
        runCurrent()
        awaitCondition { activeTaskIds.contains(2) }

        assertTrue(activeTaskIds.size <= maxConcurrentDownloads)
    }

    /** 挂起等待测试条件成立。 */
    private suspend fun awaitCondition(predicate: () -> Boolean) {
        while (predicate().not()) {
            yield()
        }
    }

    /** 由测试显式控制终态的下载器。 */
    private class ControlledFakeDownloader(
        private val taskId: Int,
        private val scope: CoroutineScope
    ) : FileDownloader {

        /** 内部可变状态。 */
        private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)

        /** 对外只读状态。 */
        override val state: StateFlow<DownloadState> = _state.asStateFlow()

        /** 当前下载协程。 */
        private var currentJob: Job? = null

        /** 当前会话终态信号。 */
        private var terminalSignal: CompletableDeferred<DownloadState>? = null

        override fun idle() {
            _state.value = DownloadState.Idle
        }

        override fun download(config: DownloadConfig): Job = startNewSession(initialState = DownloadState.Start)

        override fun restart(): Job = startNewSession(initialState = DownloadState.Start)

        override fun pause() {
            terminalSignal?.complete(DownloadState.Paused)
        }

        override fun resume(): Job {
            return if (_state.value == DownloadState.Paused) {
                startNewSession(initialState = DownloadState.Resumed)
            } else {
                scope.launch { }
            }
        }

        override fun cancel() {
            terminalSignal?.complete(DownloadState.Cancelled)
        }

        override fun cleanup() {
            terminalSignal?.complete(DownloadState.Cancelled)
            currentJob?.cancel()
        }

        /** 让当前下载会话成功完成。 */
        fun completeSuccessfully() {
            terminalSignal?.complete(DownloadState.Complete("fake/$taskId".toPath()))
        }

        /** 创建一个等待测试终态信号的新会话。 */
        private fun startNewSession(initialState: DownloadState): Job {
            currentJob?.cancel()
            val signal = CompletableDeferred<DownloadState>()
            terminalSignal = signal
            return scope.launch {
                _state.value = initialState
                _state.value = DownloadState.Downloading(
                    progress = 0.1f,
                    downloaded = 1L,
                    total = 10L,
                    speed = 1L
                )
                _state.value = signal.await()
            }.also { currentJob = it }
        }
    }
}
