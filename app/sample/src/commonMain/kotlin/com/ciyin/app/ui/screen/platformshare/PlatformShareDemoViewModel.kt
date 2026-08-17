package com.ciyin.app.ui.screen.platformshare

import ciyin.io.File
import ciyin.io.resolve
import ciyin.io.write
import ciyin.platform.Context
import ciyin.platform.files
import ciyin.platform.share.PlatformShareException
import ciyin.platform.share.PlatformShareFailureReason
import ciyin.platform.share.PlatformShareFile
import ciyin.platform.share.PlatformShareFileSource
import ciyin.platform.share.PlatformSharePayload
import ciyin.platform.share.sharePlatformContent
import ciyin.ui.foundation.viewmodel.StateMachineMviViewModel
import com.freeletics.flowredux2.FlowReduxBuilder
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

/**
 * 系统分享示例页 ViewModel。
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class PlatformShareDemoViewModel :
    StateMachineMviViewModel<
        PlatformShareDemoUiState,
        PlatformShareDemoAction,
        PlatformShareDemoEffect,
        >() {
    /**
     * 初始化页面状态。
     */
    override fun FlowReduxStateMachineFactory<PlatformShareDemoUiState, PlatformShareDemoAction>.initialize() {
        initializeWith { PlatformShareDemoUiState() }
    }

    /**
     * 声明页面动作与状态变更。
     */
    override fun FlowReduxBuilder<PlatformShareDemoUiState, PlatformShareDemoAction>.spec() {
        inState<PlatformShareDemoUiState> {
            onActionEffect<PlatformShareDemoAction.BackClick> {
                poseEffect(PlatformShareDemoEffect.NavigateBack)
            }

            on<PlatformShareDemoAction.ShareText> { action ->
                if (snapshot.isBusy) {
                    noChange()
                } else {
                    val changedState = mutate { beginOperation(PlatformShareDemoOperation.Text) }
                    startShare(
                        context = action.context,
                        operation = PlatformShareDemoOperation.Text,
                        title = action.title,
                        contents = listOf(action.content),
                    )
                    changedState
                }
            }

            on<PlatformShareDemoAction.ShareSingleFile> { action ->
                if (snapshot.isBusy) {
                    noChange()
                } else {
                    val changedState = mutate { beginOperation(PlatformShareDemoOperation.SingleFile) }
                    startShare(
                        context = action.context,
                        operation = PlatformShareDemoOperation.SingleFile,
                        title = action.title,
                        contents = listOf(action.content),
                    )
                    changedState
                }
            }

            on<PlatformShareDemoAction.ShareMultipleFiles> { action ->
                if (snapshot.isBusy) {
                    noChange()
                } else {
                    val changedState = mutate { beginOperation(PlatformShareDemoOperation.MultipleFiles) }
                    startShare(
                        context = action.context,
                        operation = PlatformShareDemoOperation.MultipleFiles,
                        title = action.title,
                        contents = listOf(action.firstContent, action.secondContent),
                    )
                    changedState
                }
            }

            on<PlatformShareDemoAction.ShareCompleted> { action ->
                mutate {
                    copy(
                        activeOperation = null,
                        lastOperation = action.operation,
                        result = action.result,
                        failureReason = null,
                        failureMessage = null,
                    )
                }
            }

            on<PlatformShareDemoAction.ShareFailed> { action ->
                mutate {
                    copy(
                        activeOperation = null,
                        lastOperation = action.operation,
                        result = null,
                        failureReason = action.reason,
                        failureMessage = action.message,
                    )
                }
            }
        }
    }

    /**
     * 生成开始分享后的页面状态。
     *
     * @param operation 分享操作类型。
     * @return 已清理上次结果的新状态。
     */
    private fun PlatformShareDemoUiState.beginOperation(
        operation: PlatformShareDemoOperation,
    ): PlatformShareDemoUiState = copy(
        activeOperation = operation,
        lastOperation = null,
        result = null,
        failureReason = null,
        failureMessage = null,
    )

    /**
     * 在后台作用域准备载荷并调用系统分享入口。
     *
     * @param context 平台上下文。
     * @param operation 分享操作类型。
     * @param title 分享内容标题。
     * @param contents 示例内容。
     */
    private fun startShare(
        context: Context,
        operation: PlatformShareDemoOperation,
        title: String,
        contents: List<String>,
    ) {
        backgroundScope.launch {
            val payload = try {
                createPayload(
                    context = context,
                    operation = operation,
                    title = title,
                    contents = contents,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                dispatchAction(
                    PlatformShareDemoAction.ShareFailed(
                        operation = operation,
                        reason = if (operation == PlatformShareDemoOperation.Text) {
                            PlatformShareFailureReason.InvalidPayload
                        } else {
                            PlatformShareFailureReason.FileUnavailable
                        },
                        message = error.technicalMessage(),
                    ),
                )
                return@launch
            }

            try {
                val result = sharePlatformContent(
                    context = context,
                    payload = payload,
                )
                dispatchAction(
                    PlatformShareDemoAction.ShareCompleted(
                        operation = operation,
                        result = result,
                    ),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: PlatformShareException) {
                dispatchAction(
                    PlatformShareDemoAction.ShareFailed(
                        operation = operation,
                        reason = error.reason,
                        message = error.technicalMessage(),
                    ),
                )
            } catch (error: Throwable) {
                dispatchAction(
                    PlatformShareDemoAction.ShareFailed(
                        operation = operation,
                        reason = PlatformShareFailureReason.LaunchFailed,
                        message = error.technicalMessage(),
                    ),
                )
            }
        }
    }

    /**
     * 创建与操作类型对应的系统分享载荷。
     *
     * @param context 平台上下文。
     * @param operation 分享操作类型。
     * @param title 分享内容标题。
     * @param contents 示例内容。
     * @return 系统分享载荷。
     */
    private fun createPayload(
        context: Context,
        operation: PlatformShareDemoOperation,
        title: String,
        contents: List<String>,
    ): PlatformSharePayload = when (operation) {
        PlatformShareDemoOperation.Text -> PlatformSharePayload.Text(
            value = contents.single(),
            title = title,
        )
        PlatformShareDemoOperation.SingleFile -> PlatformSharePayload.File(
            value = createShareFile(
                context = context,
                fileName = SINGLE_FILE_NAME,
                content = contents.single(),
            ),
            title = title,
        )

        PlatformShareDemoOperation.MultipleFiles -> PlatformSharePayload.Files(
            values = listOf(
                createShareFile(
                    context = context,
                    fileName = MULTIPLE_FILE_FIRST_NAME,
                    content = contents.first(),
                ),
                createShareFile(
                    context = context,
                    fileName = MULTIPLE_FILE_SECOND_NAME,
                    content = contents.last(),
                ),
            ),
            title = title,
        )
    }

    /**
     * 在应用缓存目录创建示例文件。
     *
     * @param context 平台上下文。
     * @param fileName 文件名。
     * @param content 文件内容。
     * @return 可分享文件描述。
     */
    private fun createShareFile(
        context: Context,
        fileName: String,
        content: String,
    ): PlatformShareFile {
        val file: File = context.files.cacheDir
            .resolve(DEMO_DIRECTORY_NAME)
            .resolve(fileName)
        file.write(content)
        return PlatformShareFile(
            source = PlatformShareFileSource.LocalFile(file),
            mimeType = TEXT_MIME_TYPE,
            displayName = file.name,
        )
    }

    /**
     * 生成用于示例状态展示的技术错误详情。
     *
     * @return 错误详情，无法取得时为空字符串。
     */
    private fun Throwable.technicalMessage(): String =
        message?.takeIf { it.isNotBlank() } ?: this::class.simpleName.orEmpty()

    private companion object {
        /** 示例文件缓存目录名。 */
        const val DEMO_DIRECTORY_NAME = "platform-share-demo"

        /** 单文件示例文件名。 */
        const val SINGLE_FILE_NAME = "nomikit-share-single.txt"

        /** 多文件示例的第一个文件名。 */
        const val MULTIPLE_FILE_FIRST_NAME = "nomikit-share-first.txt"

        /** 多文件示例的第二个文件名。 */
        const val MULTIPLE_FILE_SECOND_NAME = "nomikit-share-second.txt"

        /** 示例文件 MIME 类型。 */
        const val TEXT_MIME_TYPE = "text/plain"
    }
}
