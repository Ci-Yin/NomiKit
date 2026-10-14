package com.ciyin.app.presentation.screen.mediadirectory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.io.formatFileSize
import ciyin.material.theme.AppTheme
import ciyin.media.directory.MediaDirectoryAccessToken
import ciyin.ui.foundation.viewmodel.collectSideEffects
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.media_directory_demo_back
import com.ciyin.app.sample.media_directory_demo_canonical_path
import com.ciyin.app.sample.media_directory_demo_canonical_path_unavailable
import com.ciyin.app.sample.media_directory_demo_commit
import com.ciyin.app.sample.media_directory_demo_committing
import com.ciyin.app.sample.media_directory_demo_delete
import com.ciyin.app.sample.media_directory_demo_deleting
import com.ciyin.app.sample.media_directory_demo_detail_title
import com.ciyin.app.sample.media_directory_demo_display_name
import com.ciyin.app.sample.media_directory_demo_error_access_revoked
import com.ciyin.app.sample.media_directory_demo_error_already_exists
import com.ciyin.app.sample.media_directory_demo_error_cancelled
import com.ciyin.app.sample.media_directory_demo_error_io
import com.ciyin.app.sample.media_directory_demo_error_no_space
import com.ciyin.app.sample.media_directory_demo_error_not_found
import com.ciyin.app.sample.media_directory_demo_error_permission_denied
import com.ciyin.app.sample.media_directory_demo_error_title
import com.ciyin.app.sample.media_directory_demo_error_unsupported
import com.ciyin.app.sample.media_directory_demo_exists
import com.ciyin.app.sample.media_directory_demo_exists_no
import com.ciyin.app.sample.media_directory_demo_exists_yes
import com.ciyin.app.sample.media_directory_demo_file_name
import com.ciyin.app.sample.media_directory_demo_file_size
import com.ciyin.app.sample.media_directory_demo_granted_status
import com.ciyin.app.sample.media_directory_demo_location
import com.ciyin.app.sample.media_directory_demo_mime
import com.ciyin.app.sample.media_directory_demo_not_granted_status
import com.ciyin.app.sample.media_directory_demo_pick
import com.ciyin.app.sample.media_directory_demo_picking
import com.ciyin.app.sample.media_directory_demo_request_section
import com.ciyin.app.sample.media_directory_demo_result_empty
import com.ciyin.app.sample.media_directory_demo_result_section
import com.ciyin.app.sample.media_directory_demo_revoke_status
import com.ciyin.app.sample.media_directory_demo_title
import com.ciyin.app.sample.media_directory_demo_token
import com.ciyin.app.sample.media_directory_demo_validate
import com.ciyin.app.sample.media_directory_demo_validating
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/** 空闲状态预览。 */
@AppPreview
@Composable
private fun MediaDirectoryDemoIdlePreview() {
    AppTheme {
        MediaDirectoryDemoContent(
            state = previewIdleState(),
            onAction = {},
        )
    }
}

/** 已授权并成功提交条目后的预览。 */
@AppPreview
@Composable
private fun MediaDirectoryDemoGrantedPreview() {
    AppTheme {
        MediaDirectoryDemoContent(
            state = previewGrantedState(),
            onAction = {},
        )
    }
}

/** 错误状态预览。 */
@AppPreview
@Composable
private fun MediaDirectoryDemoFailedPreview() {
    AppTheme {
        MediaDirectoryDemoContent(
            state = previewFailedState(),
            onAction = {},
        )
    }
}

/** 创建空闲预览状态。 */
private fun previewIdleState(): MediaDirectoryDemoUiState = MediaDirectoryDemoUiState()

/** 创建已授权并完成一次提交后的预览状态。 */
private fun previewGrantedState(): MediaDirectoryDemoUiState = MediaDirectoryDemoUiState(
    accessState = MediaDirectoryDemoAccessState.Granted,
    access = MediaDirectoryDemoAccessModel(
        token = MediaDirectoryAccessTokenPreview,
        displayName = "Pictures",
        canonicalPath = "/tmp/nomikit-demo/Pictures",
    ),
    lastEntry = MediaDirectoryDemoEntryModel(
        fileName = "demo.txt",
        location = "/tmp/nomikit-demo/Pictures/demo.txt",
        mimeType = "text/plain",
        size = 48L,
        exists = true,
    ),
)

/** 创建带有错误信息的预览状态。 */
private fun previewFailedState(): MediaDirectoryDemoUiState = MediaDirectoryDemoUiState(
    accessState = MediaDirectoryDemoAccessState.Revoked,
    access = MediaDirectoryDemoAccessModel(
        token = MediaDirectoryAccessTokenPreview,
        displayName = "Documents",
        canonicalPath = null,
    ),
    lastEntry = null,
    error = MediaDirectoryDemoErrorModel(MediaDirectoryDemoErrorType.AccessRevoked),
)

/** 预览占位 token，避免硬编码在多处。 */
private val MediaDirectoryAccessTokenPreview = MediaDirectoryAccessToken(
    "content://com.android.externalstorage.documents/tree/primary%3ADocuments",
)

/** 用户媒体目录示例根节点语义标签。 */
internal const val MediaDirectoryDemoRootTag = "media-directory-demo-root"

/** 用户媒体目录示例状态卡片语义标签。 */
internal const val MediaDirectoryDemoStatusTag = "media-directory-demo-status"

/** 用户媒体目录示例详情卡片语义标签。 */
internal const val MediaDirectoryDemoDetailTag = "media-directory-demo-detail"

/**
 * 用户媒体目录测试台页面入口。
 *
 * Android 端会通过 [rememberMediaDirectoryAccessFactory] 准备 SAF 目录选择桥并注入
 * ViewModel；其他平台在 factory 为 null 时保持原样，ViewModel 走 `session.requestAccess()`。
 *
 * @param onBack 返回 sample 首页的回调。
 */
@Composable
internal fun MediaDirectoryDemoScreen(
    onBack: () -> Unit,
) {
    val accessFactory = rememberMediaDirectoryAccessFactory()
    val viewModel = remember {
        MediaDirectoryDemoViewModel(accessFactoryOverride = accessFactory)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.collectSideEffects { effect ->
        when (effect) {
            MediaDirectoryDemoEffect.NavigateBack -> onBack()
        }
    }
    MediaDirectoryDemoContent(
        state = state,
        onAction = viewModel.dispatchAction,
    )
}

/**
 * 用户媒体目录测试台纯 UI。
 *
 * @param state 当前测试台状态。
 * @param onAction 页面操作回调。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MediaDirectoryDemoContent(
    state: MediaDirectoryDemoUiState,
    onAction: (MediaDirectoryDemoAction) -> Unit,
) {
    Scaffold(
        modifier = Modifier.testTag(MediaDirectoryDemoRootTag),
        containerColor = AppTheme.colorScheme.surfaceLower,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.media_directory_demo_title),
                        color = AppTheme.colorScheme.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(MediaDirectoryDemoAction.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.media_directory_demo_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colorScheme.surfaceLower,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val maxWidth = AppTheme.sizes.layoutConstraints.dialogMaxWidth +
                    AppTheme.sizes.layoutConstraints.dialogMaxWidth +
                    AppTheme.spacings.huge
            Column(
                modifier = Modifier
                    .widthIn(max = maxWidth)
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppTheme.spacings.large,
                        vertical = AppTheme.spacings.medium,
                    ),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
            ) {
                MediaDirectoryStatusCard(state = state, onAction = onAction)
                MediaDirectoryDetailCard(state = state, onAction = onAction)
            }
        }
    }
}

/** 显示当前授权状态、阶段和基础命令。 */
@Composable
private fun MediaDirectoryStatusCard(
    state: MediaDirectoryDemoUiState,
    onAction: (MediaDirectoryDemoAction) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(MediaDirectoryDemoStatusTag),
        shape = AppTheme.shapes.small,
        color = AppTheme.colorScheme.surface,
        border = BorderStroke(
            width = AppTheme.sizes.strokes.thin,
            color = AppTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacings.large),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.medium),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = state.accessState.icon(),
                    contentDescription = null,
                    modifier = Modifier.size(AppTheme.sizes.icon.large),
                    tint = state.accessState.color(),
                )
                Text(
                    text = stringResource(state.accessState.resource()),
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colorScheme.textPrimary,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
                modifier = Modifier.fillMaxWidth(),
            ) {
                MediaDirectoryDemoCommandButton(
                    icon = Icons.Filled.CreateNewFolder,
                    loading = state.phase == MediaDirectoryDemoPhase.Picking,
                    enabled = state.isIdle,
                    loadingLabel = Res.string.media_directory_demo_picking,
                    defaultLabel = Res.string.media_directory_demo_pick,
                    onClick = { onAction(MediaDirectoryDemoAction.PickDirectoryClick) },
                    modifier = Modifier.weight(1f),
                )
                MediaDirectoryDemoCommandButton(
                    icon = Icons.Filled.VerifiedUser,
                    loading = state.phase == MediaDirectoryDemoPhase.Validating,
                    enabled = state.accessState == MediaDirectoryDemoAccessState.Granted && state.isIdle,
                    loadingLabel = Res.string.media_directory_demo_validating,
                    defaultLabel = Res.string.media_directory_demo_validate,
                    onClick = { onAction(MediaDirectoryDemoAction.ValidateClick) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** 显示当前授权信息、最近条目和操作按钮。 */
@Composable
private fun MediaDirectoryDetailCard(
    state: MediaDirectoryDemoUiState,
    onAction: (MediaDirectoryDemoAction) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(MediaDirectoryDemoDetailTag),
        shape = AppTheme.shapes.small,
        color = AppTheme.colorScheme.surface,
        border = BorderStroke(
            width = AppTheme.sizes.strokes.thin,
            color = AppTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacings.large),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(AppTheme.sizes.icon.large),
                    tint = AppTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(Res.string.media_directory_demo_detail_title),
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colorScheme.textPrimary,
                )
            }
            HorizontalDivider(color = AppTheme.colorScheme.divider)
            MediaDirectoryAccessSection(state = state)
            HorizontalDivider(color = AppTheme.colorScheme.divider)
            MediaDirectoryEntrySection(
                state = state,
                onCommit = { onAction(MediaDirectoryDemoAction.CommitSampleClick) },
                onExists = { onAction(MediaDirectoryDemoAction.ExistsClick) },
                onDelete = { onAction(MediaDirectoryDemoAction.DeleteClick) },
            )
            state.error?.let { error ->
                HorizontalDivider(color = AppTheme.colorScheme.divider)
                MediaDirectoryErrorSection(error)
            }
        }
    }
}

/** 显示当前授权目录的展示信息和 token。 */
@Composable
private fun MediaDirectoryAccessSection(state: MediaDirectoryDemoUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.medium),
    ) {
        Text(
            text = stringResource(Res.string.media_directory_demo_request_section),
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colorScheme.primary,
        )
        val access = state.access
        if (access == null) {
            Text(
                text = stringResource(Res.string.media_directory_demo_not_granted_status),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.textSecondary,
            )
        } else {
            SelectionContainer {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.medium),
                ) {
                    InfoRow(
                        label = stringResource(Res.string.media_directory_demo_display_name),
                        value = access.displayName,
                    )
                    InfoRow(
                        label = stringResource(Res.string.media_directory_demo_canonical_path),
                        value = access.canonicalPath
                            ?: stringResource(Res.string.media_directory_demo_canonical_path_unavailable),
                    )
                    InfoRow(
                        label = stringResource(Res.string.media_directory_demo_token),
                        value = access.token.value,
                    )
                }
            }
        }
    }
}

/** 显示最近一次提交得到的条目与操作按钮。 */
@Composable
private fun MediaDirectoryEntrySection(
    state: MediaDirectoryDemoUiState,
    onCommit: () -> Unit,
    onExists: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.medium),
    ) {
        Text(
            text = stringResource(Res.string.media_directory_demo_result_section),
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colorScheme.primary,
        )
        val entry = state.lastEntry
        if (entry == null) {
            Text(
                text = stringResource(Res.string.media_directory_demo_result_empty),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.textSecondary,
            )
        } else {
            SelectionContainer {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.medium),
                ) {
                    InfoRow(
                        label = stringResource(Res.string.media_directory_demo_file_name),
                        value = entry.fileName,
                    )
                    InfoRow(
                        label = stringResource(Res.string.media_directory_demo_location),
                        value = entry.location,
                    )
                    InfoRow(
                        label = stringResource(Res.string.media_directory_demo_mime),
                        value = entry.mimeType,
                    )
                    InfoRow(
                        label = stringResource(Res.string.media_directory_demo_file_size),
                        value = entry.size.formatFileSize(),
                    )
                    InfoRow(
                        label = stringResource(Res.string.media_directory_demo_exists),
                        value = stringResource(
                            when (entry.exists) {
                                true -> Res.string.media_directory_demo_exists_yes
                                false -> Res.string.media_directory_demo_exists_no
                            },
                        ),
                    )
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                MediaDirectoryDemoPrimaryButton(
                    icon = Icons.Filled.UploadFile,
                    loading = state.phase == MediaDirectoryDemoPhase.Committing,
                    enabled = state.accessState == MediaDirectoryDemoAccessState.Granted && state.isIdle,
                    loadingLabel = Res.string.media_directory_demo_committing,
                    defaultLabel = Res.string.media_directory_demo_commit,
                    onClick = onCommit,
                    modifier = Modifier.weight(1f),
                )
                MediaDirectoryDemoCommandButton(
                    icon = Icons.Filled.Search,
                    loading = false,
                    enabled = state.hasEntry && state.isIdle,
                    loadingLabel = Res.string.media_directory_demo_exists,
                    defaultLabel = Res.string.media_directory_demo_exists,
                    onClick = onExists,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                MediaDirectoryDemoCommandButton(
                    icon = Icons.Filled.Delete,
                    loading = state.phase == MediaDirectoryDemoPhase.Deleting,
                    enabled = state.hasEntry && state.isIdle,
                    loadingLabel = Res.string.media_directory_demo_deleting,
                    defaultLabel = Res.string.media_directory_demo_delete,
                    onClick = onDelete,
                )
            }
        }
    }
}

/** 显示最近一次结构化错误。 */
@Composable
private fun MediaDirectoryErrorSection(error: MediaDirectoryDemoErrorModel) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = if (error.type == MediaDirectoryDemoErrorType.Unsupported) {
                Icons.Filled.Block
            } else {
                Icons.Filled.Error
            },
            contentDescription = null,
            modifier = Modifier.size(AppTheme.sizes.icon.large),
            tint = if (error.type == MediaDirectoryDemoErrorType.Unsupported) {
                AppTheme.colorScheme.info
            } else {
                AppTheme.colorScheme.error
            },
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.tiny),
        ) {
            Text(
                text = stringResource(Res.string.media_directory_demo_error_title),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colorScheme.textPrimary,
            )
            Text(
                text = stringResource(error.type.resource()),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.textSecondary,
            )
        }
    }
}

/** 通用 Outlined 按钮：loading 时显示进度，否则显示图标 + 文案。 */
@Composable
private fun MediaDirectoryDemoCommandButton(
    icon: ImageVector,
    loading: Boolean,
    enabled: Boolean,
    loadingLabel: StringResource,
    defaultLabel: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(AppTheme.sizes.icon.medium),
                strokeWidth = AppTheme.sizes.strokes.medium,
            )
        } else {
            Icon(imageVector = icon, contentDescription = null)
        }
        Text(
            text = stringResource(if (loading) loadingLabel else defaultLabel),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(start = AppTheme.spacings.small),
        )
    }
}

/** 主操作按钮：commit 时使用 Button 而不是 OutlinedButton。 */
@Composable
private fun MediaDirectoryDemoPrimaryButton(
    icon: ImageVector,
    loading: Boolean,
    enabled: Boolean,
    loadingLabel: StringResource,
    defaultLabel: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(AppTheme.sizes.icon.medium),
                strokeWidth = AppTheme.sizes.strokes.medium,
            )
        } else {
            Icon(imageVector = icon, contentDescription = null)
        }
        Text(
            text = stringResource(if (loading) loadingLabel else defaultLabel),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(start = AppTheme.spacings.small),
        )
    }
}

/** 显示一个可自动换行的名称和值。 */
@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.tiny)) {
        Text(
            text = label,
            style = AppTheme.typography.labelMedium,
            color = AppTheme.colorScheme.textSecondary,
        )
        Text(
            text = value,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.textPrimary,
        )
    }
}

/** 返回授权状态对应的图标。 */
private fun MediaDirectoryDemoAccessState.icon() = when (this) {
    MediaDirectoryDemoAccessState.NotGranted -> Icons.Filled.Folder
    MediaDirectoryDemoAccessState.Granted -> Icons.Filled.CheckCircle
    MediaDirectoryDemoAccessState.Revoked -> Icons.Filled.Error
}

/** 返回授权状态对应的语义颜色。 */
@Composable
private fun MediaDirectoryDemoAccessState.color(): Color = when (this) {
    MediaDirectoryDemoAccessState.NotGranted -> AppTheme.colorScheme.textSecondary
    MediaDirectoryDemoAccessState.Granted -> AppTheme.colorScheme.success
    MediaDirectoryDemoAccessState.Revoked -> AppTheme.colorScheme.warning
}

/** 返回授权状态对应的标题资源。 */
private fun MediaDirectoryDemoAccessState.resource(): StringResource = when (this) {
    MediaDirectoryDemoAccessState.NotGranted -> Res.string.media_directory_demo_not_granted_status
    MediaDirectoryDemoAccessState.Granted -> Res.string.media_directory_demo_granted_status
    MediaDirectoryDemoAccessState.Revoked -> Res.string.media_directory_demo_revoke_status
}

/** 返回错误分类对应的字符串资源。 */
private fun MediaDirectoryDemoErrorType.resource(): StringResource = when (this) {
    MediaDirectoryDemoErrorType.Cancelled -> Res.string.media_directory_demo_error_cancelled
    MediaDirectoryDemoErrorType.Unsupported -> Res.string.media_directory_demo_error_unsupported
    MediaDirectoryDemoErrorType.PermissionDenied -> Res.string.media_directory_demo_error_permission_denied
    MediaDirectoryDemoErrorType.AccessRevoked -> Res.string.media_directory_demo_error_access_revoked
    MediaDirectoryDemoErrorType.AlreadyExists -> Res.string.media_directory_demo_error_already_exists
    MediaDirectoryDemoErrorType.NoSpace -> Res.string.media_directory_demo_error_no_space
    MediaDirectoryDemoErrorType.NotFound -> Res.string.media_directory_demo_error_not_found
    MediaDirectoryDemoErrorType.Io -> Res.string.media_directory_demo_error_io
}
