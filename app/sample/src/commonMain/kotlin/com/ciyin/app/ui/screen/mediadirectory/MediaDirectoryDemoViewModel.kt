package com.ciyin.app.ui.screen.mediadirectory

import androidx.lifecycle.viewModelScope
import ciyin.io.File
import ciyin.io.SystemFileSystem
import ciyin.io.resolve
import ciyin.io.writeText
import ciyin.media.directory.MediaDirectoryException
import ciyin.media.directory.MediaDirectorySession
import ciyin.platform.Context
import ciyin.platform.files
import ciyin.platform.time.currentTimeMillis
import ciyin.ui.foundation.viewmodel.StateMachineMviViewModel
import com.freeletics.flowredux2.FlowReduxBuilder
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 编排"选择目录 → 验证 → 提交 → 检查 → 删除"完整闭环。
 *
 * @param sessionOverride 测试可注入的会话实现。
 * @param accessFactoryOverride 页面级可选 Access 工厂；非空时优先于 [MediaDirectorySession.requestAccess]。
 *        用于 Android 端 `rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree())`
 *        拿到 SAF Tree URI 后立刻 take persistable permission 并查询目录展示名，返回最终的
 *        [ciyin.media.directory.MediaDirectoryAccess]。
 * @param sampleSourceBytes 内置示例字节读取器。
 * @param sampleFileName 示例提交文件名。
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class MediaDirectoryDemoViewModel(
    /** 测试可注入的会话实现。 */
    private val sessionOverride: MediaDirectorySession? = null,
    /** 页面级可选 Access 工厂；非空时优先于 [MediaDirectorySession.requestAccess]。 */
    private val accessFactoryOverride: (suspend () -> ciyin.media.directory.MediaDirectoryAccess?)? = null,
    /** 当前平台上下文（用于写示例源文件到 cache 目录）。 */
    private val contextProvider: () -> Context = {
        org.koin.core.context.GlobalContext.get().get()
    },
    /** 内置示例字节读取器。 */
    private val sampleSourceBytes: () -> ByteArray = { DEFAULT_SAMPLE_BYTES },
    /** 示例提交文件名。 */
    private val sampleFileName: () -> String = {
        "nomikit-media-directory-demo-${currentTimeMillis()}.txt"
    },
) : StateMachineMviViewModel<
        MediaDirectoryDemoUiState,
        MediaDirectoryDemoAction,
        MediaDirectoryDemoEffect,
        >(), KoinComponent {
    /** KoinBoot 自动装配的用户媒体目录会话。 */
    private val injectedSession: MediaDirectorySession by inject()

    /** 测试覆盖优先的当前会话。 */
    private val session: MediaDirectorySession
        get() = sessionOverride ?: injectedSession

    /** 当前唯一运行中的操作。 */
    private var operationJob: Job? = null

    /** 初始化示例页面状态。 */
    override fun FlowReduxStateMachineFactory<
            MediaDirectoryDemoUiState,
            MediaDirectoryDemoAction,
            >.initialize() {
        initializeWith { MediaDirectoryDemoUiState() }
    }

    /** 声明选择、验证、提交、检查、删除与导航流转。 */
    override fun FlowReduxBuilder<MediaDirectoryDemoUiState, MediaDirectoryDemoAction>.spec() {
        inState<MediaDirectoryDemoUiState> {
            onActionEffect<MediaDirectoryDemoAction.BackClick> {
                poseEffect(MediaDirectoryDemoEffect.NavigateBack)
            }

            on<MediaDirectoryDemoAction.PickDirectoryClick> { action ->
                if (!canStartOperation(snapshot)) {
                    noChange()
                } else {
                    startOperation(snapshot) {
                        runPick()
                    }
                    mutate {
                        copy(
                            phase = MediaDirectoryDemoPhase.Picking,
                            error = null,
                            isBusy = true,
                        )
                    }
                }
            }

            on<MediaDirectoryDemoAction.DirectoryPicked> { action ->
                mutate {
                    copy(
                        accessState = MediaDirectoryDemoAccessState.Granted,
                        access = MediaDirectoryDemoAccessModel(
                            token = action.token,
                            displayName = action.displayName,
                            canonicalPath = action.canonicalPath,
                        ),
                        phase = MediaDirectoryDemoPhase.Idle,
                        isBusy = false,
                        error = null,
                    )
                }
            }

            on<MediaDirectoryDemoAction.DirectoryCancelled> { action ->
                mutate {
                    copy(
                        phase = MediaDirectoryDemoPhase.Idle,
                        isBusy = false,
                    )
                }
            }

            on<MediaDirectoryDemoAction.ValidateClick> { action ->
                val token = snapshot.access?.token ?: return@on noChange()
                if (!canStartOperation(snapshot)) {
                    noChange()
                } else {
                    startOperation(snapshot) {
                        runValidate(token)
                    }
                    mutate {
                        copy(
                            phase = MediaDirectoryDemoPhase.Validating,
                            isBusy = true,
                            error = null,
                        )
                    }
                }
            }

            on<MediaDirectoryDemoAction.Validated> { action ->
                mutate {
                    copy(
                        accessState = MediaDirectoryDemoAccessState.Granted,
                        phase = MediaDirectoryDemoPhase.Idle,
                        isBusy = false,
                    )
                }
            }

            on<MediaDirectoryDemoAction.ValidationRevoked> { action ->
                mutate {
                    copy(
                        accessState = MediaDirectoryDemoAccessState.Revoked,
                        phase = MediaDirectoryDemoPhase.Idle,
                        isBusy = false,
                    )
                }
            }

            on<MediaDirectoryDemoAction.CommitSampleClick> { action ->
                val token = snapshot.access?.token ?: return@on noChange()
                if (!canStartOperation(snapshot)) {
                    noChange()
                } else {
                    startOperation(snapshot) {
                        runCommit(token)
                    }
                    mutate {
                        copy(
                            phase = MediaDirectoryDemoPhase.Committing,
                            isBusy = true,
                            error = null,
                        )
                    }
                }
            }

            on<MediaDirectoryDemoAction.Committed> { action ->
                mutate {
                    copy(
                        phase = MediaDirectoryDemoPhase.Idle,
                        isBusy = false,
                        lastEntry = MediaDirectoryDemoEntryModel(
                            fileName = action.fileName,
                            location = action.location,
                            mimeType = action.mimeType,
                            size = action.size,
                            exists = action.exists,
                        ),
                        error = null,
                    )
                }
            }

            on<MediaDirectoryDemoAction.ExistsClick> { action ->
                val token = snapshot.access?.token ?: return@on noChange()
                val location = snapshot.lastEntry?.location ?: return@on noChange()
                if (!canStartOperation(snapshot)) {
                    noChange()
                } else {
                    startOperation(snapshot) {
                        runExists(token, location)
                    }
                    mutate {
                        copy(
                            phase = MediaDirectoryDemoPhase.Deleting,
                            isBusy = true,
                            error = null,
                        )
                    }
                }
            }

            on<MediaDirectoryDemoAction.ExistsLoaded> { action ->
                mutate {
                    copy(
                        phase = MediaDirectoryDemoPhase.Idle,
                        isBusy = false,
                        lastEntry = lastEntry?.copy(exists = action.exists),
                    )
                }
            }

            on<MediaDirectoryDemoAction.DeleteClick> { action ->
                val token = snapshot.access?.token ?: return@on noChange()
                val location = snapshot.lastEntry?.location ?: return@on noChange()
                if (!canStartOperation(snapshot)) {
                    noChange()
                } else {
                    startOperation(snapshot) {
                        runDelete(token, location)
                    }
                    mutate {
                        copy(
                            phase = MediaDirectoryDemoPhase.Deleting,
                            isBusy = false,
                            error = null,
                        )
                    }
                }
            }

            on<MediaDirectoryDemoAction.Deleted> { action ->
                mutate {
                    copy(
                        phase = MediaDirectoryDemoPhase.Idle,
                        isBusy = false,
                        lastEntry = null,
                    )
                }
            }

            on<MediaDirectoryDemoAction.OperationFailed> { action ->
                mutate {
                    copy(
                        phase = MediaDirectoryDemoPhase.Idle,
                        isBusy = false,
                        error = MediaDirectoryDemoErrorModel(action.type),
                    )
                }
            }

            on<MediaDirectoryDemoAction.ClearError> { action ->
                mutate { copy(error = null) }
            }
        }
    }

    /** 页面状态和任务门闩都空闲时才允许开始新操作。 */
    private fun canStartOperation(state: MediaDirectoryDemoUiState): Boolean =
        !state.isBusy && operationJob?.isActive != true

    /** 启动一个新的异步操作。 */
    private fun startOperation(
        state: MediaDirectoryDemoUiState,
        block: suspend () -> Unit,
    ) {
        operationJob = viewModelScope.launch {
            try {
                block()
            } finally {
                operationJob = null
            }
        }
    }

    /** 拉起目录选择并把结果回写成 Action。 */
    private suspend fun runPick() {
        try {
            val access = if (accessFactoryOverride != null) {
                accessFactoryOverride.invoke()
            } else {
                session.requestAccess()
            }
            if (access == null) {
                dispatchAction(MediaDirectoryDemoAction.DirectoryCancelled)
                return
            }
            dispatchAction(
                MediaDirectoryDemoAction.DirectoryPicked(
                    token = access.accessToken,
                    displayName = access.displayName,
                    canonicalPath = access.canonicalPath,
                ),
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MediaDirectoryException) {
            if (exception.error is ciyin.media.directory.MediaDirectoryError.Cancelled) {
                dispatchAction(MediaDirectoryDemoAction.DirectoryCancelled)
            } else {
                dispatchAction(
                    MediaDirectoryDemoAction.OperationFailed(exception.error.toDemoType()),
                )
            }
        } catch (exception: Throwable) {
            dispatchAction(
                MediaDirectoryDemoAction.OperationFailed(
                    MediaDirectoryDemoErrorType.Io,
                ),
            )
        }
    }

    /** 验证目录仍然有效。 */
    private suspend fun runValidate(token: ciyin.media.directory.MediaDirectoryAccessToken) {
        try {
            session.validate(token)
            dispatchAction(MediaDirectoryDemoAction.Validated)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MediaDirectoryException) {
            val isRevoked =
                exception.error is ciyin.media.directory.MediaDirectoryError.AccessRevoked
            dispatchAction(
                if (isRevoked) {
                    MediaDirectoryDemoAction.ValidationRevoked
                } else {
                    MediaDirectoryDemoAction.OperationFailed(exception.error.toDemoType())
                },
            )
        } catch (exception: Throwable) {
            dispatchAction(
                MediaDirectoryDemoAction.OperationFailed(
                    MediaDirectoryDemoErrorType.Io,
                ),
            )
        }
    }

    /** 提交内置示例字节到当前授权目录。 */
    private suspend fun runCommit(token: ciyin.media.directory.MediaDirectoryAccessToken) {
        try {
            val source = prepareSampleSourceFile()
            val fileName = sampleFileName()
            val entry = session.commit(
                accessToken = token,
                source = source,
                fileName = fileName,
                mimeType = "text/plain",
            )
            dispatchAction(
                MediaDirectoryDemoAction.Committed(
                    fileName = entry.fileName,
                    location = entry.location,
                    mimeType = entry.mimeType,
                    size = entry.size,
                    exists = true,
                ),
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MediaDirectoryException) {
            dispatchAction(
                MediaDirectoryDemoAction.OperationFailed(exception.error.toDemoType()),
            )
        } catch (exception: Throwable) {
            dispatchAction(
                MediaDirectoryDemoAction.OperationFailed(
                    MediaDirectoryDemoErrorType.Io,
                ),
            )
        }
    }

    /** 查询最近条目是否仍存在。 */
    private suspend fun runExists(
        token: ciyin.media.directory.MediaDirectoryAccessToken,
        location: String,
    ) {
        try {
            val exists = session.exists(token, location)
            dispatchAction(MediaDirectoryDemoAction.ExistsLoaded(exists))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MediaDirectoryException) {
            dispatchAction(
                MediaDirectoryDemoAction.OperationFailed(exception.error.toDemoType()),
            )
        } catch (exception: Throwable) {
            dispatchAction(
                MediaDirectoryDemoAction.OperationFailed(
                    MediaDirectoryDemoErrorType.Io,
                ),
            )
        }
    }

    /** 删除最近条目。 */
    private suspend fun runDelete(
        token: ciyin.media.directory.MediaDirectoryAccessToken,
        location: String,
    ) {
        try {
            session.delete(token, location)
            dispatchAction(MediaDirectoryDemoAction.Deleted)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MediaDirectoryException) {
            dispatchAction(
                MediaDirectoryDemoAction.OperationFailed(exception.error.toDemoType()),
            )
        } catch (exception: Throwable) {
            dispatchAction(
                MediaDirectoryDemoAction.OperationFailed(
                    MediaDirectoryDemoErrorType.Io,
                ),
            )
        }
    }

    /** 准备示例源文件到当前平台 cache 目录。 */
    private suspend fun prepareSampleSourceFile(): File {
        val directory = contextProvider().files.cacheDir
            .resolve("media-directory-demo")
            .apply { mkdirs() }
        val source = directory.resolve("media-directory-sample.txt")
        source.writeText("")
        SystemFileSystem.write(source.toPath()) {
            write(sampleSourceBytes())
        }
        return source
    }

    /** 把 [ciyin.media.directory.MediaDirectoryError] 转换为页面错误分类。 */
    private fun ciyin.media.directory.MediaDirectoryError.toDemoType(): MediaDirectoryDemoErrorType =
        toDemoModel().type

    private companion object {
        /** 示例使用的固定字节内容。 */
        val DEFAULT_SAMPLE_BYTES: ByteArray =
            "NomiKit user-media-directory demo payload.".toByteArray()
    }
}
