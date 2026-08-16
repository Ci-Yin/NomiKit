package com.ciyin.app.ui.screen.filedownloader

import ciyin.file.downloader.core.DownloadState
import ciyin.file.downloader.core.FileDownloader
import ciyin.file.downloader.model.DownloadConfig
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [FileDownloaderDemoViewModel] 与页面映射器的行为测试。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FileDownloaderDemoViewModelTest {

    /** 页面配置应完整映射到公共下载配置。 */
    @Test
    fun uiStateMapsToDownloadConfig() {
        val config = FileDownloaderDemoUiState(
            url = "  https://example.test/file  ",
            savePath = "  /downloads/file.bin  ",
            enableResume = false,
            enableChunkedDownload = true,
            overwriteExisting = true,
        ).toDownloadConfig()

        assertEquals("https://example.test/file", config.url)
        assertEquals("/downloads/file.bin", config.savePath)
        assertFalse(config.enableResume)
        assertTrue(config.enableChunkedDownload)
        assertTrue(config.overwriteExisting)
    }

    /** 下载状态应映射进进度、速度、结果路径与错误字段。 */
    @Test
    fun downloadStateMapsToUiState() {
        val downloading = FileDownloaderDemoUiState().withDownloadState(
            DownloadState.Downloading(
                progress = 0.5f,
                downloaded = 50L,
                total = 100L,
                speed = 20L,
            )
        )
        assertEquals(FileDownloaderDemoPhase.Downloading, downloading.phase)
        assertEquals(0.5f, downloading.progress)
        assertEquals(50L, downloading.downloadedBytes)
        assertEquals(100L, downloading.totalBytes)
        assertEquals(20L, downloading.speedBytesPerSecond)

        val complete = downloading.withDownloadState(
            DownloadState.Complete("/downloads/file.bin".toPath())
        )
        assertEquals(FileDownloaderDemoPhase.Complete, complete.phase)
        assertEquals("/downloads/file.bin", complete.completedPath)
        assertNull(complete.errorMessage)

        val error = complete.withDownloadState(
            DownloadState.Error(null, IllegalStateException("boom"))
        )
        assertEquals(FileDownloaderDemoPhase.Error, error.phase)
        assertEquals("boom", error.errorMessage)
        assertNull(error.completedPath)
    }

    /** 默认路径只能填充空值，不能覆盖用户输入。 */
    @Test
    fun defaultPathDoesNotOverwriteUserInput() = runTest {
        withTestViewModel { viewModel, _ ->
            viewModel.dispatchAction(FileDownloaderDemoAction.SavePathChange("/user/file.bin"))
            runCurrent()
            viewModel.dispatchAction(
                FileDownloaderDemoAction.DefaultSavePathLoaded("/cache/default.bin")
            )
            runCurrent()

            assertEquals("/user/file.bin", viewModel.state.value.savePath)
        }
    }

    /** 开始及暂停、恢复、取消、重启动作应统一调用下载器 API。 */
    @Test
    fun controlActionsCallFileDownloaderApi() = runTest {
        withTestViewModel { viewModel, downloader ->
            viewModel.dispatchAction(
                FileDownloaderDemoAction.DefaultSavePathLoaded("/cache/file.bin")
            )
            runCurrent()
            viewModel.dispatchAction(FileDownloaderDemoAction.StartClick)
            runCurrent()

            assertEquals(1, downloader.downloadConfigs.size)
            assertEquals(DefaultDownloadUrl, downloader.downloadConfigs.single().url)

            downloader.emit(
                DownloadState.Downloading(
                    progress = 0.25f,
                    downloaded = 25L,
                    total = 100L,
                    speed = 10L,
                )
            )
            runCurrent()
            assertEquals(FileDownloaderDemoPhase.Downloading, viewModel.state.value.phase)
            viewModel.dispatchAction(FileDownloaderDemoAction.PauseClick)
            runCurrent()
            assertEquals(1, downloader.pauseCount)

            downloader.emit(DownloadState.Paused)
            runCurrent()
            viewModel.dispatchAction(FileDownloaderDemoAction.ResumeClick)
            runCurrent()
            assertEquals(1, downloader.resumeCount)

            downloader.emit(DownloadState.Downloading(0.5f, 50L, 100L, 10L))
            runCurrent()
            viewModel.dispatchAction(FileDownloaderDemoAction.CancelClick)
            runCurrent()
            assertEquals(1, downloader.cancelCount)

            downloader.emit(DownloadState.Error("failure", IllegalStateException("failure")))
            runCurrent()
            viewModel.dispatchAction(FileDownloaderDemoAction.RestartClick)
            runCurrent()
            assertEquals(1, downloader.restartCount)
        }
    }

    /** 返回动作应产生一次导航副作用。 */
    @Test
    fun backActionEmitsNavigateBackEffect() = runTest {
        withTestViewModel { viewModel, _ ->
            val effect = async(start = CoroutineStart.UNDISPATCHED) {
                viewModel.sideEffects.first()
            }

            viewModel.dispatchAction(FileDownloaderDemoAction.BackClick)

            assertIs<FileDownloaderDemoEffect.NavigateBack>(effect.await())
        }
    }

    /** 在同一个测试调度器上创建并释放 ViewModel。 */
    private suspend fun kotlinx.coroutines.test.TestScope.withTestViewModel(
        block: suspend (
            viewModel: FileDownloaderDemoViewModel,
            downloader: RecordingFileDownloader,
        ) -> Unit,
    ) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val downloader = RecordingFileDownloader()
        val viewModel = FileDownloaderDemoViewModel { downloader }
        val stateCollection = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { }
        }
        try {
            runCurrent()
            block(viewModel, downloader)
        } finally {
            stateCollection.cancelAndJoin()
            Dispatchers.resetMain()
        }
    }

    /** 可记录控制调用并主动发布状态的测试下载器。 */
    private class RecordingFileDownloader : FileDownloader {

        /** 内部可变下载状态。 */
        private val mutableState = MutableStateFlow<DownloadState>(DownloadState.Idle)

        /** 所有收到的下载配置。 */
        val downloadConfigs = mutableListOf<DownloadConfig>()

        /** 暂停调用次数。 */
        var pauseCount = 0

        /** 恢复调用次数。 */
        var resumeCount = 0

        /** 取消调用次数。 */
        var cancelCount = 0

        /** 重启调用次数。 */
        var restartCount = 0

        /** 只读下载状态。 */
        override val state: StateFlow<DownloadState> = mutableState

        /** 发布空闲状态。 */
        override fun idle() {
            mutableState.value = DownloadState.Idle
        }

        /** 记录配置并发布开始状态。 */
        override fun download(config: DownloadConfig): Job {
            downloadConfigs += config
            mutableState.value = DownloadState.Start
            return completedJob()
        }

        /** 记录重启操作。 */
        override fun restart(): Job {
            restartCount += 1
            mutableState.value = DownloadState.Start
            return completedJob()
        }

        /** 记录暂停操作。 */
        override fun pause() {
            pauseCount += 1
        }

        /** 记录恢复操作。 */
        override fun resume(): Job {
            resumeCount += 1
            return completedJob()
        }

        /** 记录取消操作。 */
        override fun cancel() {
            cancelCount += 1
        }

        /** 测试实现无需释放外部资源。 */
        override fun cleanup() = Unit

        /** 发布指定下载状态。 */
        fun emit(value: DownloadState) {
            mutableState.value = value
        }

        /** 创建已完成 Job。 */
        private fun completedJob(): Job = Job().apply { complete() }
    }
}
