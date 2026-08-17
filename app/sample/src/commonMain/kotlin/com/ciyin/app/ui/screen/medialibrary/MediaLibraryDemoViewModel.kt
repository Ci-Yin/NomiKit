package com.ciyin.app.ui.screen.medialibrary

import ciyin.io.File
import ciyin.io.SystemFileSystem
import ciyin.io.resolve
import ciyin.media.library.MediaCollection
import ciyin.media.library.MediaLibrary
import ciyin.media.library.MediaLibraryError
import ciyin.media.library.MediaLibraryException
import ciyin.media.library.MediaPublishRequest
import ciyin.media.library.PublishedMedia
import ciyin.platform.Context
import ciyin.platform.files
import ciyin.platform.time.currentTimeMillis
import ciyin.ui.foundation.viewmodel.StateMachineMviViewModel
import com.ciyin.app.sample.Res
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
 * 编排四类内置媒体的单项和批量系统媒体库操作。
 *
 * @param mediaLibraryOverride 测试可注入的媒体库实现。
 * @param sourceBytesProvider 内置媒体字节读取器。
 * @param displayNameProvider 唯一显示名称提供器。
 * @param permissionRequester 平台媒体库权限请求器。
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class MediaLibraryDemoViewModel(
    /** 测试可注入的媒体库实现。 */
    private val mediaLibraryOverride: MediaLibrary? = null,
    /** 内置媒体字节读取器。 */
    private val sourceBytesProvider: suspend (MediaLibraryDemoSample) -> ByteArray = { sample ->
        Res.readBytes(sample.resourcePath)
    },
    /** 唯一显示名称提供器。 */
    private val displayNameProvider: (MediaLibraryDemoSample) -> String = { sample ->
        val extension = sample.sourceFileName.substringAfterLast('.')
        "nomikit-media-library-${sample.displayNameStem}-${currentTimeMillis()}.$extension"
    },
    /** 平台媒体库权限请求器。 */
    private val permissionRequester: suspend (Context, MediaCollection) -> Boolean =
        ::ensureMediaLibraryDemoPermission,
) : StateMachineMviViewModel<
    MediaLibraryDemoUiState,
    MediaLibraryDemoAction,
    MediaLibraryDemoEffect,
    >(), KoinComponent {
    /** KoinBoot 自动装配的系统媒体库。 */
    private val injectedMediaLibrary: MediaLibrary by inject()

    /** 测试覆盖优先的当前媒体库。 */
    private val mediaLibrary: MediaLibrary
        get() = mediaLibraryOverride ?: injectedMediaLibrary

    /** 当前仍可执行检查或删除的原始平台引用。 */
    private val activePublishedMedia = mutableMapOf<MediaLibraryDemoSampleId, PublishedMedia>()

    /** 当前唯一运行中的单项或批量任务。 */
    private var operationJob: Job? = null

    /** 初始化系统媒体库测试台状态。 */
    override fun FlowReduxStateMachineFactory<
        MediaLibraryDemoUiState,
        MediaLibraryDemoAction,
        >.initialize() {
        initializeWith { MediaLibraryDemoUiState() }
    }

    /** 声明测试选择、资源加载、单项操作、批量操作与导航流转。 */
    override fun FlowReduxBuilder<MediaLibraryDemoUiState, MediaLibraryDemoAction>.spec() {
        inState<MediaLibraryDemoUiState> {
            // 页面首次进入时读取四个内置资源的实际大小。
            onEnterEffect {
                loadSampleSizes()
            }

            // 用户点击返回时发送导航副作用。
            onActionEffect<MediaLibraryDemoAction.BackClick> {
                poseEffect(MediaLibraryDemoEffect.NavigateBack)
            }

            // 用户选择测试后只切换详情，不影响任何运行结果。
            on<MediaLibraryDemoAction.SampleSelect> { action ->
                mutate { copy(selectedSampleId = action.sampleId) }
            }

            // 内置资源加载后逐项补齐真实字节大小。
            on<MediaLibraryDemoAction.SampleSizesLoaded> { action ->
                mutate {
                    copy(
                        items = items.map { item ->
                            item.copy(sourceSize = action.sizes[item.sampleId] ?: item.sourceSize)
                        },
                    )
                }
            }

            // 用户发布单个样本时建立独占操作任务。
            on<MediaLibraryDemoAction.PublishClick> { action ->
                val item = snapshot.item(action.sampleId)
                if (!canStartOperation(snapshot) || item.hasActiveReference) {
                    noChange()
                } else {
                    startSingleOperation(
                        context = action.context,
                        sampleId = action.sampleId,
                        operation = MediaLibraryDemoBatchOperation.Publish,
                    )
                    mutate {
                        updateItem(action.sampleId) { current ->
                            current.copy(
                                phase = MediaLibraryDemoPhase.Publishing,
                                error = null,
                            )
                        }
                    }
                }
            }

            // 用户检查单个样本时只允许操作当前活动引用。
            on<MediaLibraryDemoAction.ExistsClick> { action ->
                val media = activePublishedMedia[action.sampleId]
                if (!canStartOperation(snapshot) || media == null) {
                    noChange()
                } else {
                    startSingleOperation(
                        context = action.context,
                        sampleId = action.sampleId,
                        operation = MediaLibraryDemoBatchOperation.Check,
                        media = media,
                    )
                    mutate {
                        updateItem(action.sampleId) { current ->
                            current.copy(
                                phase = MediaLibraryDemoPhase.Checking,
                                error = null,
                            )
                        }
                    }
                }
            }

            // 用户删除单个样本时只允许操作当前活动引用。
            on<MediaLibraryDemoAction.DeleteClick> { action ->
                val media = activePublishedMedia[action.sampleId]
                if (!canStartOperation(snapshot) || media == null) {
                    noChange()
                } else {
                    startSingleOperation(
                        context = action.context,
                        sampleId = action.sampleId,
                        operation = MediaLibraryDemoBatchOperation.Delete,
                        media = media,
                    )
                    mutate {
                        updateItem(action.sampleId) { current ->
                            current.copy(
                                phase = MediaLibraryDemoPhase.Deleting,
                                error = null,
                            )
                        }
                    }
                }
            }

            // 用户批量发布时按定义顺序遍历全部测试。
            on<MediaLibraryDemoAction.PublishAllClick> { action ->
                startBatchOrKeepState(
                    state = snapshot,
                    context = action.context,
                    operation = MediaLibraryDemoBatchOperation.Publish,
                )?.let { running ->
                    mutate { copy(batch = running) }
                } ?: noChange()
            }

            // 用户批量检查时按定义顺序遍历全部活动引用。
            on<MediaLibraryDemoAction.ExistsAllClick> { action ->
                startBatchOrKeepState(
                    state = snapshot,
                    context = action.context,
                    operation = MediaLibraryDemoBatchOperation.Check,
                )?.let { running ->
                    mutate { copy(batch = running) }
                } ?: noChange()
            }

            // 用户批量清理时按定义顺序删除全部活动引用。
            on<MediaLibraryDemoAction.DeleteAllClick> { action ->
                startBatchOrKeepState(
                    state = snapshot,
                    context = action.context,
                    operation = MediaLibraryDemoBatchOperation.Delete,
                )?.let { running ->
                    mutate { copy(batch = running) }
                } ?: noChange()
            }

            // 批量任务开始处理某项时更新对应的局部阶段。
            on<MediaLibraryDemoAction.ItemOperationStarted> { action ->
                mutate {
                    updateItem(action.sampleId) { current ->
                        current.copy(
                            phase = action.operation.runningPhase(),
                            error = null,
                        )
                    }
                }
            }

            // 发布成功后保存展示模型并标记引用存在。
            on<MediaLibraryDemoAction.Published> { action ->
                mutate {
                    updateItem(action.sampleId) { current ->
                        current.copy(
                            phase = MediaLibraryDemoPhase.Published,
                            published = action.media.toDemoModel(),
                            exists = true,
                            error = null,
                        )
                    }
                }
            }

            // 存在性检查后区分已存在和已丢失状态。
            on<MediaLibraryDemoAction.ExistsLoaded> { action ->
                mutate {
                    updateItem(action.sampleId) { current ->
                        current.copy(
                            phase = if (action.exists) {
                                MediaLibraryDemoPhase.Published
                            } else {
                                MediaLibraryDemoPhase.Missing
                            },
                            exists = action.exists,
                            error = null,
                        )
                    }
                }
            }

            // 删除成功后保留最近结果并释放活动引用。
            on<MediaLibraryDemoAction.Deleted> { action ->
                mutate {
                    updateItem(action.sampleId) { current ->
                        current.copy(
                            phase = MediaLibraryDemoPhase.Deleted,
                            exists = false,
                            error = null,
                        )
                    }
                }
            }

            // 单项错误只更新对应测试并保留其他项目结果。
            on<MediaLibraryDemoAction.OperationFailed> { action ->
                mutate {
                    updateItem(action.sampleId) { current ->
                        current.copy(
                            phase = if (action.error is MediaLibraryError.Unsupported) {
                                MediaLibraryDemoPhase.Unsupported
                            } else {
                                MediaLibraryDemoPhase.Failed
                            },
                            error = action.error.toDemoModel(),
                        )
                    }
                }
            }

            // 每处理一个批量项目后刷新汇总进度。
            on<MediaLibraryDemoAction.BatchProgressed> { action ->
                mutate {
                    copy(
                        batch = MediaLibraryDemoBatchState.Running(
                            operation = action.operation,
                            summary = action.summary,
                        ),
                    )
                }
            }

            // 批量任务遍历完成或遇到全局阻塞错误后输出最终状态。
            on<MediaLibraryDemoAction.BatchFinished> { action ->
                mutate {
                    copy(
                        batch = action.blocker?.let { blocker ->
                            MediaLibraryDemoBatchState.Stopped(
                                operation = action.operation,
                                summary = action.summary,
                                error = blocker.toDemoModel(),
                            )
                        } ?: MediaLibraryDemoBatchState.Completed(
                            operation = action.operation,
                            summary = action.summary,
                        ),
                    )
                }
            }
        }
    }

    /** 页面状态和任务门闩都空闲时才允许开始新操作。 */
    private fun canStartOperation(state: MediaLibraryDemoUiState): Boolean =
        !state.isBusy && operationJob?.isActive != true

    /** 启动单个媒体测试操作。 */
    private fun startSingleOperation(
        context: Context,
        sampleId: MediaLibraryDemoSampleId,
        operation: MediaLibraryDemoBatchOperation,
        media: PublishedMedia? = null,
    ) {
        operationJob = backgroundScope.launch {
            try {
                performOperation(
                    context = context,
                    sampleId = sampleId,
                    operation = operation,
                    media = media,
                )
            } finally {
                operationJob = null
            }
        }
    }

    /** 空闲时启动批量任务并返回初始运行状态，否则保持原状态。 */
    private fun startBatchOrKeepState(
        state: MediaLibraryDemoUiState,
        context: Context,
        operation: MediaLibraryDemoBatchOperation,
    ): MediaLibraryDemoBatchState.Running? {
        if (!canStartOperation(state)) return null
        val summary = MediaLibraryDemoBatchSummary()
        operationJob = backgroundScope.launch {
            try {
                runBatch(
                    context = context,
                    operation = operation,
                    initialSummary = summary,
                )
            } finally {
                operationJob = null
            }
        }
        return MediaLibraryDemoBatchState.Running(
            operation = operation,
            summary = summary,
        )
    }

    /** 按四类样本的固定顺序执行批量操作和分类失败策略。 */
    private suspend fun runBatch(
        context: Context,
        operation: MediaLibraryDemoBatchOperation,
        initialSummary: MediaLibraryDemoBatchSummary,
    ) {
        var summary = initialSummary
        var blocker: MediaLibraryError? = null
        for (sample in mediaLibraryDemoSamples) {
            val media = activePublishedMedia[sample.id]
            val shouldSkip = when (operation) {
                MediaLibraryDemoBatchOperation.Publish -> media != null
                MediaLibraryDemoBatchOperation.Check,
                MediaLibraryDemoBatchOperation.Delete,
                -> media == null
            }
            if (shouldSkip) {
                summary = summary.recordSkipped()
                dispatchAction(MediaLibraryDemoAction.BatchProgressed(operation, summary))
                continue
            }

            dispatchAction(MediaLibraryDemoAction.ItemOperationStarted(sample.id, operation))
            when (
                val outcome = performOperation(
                    context = context,
                    sampleId = sample.id,
                    operation = operation,
                    media = media,
                )
            ) {
                MediaLibraryDemoOperationOutcome.Success -> summary.recordSucceeded()
                is MediaLibraryDemoOperationOutcome.Unsupported -> summary.recordUnsupported()
                is MediaLibraryDemoOperationOutcome.Failed -> {
                    summary = summary.recordFailed()
                    if (outcome.error.stopsBatch()) {
                        blocker = outcome.error
                    }
                    summary
                }
            }.also { updatedSummary ->
                summary = updatedSummary
                dispatchAction(MediaLibraryDemoAction.BatchProgressed(operation, summary))
            }
            if (blocker != null) break
        }
        dispatchAction(
            MediaLibraryDemoAction.BatchFinished(
                operation = operation,
                summary = summary,
                blocker = blocker,
            ),
        )
    }

    /** 执行一个平台媒体库操作并把结果回写为页面 Action。 */
    private suspend fun performOperation(
        context: Context,
        sampleId: MediaLibraryDemoSampleId,
        operation: MediaLibraryDemoBatchOperation,
        media: PublishedMedia?,
    ): MediaLibraryDemoOperationOutcome {
        val sample = sampleId.sample()
        return try {
            ensurePermission(context, sample.collection)
            when (operation) {
                MediaLibraryDemoBatchOperation.Publish -> {
                    val source = prepareSourceFile(context, sample)
                    val published = mediaLibrary.publish(
                        MediaPublishRequest(
                            source = source,
                            displayName = displayNameProvider(sample),
                            mimeType = sample.mimeType,
                            collection = sample.collection,
                            relativeDirectory = null,
                        ),
                    )
                    activePublishedMedia[sampleId] = published
                    dispatchAction(MediaLibraryDemoAction.Published(sampleId, published))
                }

                MediaLibraryDemoBatchOperation.Check -> {
                    val published = requireNotNull(media) { "检查操作必须提供平台媒体引用" }
                    val exists = mediaLibrary.exists(published)
                    if (!exists) activePublishedMedia.remove(sampleId)
                    dispatchAction(MediaLibraryDemoAction.ExistsLoaded(sampleId, exists))
                }

                MediaLibraryDemoBatchOperation.Delete -> {
                    val published = requireNotNull(media) { "删除操作必须提供平台媒体引用" }
                    mediaLibrary.delete(published)
                    activePublishedMedia.remove(sampleId)
                    dispatchAction(MediaLibraryDemoAction.Deleted(sampleId))
                }
            }
            MediaLibraryDemoOperationOutcome.Success
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MediaLibraryException) {
            val error = exception.error
            dispatchAction(MediaLibraryDemoAction.OperationFailed(sampleId, error))
            if (error is MediaLibraryError.Unsupported) {
                MediaLibraryDemoOperationOutcome.Unsupported(error)
            } else {
                MediaLibraryDemoOperationOutcome.Failed(error)
            }
        } catch (exception: Throwable) {
            val error = MediaLibraryError.Io("系统媒体库演示操作失败", exception)
            dispatchAction(MediaLibraryDemoAction.OperationFailed(sampleId, error))
            MediaLibraryDemoOperationOutcome.Failed(error)
        }
    }

    /** 读取全部内置资源大小，读取失败时给对应测试返回 I/O 错误。 */
    private suspend fun loadSampleSizes() {
        val sizes = mutableMapOf<MediaLibraryDemoSampleId, Long>()
        for (sample in mediaLibraryDemoSamples) {
            try {
                sizes[sample.id] = sourceBytesProvider(sample).size.toLong()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                dispatchAction(
                    MediaLibraryDemoAction.OperationFailed(
                        sampleId = sample.id,
                        error = MediaLibraryError.Io("无法读取内置媒体资源", exception),
                    ),
                )
            }
        }
        dispatchAction(MediaLibraryDemoAction.SampleSizesLoaded(sizes))
    }

    /** 将 Compose 二进制资源写入应用 cache 文件。 */
    private suspend fun prepareSourceFile(
        context: Context,
        sample: MediaLibraryDemoSample,
    ): File {
        val sourceDirectory = context.files.cacheDir.resolve("media-library-demo").apply { mkdirs() }
        val source = sourceDirectory.resolve(sample.sourceFileName)
        val bytes = sourceBytesProvider(sample)
        SystemFileSystem.write(source.toPath()) {
            write(bytes)
        }
        return source
    }

    /** 校验指定 Collection 的平台权限，拒绝时抛出统一权限错误。 */
    private suspend fun ensurePermission(context: Context, collection: MediaCollection) {
        if (!permissionRequester(context, collection)) {
            throw MediaLibraryException(
                MediaLibraryError.PermissionDenied("用户未授予系统媒体库权限"),
            )
        }
    }
}

/** 单次媒体库操作的内部结果。 */
private sealed interface MediaLibraryDemoOperationOutcome {
    /** 操作成功。 */
    data object Success : MediaLibraryDemoOperationOutcome

    /** 平台明确不支持该操作。 */
    data class Unsupported(
        /** 平台返回的 Unsupported 错误。 */
        val error: MediaLibraryError.Unsupported,
    ) : MediaLibraryDemoOperationOutcome

    /** 操作发生非 Unsupported 错误。 */
    data class Failed(
        /** 平台返回的结构化错误。 */
        val error: MediaLibraryError,
    ) : MediaLibraryDemoOperationOutcome
}

/** 返回操作进行中对应的页面阶段。 */
private fun MediaLibraryDemoBatchOperation.runningPhase(): MediaLibraryDemoPhase = when (this) {
    MediaLibraryDemoBatchOperation.Publish -> MediaLibraryDemoPhase.Publishing
    MediaLibraryDemoBatchOperation.Check -> MediaLibraryDemoPhase.Checking
    MediaLibraryDemoBatchOperation.Delete -> MediaLibraryDemoPhase.Deleting
}

/** 权限拒绝和空间不足会阻止同批次后续项目。 */
private fun MediaLibraryError.stopsBatch(): Boolean =
    this is MediaLibraryError.PermissionDenied || this is MediaLibraryError.NoSpace

/** 累计一个成功项目。 */
private fun MediaLibraryDemoBatchSummary.recordSucceeded(): MediaLibraryDemoBatchSummary = copy(
    processed = processed + 1,
    succeeded = succeeded + 1,
)

/** 累计一个平台不支持项目。 */
private fun MediaLibraryDemoBatchSummary.recordUnsupported(): MediaLibraryDemoBatchSummary = copy(
    processed = processed + 1,
    unsupported = unsupported + 1,
)

/** 累计一个失败项目。 */
private fun MediaLibraryDemoBatchSummary.recordFailed(): MediaLibraryDemoBatchSummary = copy(
    processed = processed + 1,
    failed = failed + 1,
)

/** 累计一个跳过项目。 */
private fun MediaLibraryDemoBatchSummary.recordSkipped(): MediaLibraryDemoBatchSummary = copy(
    processed = processed + 1,
    skipped = skipped + 1,
)
