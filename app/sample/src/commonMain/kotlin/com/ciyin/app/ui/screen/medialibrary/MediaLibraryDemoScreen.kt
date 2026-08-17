package com.ciyin.app.ui.screen.medialibrary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.io.formatFileSize
import ciyin.material.theme.AppTheme
import ciyin.media.library.MediaCollection
import ciyin.platform.LocalContext
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.media_library_demo_back
import com.ciyin.app.sample.media_library_demo_batch_check
import com.ciyin.app.sample.media_library_demo_batch_completed
import com.ciyin.app.sample.media_library_demo_batch_delete
import com.ciyin.app.sample.media_library_demo_batch_idle
import com.ciyin.app.sample.media_library_demo_batch_publish
import com.ciyin.app.sample.media_library_demo_batch_running
import com.ciyin.app.sample.media_library_demo_batch_stopped
import com.ciyin.app.sample.media_library_demo_batch_summary
import com.ciyin.app.sample.media_library_demo_check
import com.ciyin.app.sample.media_library_demo_checking
import com.ciyin.app.sample.media_library_demo_collection
import com.ciyin.app.sample.media_library_demo_collection_audio
import com.ciyin.app.sample.media_library_demo_collection_downloads
import com.ciyin.app.sample.media_library_demo_collection_images
import com.ciyin.app.sample.media_library_demo_collection_videos
import com.ciyin.app.sample.media_library_demo_delete
import com.ciyin.app.sample.media_library_demo_deleting
import com.ciyin.app.sample.media_library_demo_detail_title
import com.ciyin.app.sample.media_library_demo_display_name
import com.ciyin.app.sample.media_library_demo_error_already_exists
import com.ciyin.app.sample.media_library_demo_error_io
import com.ciyin.app.sample.media_library_demo_error_no_space
import com.ciyin.app.sample.media_library_demo_error_not_found
import com.ciyin.app.sample.media_library_demo_error_permission_denied
import com.ciyin.app.sample.media_library_demo_error_title
import com.ciyin.app.sample.media_library_demo_error_unsupported
import com.ciyin.app.sample.media_library_demo_exists
import com.ciyin.app.sample.media_library_demo_exists_no
import com.ciyin.app.sample.media_library_demo_exists_unknown
import com.ciyin.app.sample.media_library_demo_exists_yes
import com.ciyin.app.sample.media_library_demo_matrix_count
import com.ciyin.app.sample.media_library_demo_matrix_title
import com.ciyin.app.sample.media_library_demo_mime
import com.ciyin.app.sample.media_library_demo_operation_check
import com.ciyin.app.sample.media_library_demo_operation_delete
import com.ciyin.app.sample.media_library_demo_operation_publish
import com.ciyin.app.sample.media_library_demo_phase_checking
import com.ciyin.app.sample.media_library_demo_phase_deleted
import com.ciyin.app.sample.media_library_demo_phase_deleting
import com.ciyin.app.sample.media_library_demo_phase_failed
import com.ciyin.app.sample.media_library_demo_phase_missing
import com.ciyin.app.sample.media_library_demo_phase_published
import com.ciyin.app.sample.media_library_demo_phase_publishing
import com.ciyin.app.sample.media_library_demo_phase_ready
import com.ciyin.app.sample.media_library_demo_phase_unsupported
import com.ciyin.app.sample.media_library_demo_platform_id
import com.ciyin.app.sample.media_library_demo_publish
import com.ciyin.app.sample.media_library_demo_publishing
import com.ciyin.app.sample.media_library_demo_relative_directory
import com.ciyin.app.sample.media_library_demo_relative_directory_default
import com.ciyin.app.sample.media_library_demo_request_section
import com.ciyin.app.sample.media_library_demo_result_empty
import com.ciyin.app.sample.media_library_demo_result_section
import com.ciyin.app.sample.media_library_demo_sample_audio
import com.ciyin.app.sample.media_library_demo_sample_download
import com.ciyin.app.sample.media_library_demo_sample_image
import com.ciyin.app.sample.media_library_demo_sample_video
import com.ciyin.app.sample.media_library_demo_size
import com.ciyin.app.sample.media_library_demo_size_unknown
import com.ciyin.app.sample.media_library_demo_source_name
import com.ciyin.app.sample.media_library_demo_title
import com.ciyin.app.sample.media_library_demo_uri
import com.ciyin.app.sample.media_library_demo_uri_unavailable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/** 媒体库测试台根节点语义标签。 */
internal const val MediaLibraryDemoRootTag = "media-library-demo-root"

/** 媒体库测试矩阵语义标签。 */
internal const val MediaLibraryDemoMatrixTag = "media-library-demo-matrix"

/** 媒体库测试详情面板语义标签。 */
internal const val MediaLibraryDemoDetailTag = "media-library-demo-detail"

/** 媒体库单项命令区语义标签。 */
internal const val MediaLibraryDemoActionsTag = "media-library-demo-actions"

/**
 * 系统媒体库测试台页面入口。
 *
 * @param onBack 返回 sample 首页的回调。
 * @param viewModel 页面状态持有者。
 */
@Composable
internal fun MediaLibraryDemoScreen(
    onBack: () -> Unit,
    viewModel: MediaLibraryDemoViewModel = viewModel(::MediaLibraryDemoViewModel),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.collectSideEffects { effect ->
        when (effect) {
            MediaLibraryDemoEffect.NavigateBack -> onBack()
        }
    }

    MediaLibraryDemoContent(
        state = state,
        onAction = viewModel.dispatchAction,
    )
}

/**
 * 系统媒体库测试台纯 UI。
 *
 * @param state 当前测试台状态。
 * @param onAction 页面操作回调。
 * @param contentScrollState 页面内容滚动状态。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MediaLibraryDemoContent(
    state: MediaLibraryDemoUiState,
    onAction: (MediaLibraryDemoAction) -> Unit,
    contentScrollState: ScrollState = rememberScrollState(),
) {
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.testTag(MediaLibraryDemoRootTag),
        containerColor = AppTheme.colorScheme.surfaceLower,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.media_library_demo_title),
                        color = AppTheme.colorScheme.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(MediaLibraryDemoAction.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.media_library_demo_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colorScheme.surfaceLower,
                ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(contentScrollState),
            contentAlignment = Alignment.TopCenter,
        ) {
            val pageMaxWidth = AppTheme.sizes.layoutConstraints.dialogMaxWidth +
                AppTheme.sizes.layoutConstraints.dialogMaxWidth +
                AppTheme.spacings.huge
            Column(
                modifier = Modifier
                    .widthIn(max = pageMaxWidth)
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppTheme.spacings.large,
                        vertical = AppTheme.spacings.medium,
                    ),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
            ) {
                MediaLibraryBatchPanel(
                    state = state,
                    onPublishAll = {
                        onAction(MediaLibraryDemoAction.PublishAllClick(context))
                    },
                    onCheckAll = {
                        onAction(MediaLibraryDemoAction.ExistsAllClick(context))
                    },
                    onDeleteAll = {
                        onAction(MediaLibraryDemoAction.DeleteAllClick(context))
                    },
                )
                MediaLibraryWorkspace(
                    state = state,
                    onSampleSelect = { sampleId ->
                        onAction(MediaLibraryDemoAction.SampleSelect(sampleId))
                    },
                    onPublish = { sampleId ->
                        onAction(MediaLibraryDemoAction.PublishClick(context, sampleId))
                    },
                    onCheck = { sampleId ->
                        onAction(MediaLibraryDemoAction.ExistsClick(context, sampleId))
                    },
                    onDelete = { sampleId ->
                        onAction(MediaLibraryDemoAction.DeleteClick(context, sampleId))
                    },
                )
            }
        }
    }
}

/** 显示批量命令、当前进度和最终分类统计。 */
@Composable
private fun MediaLibraryBatchPanel(
    state: MediaLibraryDemoUiState,
    onPublishAll: () -> Unit,
    onCheckAll: () -> Unit,
    onDeleteAll: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
            MediaLibraryBatchStatus(state.batch)
            MediaLibraryBatchActions(
                publishEnabled = !state.isBusy && state.items.any { item -> !item.hasActiveReference },
                checkEnabled = !state.isBusy && state.items.any { item -> item.hasActiveReference },
                deleteEnabled = !state.isBusy && state.items.any { item -> item.hasActiveReference },
                onPublishAll = onPublishAll,
                onCheckAll = onCheckAll,
                onDeleteAll = onDeleteAll,
            )
        }
    }
}

/** 显示批量操作阶段、进度条和累计分类。 */
@Composable
private fun MediaLibraryBatchStatus(batch: MediaLibraryDemoBatchState) {
    val operation = batch.operationOrNull()
    val summary = batch.summaryOrNull()
    val title = when (batch) {
        MediaLibraryDemoBatchState.Idle -> stringResource(Res.string.media_library_demo_batch_idle)
        is MediaLibraryDemoBatchState.Running -> stringResource(
            Res.string.media_library_demo_batch_running,
            stringResource(batch.operation.resource()),
            batch.summary.processed,
            batch.summary.total,
        )
        is MediaLibraryDemoBatchState.Completed -> stringResource(
            Res.string.media_library_demo_batch_completed,
            stringResource(batch.operation.resource()),
        )
        is MediaLibraryDemoBatchState.Stopped -> stringResource(
            Res.string.media_library_demo_batch_stopped,
            stringResource(batch.operation.resource()),
        )
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = when (batch) {
                    MediaLibraryDemoBatchState.Idle -> Icons.Filled.Schedule
                    is MediaLibraryDemoBatchState.Running -> operation?.icon() ?: Icons.Filled.Schedule
                    is MediaLibraryDemoBatchState.Completed -> Icons.Filled.CheckCircle
                    is MediaLibraryDemoBatchState.Stopped -> Icons.Filled.Error
                },
                contentDescription = null,
                modifier = Modifier.size(AppTheme.sizes.icon.large),
                tint = when (batch) {
                    is MediaLibraryDemoBatchState.Completed -> AppTheme.colorScheme.success
                    is MediaLibraryDemoBatchState.Stopped -> AppTheme.colorScheme.error
                    else -> AppTheme.colorScheme.primary
                },
            )
            Text(
                text = title,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colorScheme.textPrimary,
            )
        }
        if (batch is MediaLibraryDemoBatchState.Running) {
            val progress = if (batch.summary.total == 0) {
                0f
            } else {
                batch.summary.processed.toFloat() / batch.summary.total.toFloat()
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        summary?.let { current ->
            Text(
                text = stringResource(
                    Res.string.media_library_demo_batch_summary,
                    current.succeeded,
                    current.unsupported,
                    current.failed,
                    current.skipped,
                ),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.textSecondary,
            )
        }
    }
}

/** 根据可用宽度横向或纵向排列三个批量命令。 */
@Composable
private fun MediaLibraryBatchActions(
    publishEnabled: Boolean,
    checkEnabled: Boolean,
    deleteEnabled: Boolean,
    onPublishAll: () -> Unit,
    onCheckAll: () -> Unit,
    onDeleteAll: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(MediaLibraryDemoActionsTag),
    ) {
        val useRow = maxWidth >= AppTheme.sizes.layoutConstraints.dialogMaxWidth
        if (useRow) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
            ) {
                MediaLibraryBatchButton(
                    label = stringResource(Res.string.media_library_demo_batch_publish),
                    icon = Icons.Filled.AddPhotoAlternate,
                    enabled = publishEnabled,
                    onClick = onPublishAll,
                    modifier = Modifier.weight(1f),
                )
                MediaLibraryBatchButton(
                    label = stringResource(Res.string.media_library_demo_batch_check),
                    icon = Icons.Filled.Search,
                    enabled = checkEnabled,
                    onClick = onCheckAll,
                    modifier = Modifier.weight(1f),
                )
                MediaLibraryBatchButton(
                    label = stringResource(Res.string.media_library_demo_batch_delete),
                    icon = Icons.Filled.Delete,
                    enabled = deleteEnabled,
                    onClick = onDeleteAll,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
            ) {
                MediaLibraryBatchButton(
                    label = stringResource(Res.string.media_library_demo_batch_publish),
                    icon = Icons.Filled.AddPhotoAlternate,
                    enabled = publishEnabled,
                    onClick = onPublishAll,
                )
                MediaLibraryBatchButton(
                    label = stringResource(Res.string.media_library_demo_batch_check),
                    icon = Icons.Filled.Search,
                    enabled = checkEnabled,
                    onClick = onCheckAll,
                )
                MediaLibraryBatchButton(
                    label = stringResource(Res.string.media_library_demo_batch_delete),
                    icon = Icons.Filled.Delete,
                    enabled = deleteEnabled,
                    onClick = onDeleteAll,
                )
            }
        }
    }
}

/** 显示一个批量操作按钮。 */
@Composable
private fun MediaLibraryBatchButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(
            text = label,
            modifier = Modifier.padding(start = AppTheme.spacings.small),
        )
    }
}

/** 根据页面宽度在双栏和上下布局之间切换。 */
@Composable
private fun MediaLibraryWorkspace(
    state: MediaLibraryDemoUiState,
    onSampleSelect: (MediaLibraryDemoSampleId) -> Unit,
    onPublish: (MediaLibraryDemoSampleId) -> Unit,
    onCheck: (MediaLibraryDemoSampleId) -> Unit,
    onDelete: (MediaLibraryDemoSampleId) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val wideLayoutMinWidth = AppTheme.sizes.layoutConstraints.dialogMaxWidth +
            AppTheme.sizes.layoutConstraints.cardMinWidth +
            AppTheme.spacings.huge
        if (maxWidth >= wideLayoutMinWidth) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
                verticalAlignment = Alignment.Top,
            ) {
                MediaLibraryMatrixPanel(
                    state = state,
                    onSampleSelect = onSampleSelect,
                    modifier = Modifier.weight(2f),
                )
                MediaLibraryDetailPanel(
                    state = state,
                    onPublish = onPublish,
                    onCheck = onCheck,
                    onDelete = onDelete,
                    modifier = Modifier.weight(3f),
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
            ) {
                MediaLibraryMatrixPanel(
                    state = state,
                    onSampleSelect = onSampleSelect,
                )
                MediaLibraryDetailPanel(
                    state = state,
                    onPublish = onPublish,
                    onCheck = onCheck,
                    onDelete = onDelete,
                )
            }
        }
    }
}

/** 显示四类媒体测试及其独立状态。 */
@Composable
private fun MediaLibraryMatrixPanel(
    state: MediaLibraryDemoUiState,
    onSampleSelect: (MediaLibraryDemoSampleId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(MediaLibraryDemoMatrixTag),
        shape = AppTheme.shapes.small,
        color = AppTheme.colorScheme.surface,
        border = BorderStroke(
            width = AppTheme.sizes.strokes.thin,
            color = AppTheme.colorScheme.outline,
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.spacings.large),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.media_library_demo_matrix_title),
                    style = AppTheme.typography.titleLarge,
                    color = AppTheme.colorScheme.textPrimary,
                )
                Text(
                    text = stringResource(
                        Res.string.media_library_demo_matrix_count,
                        state.items.size,
                    ),
                    style = AppTheme.typography.labelLarge,
                    color = AppTheme.colorScheme.textSecondary,
                )
            }
            HorizontalDivider(color = AppTheme.colorScheme.divider)
            state.items.forEachIndexed { index, item ->
                MediaLibraryMatrixItem(
                    item = item,
                    selected = item.sampleId == state.selectedSampleId,
                    onClick = { onSampleSelect(item.sampleId) },
                )
                if (index < state.items.lastIndex) {
                    HorizontalDivider(color = AppTheme.colorScheme.divider)
                }
            }
        }
    }
}

/** 显示一个可选择的媒体测试矩阵行。 */
@Composable
private fun MediaLibraryMatrixItem(
    item: MediaLibraryDemoItemState,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val sample = item.sampleId.sample()
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(item.sampleId.titleResource()),
                style = AppTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            Text(
                text = "${sample.mimeType} · ${item.sourceSize.sizeText()}",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colorScheme.textSecondary,
            )
        },
        leadingContent = {
            Icon(
                imageVector = item.sampleId.icon(),
                contentDescription = null,
                modifier = Modifier.size(AppTheme.sizes.icon.large),
                tint = if (selected) AppTheme.colorScheme.primary else AppTheme.colorScheme.textSecondary,
            )
        },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.extraSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = item.phase.icon(),
                    contentDescription = null,
                    modifier = Modifier.size(AppTheme.sizes.icon.small),
                    tint = item.phase.color(),
                )
                Text(
                    text = stringResource(item.phase.resource()),
                    style = AppTheme.typography.labelMedium,
                    color = item.phase.color(),
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (selected) {
                AppTheme.colorScheme.surfaceHigh
            } else {
                AppTheme.colorScheme.surface
            },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

/** 显示当前选中测试的请求、结果、错误和单项操作。 */
@Composable
private fun MediaLibraryDetailPanel(
    state: MediaLibraryDemoUiState,
    onPublish: (MediaLibraryDemoSampleId) -> Unit,
    onCheck: (MediaLibraryDemoSampleId) -> Unit,
    onDelete: (MediaLibraryDemoSampleId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = state.selectedItem
    val sample = item.sampleId.sample()
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(MediaLibraryDemoDetailTag),
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
            MediaLibraryDetailHeader(item)
            HorizontalDivider(color = AppTheme.colorScheme.divider)
            MediaLibraryRequestSection(
                item = item,
                sample = sample,
            )
            HorizontalDivider(color = AppTheme.colorScheme.divider)
            MediaLibraryResultSection(item)
            item.error?.let { error ->
                HorizontalDivider(color = AppTheme.colorScheme.divider)
                MediaLibraryErrorSection(error)
            }
            HorizontalDivider(color = AppTheme.colorScheme.divider)
            MediaLibraryItemActions(
                item = item,
                pageBusy = state.isBusy,
                onPublish = { onPublish(item.sampleId) },
                onCheck = { onCheck(item.sampleId) },
                onDelete = { onDelete(item.sampleId) },
            )
        }
    }
}

/** 显示当前测试名称和状态。 */
@Composable
private fun MediaLibraryDetailHeader(item: MediaLibraryDemoItemState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.sampleId.icon(),
            contentDescription = null,
            modifier = Modifier.size(AppTheme.sizes.icon.extraLarge),
            tint = AppTheme.colorScheme.primary,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.tiny),
        ) {
            Text(
                text = stringResource(Res.string.media_library_demo_detail_title),
                style = AppTheme.typography.labelLarge,
                color = AppTheme.colorScheme.textSecondary,
            )
            Text(
                text = stringResource(item.sampleId.titleResource()),
                style = AppTheme.typography.titleLarge,
                color = AppTheme.colorScheme.textPrimary,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = item.phase.icon(),
                contentDescription = null,
                modifier = Modifier.size(AppTheme.sizes.icon.small),
                tint = item.phase.color(),
            )
            Text(
                text = stringResource(item.phase.resource()),
                style = AppTheme.typography.labelLarge,
                color = item.phase.color(),
            )
        }
    }
}

/** 显示选中样本生成 MediaPublishRequest 时使用的字段。 */
@Composable
private fun MediaLibraryRequestSection(
    item: MediaLibraryDemoItemState,
    sample: MediaLibraryDemoSample,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.medium),
    ) {
        SectionTitle(stringResource(Res.string.media_library_demo_request_section))
        InfoRow(
            label = stringResource(Res.string.media_library_demo_source_name),
            value = sample.sourceFileName,
        )
        InfoRow(
            label = stringResource(Res.string.media_library_demo_mime),
            value = sample.mimeType,
        )
        InfoRow(
            label = stringResource(Res.string.media_library_demo_collection),
            value = stringResource(sample.collection.resource()),
        )
        InfoRow(
            label = stringResource(Res.string.media_library_demo_relative_directory),
            value = stringResource(Res.string.media_library_demo_relative_directory_default),
        )
        InfoRow(
            label = stringResource(Res.string.media_library_demo_size),
            value = item.sourceSize.sizeText(),
        )
    }
}

/** 显示最近一次 PublishedMedia 和存在性结果。 */
@Composable
private fun MediaLibraryResultSection(item: MediaLibraryDemoItemState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.medium),
    ) {
        SectionTitle(stringResource(Res.string.media_library_demo_result_section))
        val media = item.published
        if (media == null) {
            Text(
                text = stringResource(Res.string.media_library_demo_result_empty),
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
                        label = stringResource(Res.string.media_library_demo_platform_id),
                        value = media.platformId,
                    )
                    InfoRow(
                        label = stringResource(Res.string.media_library_demo_uri),
                        value = media.uri
                            ?: stringResource(Res.string.media_library_demo_uri_unavailable),
                    )
                    InfoRow(
                        label = stringResource(Res.string.media_library_demo_display_name),
                        value = media.displayName,
                    )
                    InfoRow(
                        label = stringResource(Res.string.media_library_demo_mime),
                        value = media.mimeType,
                    )
                    InfoRow(
                        label = stringResource(Res.string.media_library_demo_size),
                        value = media.size.formatFileSize(),
                    )
                    InfoRow(
                        label = stringResource(Res.string.media_library_demo_exists),
                        value = stringResource(item.exists.statusResource()),
                    )
                }
            }
        }
    }
}

/** 显示当前测试最近一次结构化错误。 */
@Composable
private fun MediaLibraryErrorSection(error: MediaLibraryDemoErrorModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = if (error.type == MediaLibraryDemoErrorType.Unsupported) {
                Icons.Filled.Block
            } else {
                Icons.Filled.Error
            },
            contentDescription = null,
            modifier = Modifier.size(AppTheme.sizes.icon.large),
            tint = if (error.type == MediaLibraryDemoErrorType.Unsupported) {
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
                text = stringResource(Res.string.media_library_demo_error_title),
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

/** 根据详情宽度横向或纵向排列单项命令。 */
@Composable
private fun MediaLibraryItemActions(
    item: MediaLibraryDemoItemState,
    pageBusy: Boolean,
    onPublish: () -> Unit,
    onCheck: () -> Unit,
    onDelete: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val useRow = maxWidth >= AppTheme.sizes.layoutConstraints.sheetMinWidth
        val publishButton: @Composable (Modifier) -> Unit = { modifier ->
            MediaLibraryOperationButton(
                label = stringResource(
                    if (item.phase == MediaLibraryDemoPhase.Publishing) {
                        Res.string.media_library_demo_publishing
                    } else {
                        Res.string.media_library_demo_publish
                    },
                ),
                icon = Icons.Filled.AddPhotoAlternate,
                loading = item.phase == MediaLibraryDemoPhase.Publishing,
                enabled = !pageBusy && !item.hasActiveReference,
                onClick = onPublish,
                primary = true,
                modifier = modifier,
            )
        }
        val checkButton: @Composable (Modifier) -> Unit = { modifier ->
            MediaLibraryOperationButton(
                label = stringResource(
                    if (item.phase == MediaLibraryDemoPhase.Checking) {
                        Res.string.media_library_demo_checking
                    } else {
                        Res.string.media_library_demo_check
                    },
                ),
                icon = Icons.Filled.Search,
                loading = item.phase == MediaLibraryDemoPhase.Checking,
                enabled = !pageBusy && item.hasActiveReference,
                onClick = onCheck,
                modifier = modifier,
            )
        }
        val deleteButton: @Composable (Modifier) -> Unit = { modifier ->
            MediaLibraryOperationButton(
                label = stringResource(
                    if (item.phase == MediaLibraryDemoPhase.Deleting) {
                        Res.string.media_library_demo_deleting
                    } else {
                        Res.string.media_library_demo_delete
                    },
                ),
                icon = Icons.Filled.Delete,
                loading = item.phase == MediaLibraryDemoPhase.Deleting,
                enabled = !pageBusy && item.hasActiveReference,
                onClick = onDelete,
                modifier = modifier,
            )
        }
        if (useRow) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
            ) {
                publishButton(Modifier.weight(1f))
                checkButton(Modifier.weight(1f))
                deleteButton(Modifier.weight(1f))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
            ) {
                publishButton(Modifier.fillMaxWidth())
                checkButton(Modifier.fillMaxWidth())
                deleteButton(Modifier.fillMaxWidth())
            }
        }
    }
}

/** 显示一个单项媒体库操作按钮。 */
@Composable
private fun MediaLibraryOperationButton(
    label: String,
    icon: ImageVector,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    val content: @Composable RowScope.() -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(AppTheme.sizes.icon.medium),
                strokeWidth = AppTheme.sizes.strokes.medium,
            )
        } else {
            Icon(imageVector = icon, contentDescription = null)
        }
        Text(
            text = label,
            modifier = Modifier.padding(start = AppTheme.spacings.small),
        )
    }
    if (primary) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.fillMaxWidth(),
            content = content,
        )
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.fillMaxWidth(),
            content = content,
        )
    }
}

/** 显示详情区块标题。 */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = AppTheme.typography.titleMedium,
        color = AppTheme.colorScheme.primary,
    )
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

/** 返回批量状态中的可选操作类型。 */
private fun MediaLibraryDemoBatchState.operationOrNull(): MediaLibraryDemoBatchOperation? = when (this) {
    MediaLibraryDemoBatchState.Idle -> null
    is MediaLibraryDemoBatchState.Running -> operation
    is MediaLibraryDemoBatchState.Completed -> operation
    is MediaLibraryDemoBatchState.Stopped -> operation
}

/** 返回批量状态中的可选累计结果。 */
private fun MediaLibraryDemoBatchState.summaryOrNull(): MediaLibraryDemoBatchSummary? = when (this) {
    MediaLibraryDemoBatchState.Idle -> null
    is MediaLibraryDemoBatchState.Running -> summary
    is MediaLibraryDemoBatchState.Completed -> summary
    is MediaLibraryDemoBatchState.Stopped -> summary
}

/** 返回测试标识对应的标题资源。 */
private fun MediaLibraryDemoSampleId.titleResource(): StringResource = when (this) {
    MediaLibraryDemoSampleId.Image -> Res.string.media_library_demo_sample_image
    MediaLibraryDemoSampleId.Video -> Res.string.media_library_demo_sample_video
    MediaLibraryDemoSampleId.Audio -> Res.string.media_library_demo_sample_audio
    MediaLibraryDemoSampleId.Download -> Res.string.media_library_demo_sample_download
}

/** 返回测试标识对应的媒体图标。 */
private fun MediaLibraryDemoSampleId.icon(): ImageVector = when (this) {
    MediaLibraryDemoSampleId.Image -> Icons.Filled.AddPhotoAlternate
    MediaLibraryDemoSampleId.Video -> Icons.Filled.PlayArrow
    MediaLibraryDemoSampleId.Audio -> Icons.Filled.MusicNote
    MediaLibraryDemoSampleId.Download -> Icons.Filled.Download
}

/** 返回媒体分类对应的标题资源。 */
private fun MediaCollection.resource(): StringResource = when (this) {
    MediaCollection.Images -> Res.string.media_library_demo_collection_images
    MediaCollection.Videos -> Res.string.media_library_demo_collection_videos
    MediaCollection.Audio -> Res.string.media_library_demo_collection_audio
    MediaCollection.Downloads -> Res.string.media_library_demo_collection_downloads
}

/** 返回页面阶段对应的标题资源。 */
private fun MediaLibraryDemoPhase.resource(): StringResource = when (this) {
    MediaLibraryDemoPhase.Ready -> Res.string.media_library_demo_phase_ready
    MediaLibraryDemoPhase.Publishing -> Res.string.media_library_demo_phase_publishing
    MediaLibraryDemoPhase.Published -> Res.string.media_library_demo_phase_published
    MediaLibraryDemoPhase.Checking -> Res.string.media_library_demo_phase_checking
    MediaLibraryDemoPhase.Missing -> Res.string.media_library_demo_phase_missing
    MediaLibraryDemoPhase.Deleting -> Res.string.media_library_demo_phase_deleting
    MediaLibraryDemoPhase.Deleted -> Res.string.media_library_demo_phase_deleted
    MediaLibraryDemoPhase.Unsupported -> Res.string.media_library_demo_phase_unsupported
    MediaLibraryDemoPhase.Failed -> Res.string.media_library_demo_phase_failed
}

/** 返回页面阶段对应的状态图标。 */
private fun MediaLibraryDemoPhase.icon(): ImageVector = when (this) {
    MediaLibraryDemoPhase.Ready -> Icons.Filled.Schedule
    MediaLibraryDemoPhase.Publishing -> Icons.Filled.AddPhotoAlternate
    MediaLibraryDemoPhase.Published -> Icons.Filled.CheckCircle
    MediaLibraryDemoPhase.Checking -> Icons.Filled.Search
    MediaLibraryDemoPhase.Missing -> Icons.Filled.Error
    MediaLibraryDemoPhase.Deleting -> Icons.Filled.Delete
    MediaLibraryDemoPhase.Deleted -> Icons.Filled.CheckCircle
    MediaLibraryDemoPhase.Unsupported -> Icons.Filled.Block
    MediaLibraryDemoPhase.Failed -> Icons.Filled.Error
}

/** 返回页面阶段对应的语义颜色。 */
@Composable
private fun MediaLibraryDemoPhase.color(): Color = when (this) {
    MediaLibraryDemoPhase.Ready,
    MediaLibraryDemoPhase.Deleted,
    -> AppTheme.colorScheme.textSecondary
    MediaLibraryDemoPhase.Publishing,
    MediaLibraryDemoPhase.Checking,
    MediaLibraryDemoPhase.Deleting,
    -> AppTheme.colorScheme.primary
    MediaLibraryDemoPhase.Published -> AppTheme.colorScheme.success
    MediaLibraryDemoPhase.Missing -> AppTheme.colorScheme.warning
    MediaLibraryDemoPhase.Unsupported -> AppTheme.colorScheme.info
    MediaLibraryDemoPhase.Failed -> AppTheme.colorScheme.error
}

/** 返回批量操作对应的标题资源。 */
private fun MediaLibraryDemoBatchOperation.resource(): StringResource = when (this) {
    MediaLibraryDemoBatchOperation.Publish -> Res.string.media_library_demo_operation_publish
    MediaLibraryDemoBatchOperation.Check -> Res.string.media_library_demo_operation_check
    MediaLibraryDemoBatchOperation.Delete -> Res.string.media_library_demo_operation_delete
}

/** 返回批量操作对应的图标。 */
private fun MediaLibraryDemoBatchOperation.icon(): ImageVector = when (this) {
    MediaLibraryDemoBatchOperation.Publish -> Icons.Filled.AddPhotoAlternate
    MediaLibraryDemoBatchOperation.Check -> Icons.Filled.Search
    MediaLibraryDemoBatchOperation.Delete -> Icons.Filled.Delete
}

/** 返回存在状态对应的字符串资源。 */
private fun Boolean?.statusResource(): StringResource = when (this) {
    null -> Res.string.media_library_demo_exists_unknown
    true -> Res.string.media_library_demo_exists_yes
    false -> Res.string.media_library_demo_exists_no
}

/** 返回可选字节大小对应的展示文本。 */
@Composable
private fun Long?.sizeText(): String = this?.formatFileSize()
    ?: stringResource(Res.string.media_library_demo_size_unknown)

/** 返回错误分类对应的字符串资源。 */
private fun MediaLibraryDemoErrorType.resource(): StringResource = when (this) {
    MediaLibraryDemoErrorType.NotFound -> Res.string.media_library_demo_error_not_found
    MediaLibraryDemoErrorType.AlreadyExists -> Res.string.media_library_demo_error_already_exists
    MediaLibraryDemoErrorType.PermissionDenied -> Res.string.media_library_demo_error_permission_denied
    MediaLibraryDemoErrorType.NoSpace -> Res.string.media_library_demo_error_no_space
    MediaLibraryDemoErrorType.Unsupported -> Res.string.media_library_demo_error_unsupported
    MediaLibraryDemoErrorType.Io -> Res.string.media_library_demo_error_io
}

/** 空闲测试矩阵预览。 */
@AppPreview
@Composable
private fun MediaLibraryDemoReadyPreview() {
    AppTheme {
        MediaLibraryDemoContent(
            state = previewReadyState(),
            onAction = {},
        )
    }
}

/** 混合测试结果预览。 */
@AppPreview
@Composable
private fun MediaLibraryDemoMixedPreview() {
    AppTheme {
        MediaLibraryDemoContent(
            state = previewMixedState(),
            onAction = {},
        )
    }
}

/** 创建包含资源大小的空闲预览状态。 */
private fun previewReadyState(): MediaLibraryDemoUiState = MediaLibraryDemoUiState(
    items = defaultMediaLibraryDemoItems.mapIndexed { index, item ->
        item.copy(sourceSize = (index + 1) * 1024L)
    },
)

/** 创建覆盖成功、不支持、失败和删除状态的预览。 */
private fun previewMixedState(): MediaLibraryDemoUiState {
    val image = MediaLibraryDemoPublishedModel(
        platformId = "content://media/external/images/media/42",
        uri = "content://media/external/images/media/42",
        displayName = "nomikit-media-library-image-42.png",
        mimeType = "image/png",
        size = 2636L,
    )
    return MediaLibraryDemoUiState(
        selectedSampleId = MediaLibraryDemoSampleId.Image,
        items = defaultMediaLibraryDemoItems.map { item ->
            when (item.sampleId) {
                MediaLibraryDemoSampleId.Image -> item.copy(
                    phase = MediaLibraryDemoPhase.Published,
                    sourceSize = 2636L,
                    published = image,
                    exists = true,
                )
                MediaLibraryDemoSampleId.Video -> item.copy(
                    phase = MediaLibraryDemoPhase.Deleted,
                    sourceSize = 48_000L,
                    exists = false,
                )
                MediaLibraryDemoSampleId.Audio -> item.copy(
                    phase = MediaLibraryDemoPhase.Unsupported,
                    sourceSize = 176_444L,
                    error = MediaLibraryDemoErrorModel(MediaLibraryDemoErrorType.Unsupported),
                )
                MediaLibraryDemoSampleId.Download -> item.copy(
                    phase = MediaLibraryDemoPhase.Failed,
                    sourceSize = 128L,
                    error = MediaLibraryDemoErrorModel(MediaLibraryDemoErrorType.PermissionDenied),
                )
            }
        },
        batch = MediaLibraryDemoBatchState.Completed(
            operation = MediaLibraryDemoBatchOperation.Publish,
            summary = MediaLibraryDemoBatchSummary(
                processed = 4,
                succeeded = 2,
                unsupported = 1,
                failed = 1,
            ),
        ),
    )
}
