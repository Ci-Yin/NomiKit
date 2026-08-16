package com.ciyin.app.ui.screen.filedownloader

import ciyin.file.downloader.core.FileDownloader
import ciyin.file.downloader.fileDownloader
import ciyin.ui.foundation.viewmodel.StateMachineMviViewModel
import com.freeletics.flowredux2.ExecutionPolicy
import com.freeletics.flowredux2.FlowReduxBuilder
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob

/**
 * 文件下载示例页面的 ViewModel。
 *
 * @param downloaderFactory 下载器工厂，测试可注入可控实现。
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class FileDownloaderDemoViewModel(
    private val downloaderFactory: (CoroutineScope) -> FileDownloader = ::fileDownloader,
) : StateMachineMviViewModel<
        FileDownloaderDemoUiState,
        FileDownloaderDemoAction,
        FileDownloaderDemoEffect,
        >() {

    /** 下载器独立作用域，避免清理下载器时取消 ViewModel 的状态机。 */
    private val downloaderScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 延迟创建下载器，保持构造阶段无网络请求。 */
    private val downloaderDelegate = lazy { downloaderFactory(downloaderScope) }

    /** 当前页面使用的单任务下载器。 */
    private val downloader: FileDownloader by downloaderDelegate

    /** 初始化页面状态。 */
    override fun FlowReduxStateMachineFactory<
            FileDownloaderDemoUiState,
            FileDownloaderDemoAction,
            >.initialize() {
        initializeWith { FileDownloaderDemoUiState() }
    }

    /** 声明页面动作、下载状态流与不可变状态之间的转换。 */
    override fun FlowReduxBuilder<FileDownloaderDemoUiState, FileDownloaderDemoAction>.spec() {
        inState<FileDownloaderDemoUiState> {
            // 顺序消费下载器状态，避免高频进度与终态发生重排。
            collectWhileInState(
                flow = downloader.state,
                executionPolicy = ExecutionPolicy.Ordered,
            ) { downloadState ->
                mutate { withDownloadState(downloadState) }
            }

            // 返回操作只产生导航副作用。
            onActionEffect<FileDownloaderDemoAction.BackClick> {
                poseEffect(FileDownloaderDemoEffect.NavigateBack)
            }

            // 默认路径仅填充空白输入，避免覆盖用户先行编辑的内容。
            on<FileDownloaderDemoAction.DefaultSavePathLoaded> { action ->
                if (snapshot.savePath.isBlank()) {
                    mutate { copy(savePath = action.value) }
                } else {
                    noChange()
                }
            }

            // 编辑下载地址。
            on<FileDownloaderDemoAction.UrlChange> { action ->
                if (snapshot.canEditConfig) {
                    mutate { copy(url = action.value, errorMessage = null) }
                } else {
                    noChange()
                }
            }

            // 编辑保存路径。
            on<FileDownloaderDemoAction.SavePathChange> { action ->
                if (snapshot.canEditConfig) {
                    mutate { copy(savePath = action.value, errorMessage = null) }
                } else {
                    noChange()
                }
            }

            // 切换断点续传配置。
            on<FileDownloaderDemoAction.EnableResumeChange> { action ->
                if (snapshot.canEditConfig) {
                    mutate { copy(enableResume = action.value) }
                } else {
                    noChange()
                }
            }

            // 切换分块下载配置。
            on<FileDownloaderDemoAction.EnableChunkedDownloadChange> { action ->
                if (snapshot.canEditConfig) {
                    mutate { copy(enableChunkedDownload = action.value) }
                } else {
                    noChange()
                }
            }

            // 切换覆盖已有文件配置。
            on<FileDownloaderDemoAction.OverwriteExistingChange> { action ->
                if (snapshot.canEditConfig) {
                    mutate { copy(overwriteExisting = action.value) }
                } else {
                    noChange()
                }
            }

            // 校验输入后启动下载任务。
            on<FileDownloaderDemoAction.StartClick> {
                when {
                    snapshot.url.isBlank() -> mutate { copy(errorMessage = "请输入下载地址") }
                    snapshot.savePath.isBlank() -> mutate { copy(errorMessage = "请输入保存路径") }
                    snapshot.canStart.not() -> noChange()
                    else -> {
                        downloader.download(snapshot.toDownloadConfig())
                        mutate {
                            copy(
                                phase = FileDownloaderDemoPhase.Starting,
                                progress = null,
                                downloadedBytes = 0L,
                                totalBytes = 0L,
                                speedBytesPerSecond = 0L,
                                completedPath = null,
                                errorMessage = null,
                            )
                        }
                    }
                }
            }

            // 暂停当前任务。
            on<FileDownloaderDemoAction.PauseClick> {
                if (snapshot.canPause) downloader.pause()
                noChange()
            }

            // 恢复已暂停任务。
            on<FileDownloaderDemoAction.ResumeClick> {
                if (snapshot.canResume) downloader.resume()
                noChange()
            }

            // 取消当前任务。
            on<FileDownloaderDemoAction.CancelClick> {
                if (snapshot.canCancel) downloader.cancel()
                noChange()
            }

            // 使用最近一次配置重新开始任务。
            on<FileDownloaderDemoAction.RestartClick> {
                if (snapshot.canRestart) {
                    downloader.restart()
                    mutate {
                        copy(
                            phase = FileDownloaderDemoPhase.Starting,
                            progress = null,
                            downloadedBytes = 0L,
                            totalBytes = 0L,
                            speedBytesPerSecond = 0L,
                            completedPath = null,
                            errorMessage = null,
                        )
                    }
                } else {
                    noChange()
                }
            }
        }
    }

    /** 清理页面持有的下载器与网络资源。 */
    override fun onCleared() {
        if (downloaderDelegate.isInitialized()) {
            downloader.cleanup()
        }
        super.onCleared()
    }
}
