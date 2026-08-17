package com.ciyin.app.ui.screen.platformshare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.material.theme.AppTheme
import ciyin.platform.LocalContext
import ciyin.platform.share.PlatformShareFailureReason
import ciyin.platform.share.PlatformShareResult
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.platform_share_demo_back
import com.ciyin.app.sample.platform_share_demo_description
import com.ciyin.app.sample.platform_share_demo_failure_file_unavailable
import com.ciyin.app.sample.platform_share_demo_failure_invalid_payload
import com.ciyin.app.sample.platform_share_demo_failure_invalid_uri
import com.ciyin.app.sample.platform_share_demo_failure_launch_failed
import com.ciyin.app.sample.platform_share_demo_failure_permission_denied
import com.ciyin.app.sample.platform_share_demo_failure_presenter_unavailable
import com.ciyin.app.sample.platform_share_demo_failure_uri_unavailable
import com.ciyin.app.sample.platform_share_demo_file_action
import com.ciyin.app.sample.platform_share_demo_file_description
import com.ciyin.app.sample.platform_share_demo_file_title
import com.ciyin.app.sample.platform_share_demo_files_action
import com.ciyin.app.sample.platform_share_demo_files_description
import com.ciyin.app.sample.platform_share_demo_files_title
import com.ciyin.app.sample.platform_share_demo_multiple_file_first_content
import com.ciyin.app.sample.platform_share_demo_multiple_file_second_content
import com.ciyin.app.sample.platform_share_demo_operation_files
import com.ciyin.app.sample.platform_share_demo_operation_single_file
import com.ciyin.app.sample.platform_share_demo_operation_text
import com.ciyin.app.sample.platform_share_demo_progress
import com.ciyin.app.sample.platform_share_demo_result_failed
import com.ciyin.app.sample.platform_share_demo_result_failed_detail
import com.ciyin.app.sample.platform_share_demo_result_idle
import com.ciyin.app.sample.platform_share_demo_result_opened
import com.ciyin.app.sample.platform_share_demo_result_title
import com.ciyin.app.sample.platform_share_demo_result_unsupported
import com.ciyin.app.sample.platform_share_demo_result_with_operation
import com.ciyin.app.sample.platform_share_demo_single_file_content
import com.ciyin.app.sample.platform_share_demo_text_action
import com.ciyin.app.sample.platform_share_demo_text_content
import com.ciyin.app.sample.platform_share_demo_text_description
import com.ciyin.app.sample.platform_share_demo_text_title
import com.ciyin.app.sample.platform_share_demo_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 系统分享示例页面。
 *
 * @param onBackRequest 返回上一页回调。
 * @param viewModel 页面 ViewModel。
 */
@Composable
internal fun PlatformShareDemoScreen(
    onBackRequest: () -> Unit,
    viewModel: PlatformShareDemoViewModel = viewModel(::PlatformShareDemoViewModel),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.collectSideEffects { effect ->
        when (effect) {
            PlatformShareDemoEffect.NavigateBack -> onBackRequest()
        }
    }

    PlatformShareDemoContent(
        state = state,
        onAction = viewModel.dispatchAction,
    )
}

/**
 * 系统分享示例页面内容。
 *
 * @param state 页面状态。
 * @param onAction 页面动作回调。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlatformShareDemoContent(
    state: PlatformShareDemoUiState,
    onAction: (PlatformShareDemoAction) -> Unit,
) {
    val context = LocalContext.current
    val textTitle = stringResource(Res.string.platform_share_demo_text_title)
    val singleFileTitle = stringResource(Res.string.platform_share_demo_file_title)
    val multipleFilesTitle = stringResource(Res.string.platform_share_demo_files_title)
    val textContent = stringResource(Res.string.platform_share_demo_text_content)
    val singleFileContent = stringResource(Res.string.platform_share_demo_single_file_content)
    val firstFileContent = stringResource(Res.string.platform_share_demo_multiple_file_first_content)
    val secondFileContent = stringResource(Res.string.platform_share_demo_multiple_file_second_content)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.platform_share_demo_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(PlatformShareDemoAction.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.platform_share_demo_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(AppTheme.spacings.large),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.platform_share_demo_description),
                    color = AppTheme.colorScheme.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            item {
                ShareOperationSection(
                    icon = Icons.Filled.Share,
                    title = textTitle,
                    description = stringResource(Res.string.platform_share_demo_text_description),
                    actionLabel = stringResource(Res.string.platform_share_demo_text_action),
                    isActive = state.activeOperation == PlatformShareDemoOperation.Text,
                    enabled = !state.isBusy,
                    onClick = {
                        onAction(
                            PlatformShareDemoAction.ShareText(
                                context = context,
                                title = textTitle,
                                content = textContent,
                            ),
                        )
                    },
                )
            }

            item {
                ShareOperationSection(
                    icon = Icons.Filled.Description,
                    title = singleFileTitle,
                    description = stringResource(Res.string.platform_share_demo_file_description),
                    actionLabel = stringResource(Res.string.platform_share_demo_file_action),
                    isActive = state.activeOperation == PlatformShareDemoOperation.SingleFile,
                    enabled = !state.isBusy,
                    onClick = {
                        onAction(
                            PlatformShareDemoAction.ShareSingleFile(
                                context = context,
                                title = singleFileTitle,
                                content = singleFileContent,
                            ),
                        )
                    },
                )
            }

            item {
                ShareOperationSection(
                    icon = Icons.Filled.Folder,
                    title = multipleFilesTitle,
                    description = stringResource(Res.string.platform_share_demo_files_description),
                    actionLabel = stringResource(Res.string.platform_share_demo_files_action),
                    isActive = state.activeOperation == PlatformShareDemoOperation.MultipleFiles,
                    enabled = !state.isBusy,
                    onClick = {
                        onAction(
                            PlatformShareDemoAction.ShareMultipleFiles(
                                context = context,
                                title = multipleFilesTitle,
                                firstContent = firstFileContent,
                                secondContent = secondFileContent,
                            ),
                        )
                    },
                )
            }

            if (state.isBusy) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = stringResource(Res.string.platform_share_demo_progress),
                            color = AppTheme.colorScheme.textSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            item {
                HorizontalDivider()
            }

            item {
                ShareResultSection(state = state)
            }
        }
    }
}

/**
 * 单个分享操作区域。
 *
 * @param icon 操作图标。
 * @param title 操作标题。
 * @param description 操作说明。
 * @param actionLabel 按钮文案。
 * @param isActive 当前操作是否正在执行。
 * @param enabled 操作是否可用。
 * @param onClick 点击回调。
 */
@Composable
private fun ShareOperationSection(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String,
    isActive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.medium),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppTheme.sizes.icon.medium),
                tint = AppTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(AppTheme.spacings.medium))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.extraSmall),
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = description,
                    color = AppTheme.colorScheme.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        ) {
            if (isActive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(AppTheme.sizes.icon.small),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = AppTheme.sizes.strokes.medium,
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(AppTheme.sizes.icon.small),
                )
            }
            Spacer(modifier = Modifier.width(AppTheme.spacings.small))
            Text(actionLabel)
        }
    }
}

/**
 * 最近一次分享结果区域。
 *
 * @param state 页面状态。
 */
@Composable
private fun ShareResultSection(state: PlatformShareDemoUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
    ) {
        Text(
            text = stringResource(Res.string.platform_share_demo_result_title),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = state.resultText(),
            color = state.resultColor(),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * 将页面状态转换为本地化结果文案。
 *
 * @return 本地化结果文案。
 */
@Composable
private fun PlatformShareDemoUiState.resultText(): String {
    val status = when {
        failureReason != null -> {
            val reasonText = stringResource(failureReason.titleResource())
            failureMessage?.takeIf { it.isNotBlank() }?.let { detail ->
                stringResource(
                    Res.string.platform_share_demo_result_failed_detail,
                    reasonText,
                    detail,
                )
            } ?: stringResource(Res.string.platform_share_demo_result_failed, reasonText)
        }

        result == PlatformShareResult.Opened ->
            stringResource(Res.string.platform_share_demo_result_opened)

        result == PlatformShareResult.Unsupported ->
            stringResource(Res.string.platform_share_demo_result_unsupported)

        else -> stringResource(Res.string.platform_share_demo_result_idle)
    }
    val operation = lastOperation?.let { stringResource(it.titleResource()) }
    return operation?.let {
        stringResource(
            Res.string.platform_share_demo_result_with_operation,
            it,
            status,
        )
    } ?: status
}

/**
 * 获取页面状态对应的结果颜色。
 *
 * @return 结果文字颜色。
 */
@Composable
private fun PlatformShareDemoUiState.resultColor(): Color = when {
    failureReason != null -> AppTheme.colorScheme.error
    result == PlatformShareResult.Opened -> AppTheme.colorScheme.success
    result == PlatformShareResult.Unsupported -> AppTheme.colorScheme.warning
    else -> AppTheme.colorScheme.textSecondary
}

/**
 * 获取分享操作类型的本地化资源。
 *
 * @return 操作类型字符串资源。
 */
private fun PlatformShareDemoOperation.titleResource(): StringResource = when (this) {
    PlatformShareDemoOperation.Text -> Res.string.platform_share_demo_operation_text
    PlatformShareDemoOperation.SingleFile -> Res.string.platform_share_demo_operation_single_file
    PlatformShareDemoOperation.MultipleFiles -> Res.string.platform_share_demo_operation_files
}

/**
 * 获取技术失败原因的本地化资源。
 *
 * @return 失败原因字符串资源。
 */
private fun PlatformShareFailureReason.titleResource(): StringResource = when (this) {
    PlatformShareFailureReason.InvalidPayload -> Res.string.platform_share_demo_failure_invalid_payload
    PlatformShareFailureReason.InvalidUri -> Res.string.platform_share_demo_failure_invalid_uri
    PlatformShareFailureReason.FileUnavailable -> Res.string.platform_share_demo_failure_file_unavailable
    PlatformShareFailureReason.UriUnavailable -> Res.string.platform_share_demo_failure_uri_unavailable
    PlatformShareFailureReason.PermissionDenied -> Res.string.platform_share_demo_failure_permission_denied
    PlatformShareFailureReason.PresenterUnavailable -> Res.string.platform_share_demo_failure_presenter_unavailable
    PlatformShareFailureReason.LaunchFailed -> Res.string.platform_share_demo_failure_launch_failed
}

/**
 * 系统分享示例页预览。
 */
@AppPreview
@Composable
private fun PlatformShareDemoScreenPreview() {
    AppTheme {
        PlatformShareDemoContent(
            state = PlatformShareDemoUiState(
                lastOperation = PlatformShareDemoOperation.MultipleFiles,
                result = PlatformShareResult.Unsupported,
            ),
            onAction = {},
        )
    }
}
