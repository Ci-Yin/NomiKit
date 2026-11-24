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
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Prohibited
import io.github.composefluent.icons.regular.CheckmarkCircle
import io.github.composefluent.icons.regular.FolderAdd
import io.github.composefluent.icons.regular.Delete
import io.github.composefluent.icons.regular.ErrorCircle
import io.github.composefluent.icons.regular.Folder
import io.github.composefluent.icons.regular.Search
import io.github.composefluent.icons.regular.ArrowUpload
import io.github.composefluent.icons.regular.ShieldCheckmark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.divider.NomiDivider
import ciyin.designsystem.component.icon.NomiIcon
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.text.NomiText
import ciyin.io.formatFileSize
import ciyin.material.theme.AppTheme
import ciyin.media.directory.MediaDirectoryAccessToken
import ciyin.ui.foundation.viewmodel.collectSideEffects
import com.ciyin.app.presentation.component.SamplePageScaffold
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
@Composable
internal fun MediaDirectoryDemoContent(
    state: MediaDirectoryDemoUiState,
    onAction: (MediaDirectoryDemoAction) -> Unit,
) {
    SamplePageScaffold(
        title = stringResource(Res.string.media_directory_demo_title),
        onBack = { onAction(MediaDirectoryDemoAction.BackClick) },
        backContentDescription = stringResource(Res.string.media_directory_demo_back),
        modifier = Modifier.testTag(MediaDirectoryDemoRootTag),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
    NomiSurface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(MediaDirectoryDemoStatusTag),
        shape = AppTheme.shapes.small,
        colors = NomiSurfaceColors(containerColor = AppTheme.colorScheme.surface),
        border = NomiBorder.Stroke(
            BorderStroke(
                width = AppTheme.sizes.strokes.thin,
                color = AppTheme.colorScheme.outline,
            ),
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
                NomiIcon(
                    imageVector = state.accessState.icon(),
                    contentDescription = null,
                    modifier = Modifier.size(AppTheme.sizes.icon.large),
                    tint = state.accessState.color(),
                )
                NomiText(
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
                    icon = Icons.Regular.FolderAdd,
                    loading = state.phase == MediaDirectoryDemoPhase.Picking,
                    enabled = state.isIdle,
                    loadingLabel = Res.string.media_directory_demo_picking,
                    defaultLabel = Res.string.media_directory_demo_pick,
                    onClick = { onAction(MediaDirectoryDemoAction.PickDirectoryClick) },
                    modifier = Modifier.weight(1f),
                )
                MediaDirectoryDemoCommandButton(
                    icon = Icons.Regular.ShieldCheckmark,
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
    NomiSurface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(MediaDirectoryDemoDetailTag),
        shape = AppTheme.shapes.small,
        colors = NomiSurfaceColors(containerColor = AppTheme.colorScheme.surface),
        border = NomiBorder.Stroke(
            BorderStroke(
                width = AppTheme.sizes.strokes.thin,
                color = AppTheme.colorScheme.outline,
            ),
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
                NomiIcon(
                    imageVector = Icons.Regular.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(AppTheme.sizes.icon.large),
                    tint = AppTheme.colorScheme.primary,
                )
                NomiText(
                    text = stringResource(Res.string.media_directory_demo_detail_title),
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colorScheme.textPrimary,
                )
            }
            NomiDivider(color = AppTheme.colorScheme.divider)
            MediaDirectoryAccessSection(state = state)
            NomiDivider(color = AppTheme.colorScheme.divider)
            MediaDirectoryEntrySection(
                state = state,
                onCommit = { onAction(MediaDirectoryDemoAction.CommitSampleClick) },
                onExists = { onAction(MediaDirectoryDemoAction.ExistsClick) },
                onDelete = { onAction(MediaDirectoryDemoAction.DeleteClick) },
            )
            state.error?.let { error ->
                NomiDivider(color = AppTheme.colorScheme.divider)
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
        NomiText(
            text = stringResource(Res.string.media_directory_demo_request_section),
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colorScheme.primary,
        )
        val access = state.access
        if (access == null) {
            NomiText(
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
        NomiText(
            text = stringResource(Res.string.media_directory_demo_result_section),
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colorScheme.primary,
        )
        val entry = state.lastEntry
        if (entry == null) {
            NomiText(
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
                    icon = Icons.Regular.ArrowUpload,
                    loading = state.phase == MediaDirectoryDemoPhase.Committing,
                    enabled = state.accessState == MediaDirectoryDemoAccessState.Granted && state.isIdle,
                    loadingLabel = Res.string.media_directory_demo_committing,
                    defaultLabel = Res.string.media_directory_demo_commit,
                    onClick = onCommit,
                    modifier = Modifier.weight(1f),
                )
                MediaDirectoryDemoCommandButton(
                    icon = Icons.Regular.Search,
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
                    icon = Icons.Regular.Delete,
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
        NomiIcon(
            imageVector = if (error.type == MediaDirectoryDemoErrorType.Unsupported) {
                Icons.Regular.Prohibited
            } else {
                Icons.Regular.ErrorCircle
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
            NomiText(
                text = stringResource(Res.string.media_directory_demo_error_title),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colorScheme.textPrimary,
            )
            NomiText(
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
    NomiButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        variant = NomiButtonVariant.Default,
    ) {
        if (!loading) {
            NomiIcon(imageVector = icon, contentDescription = null)
        }
        NomiText(
            text = stringResource(if (loading) loadingLabel else defaultLabel),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(start = AppTheme.spacings.small),
        )
    }
}

/** 主操作按钮：commit 使用主要按钮，其余命令使用次级按钮。 */
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
    NomiButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        if (!loading) {
            NomiIcon(imageVector = icon, contentDescription = null)
        }
        NomiText(
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
        NomiText(
            text = label,
            style = AppTheme.typography.labelMedium,
            color = AppTheme.colorScheme.textSecondary,
        )
        NomiText(
            text = value,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.textPrimary,
        )
    }
}

/** 返回授权状态对应的图标。 */
private fun MediaDirectoryDemoAccessState.icon() = when (this) {
    MediaDirectoryDemoAccessState.NotGranted -> Icons.Regular.Folder
    MediaDirectoryDemoAccessState.Granted -> Icons.Regular.CheckmarkCircle
    MediaDirectoryDemoAccessState.Revoked -> Icons.Regular.ErrorCircle
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
