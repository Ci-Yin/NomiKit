package com.ciyin.app.ui.screen.filedownloader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.io.resolve
import ciyin.material.theme.AppTheme
import ciyin.platform.LocalContext
import ciyin.platform.files
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 文件下载示例页面入口。
 *
 * @param onBack 宿主返回上一页回调。
 * @param viewModel 页面 ViewModel。
 */
@Composable
internal fun FileDownloaderDemoScreen(
    onBack: () -> Unit,
    viewModel: FileDownloaderDemoViewModel = viewModel(::FileDownloaderDemoViewModel),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val defaultSavePath = remember(context) {
        context.files.cacheDir.resolve(DefaultDownloadFileName).absolutePath
    }

    LaunchedEffect(defaultSavePath) {
        viewModel.dispatchAction(FileDownloaderDemoAction.DefaultSavePathLoaded(defaultSavePath))
    }

    viewModel.collectSideEffects { effect ->
        when (effect) {
            FileDownloaderDemoEffect.NavigateBack -> onBack()
        }
    }

    FileDownloaderDemoContent(
        state = state,
        onAction = viewModel.dispatchAction,
    )
}

/**
 * 文件下载示例页面纯 UI。
 *
 * @param state 页面状态。
 * @param onAction 页面动作回调。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileDownloaderDemoContent(
    state: FileDownloaderDemoUiState,
    onAction: (FileDownloaderDemoAction) -> Unit,
) {
    Scaffold(
        containerColor = AppTheme.colorScheme.surfaceLower,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "文件下载",
                        color = AppTheme.colorScheme.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(FileDownloaderDemoAction.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
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
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            val pageMaxWidth = AppTheme.sizes.layoutConstraints.dialogMaxWidth +
                    AppTheme.sizes.layoutConstraints.dialogMaxWidth +
                    AppTheme.spacings.huge
            BoxWithConstraints(
                modifier = Modifier
                    .widthIn(max = pageMaxWidth)
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppTheme.spacings.large,
                        vertical = AppTheme.spacings.medium,
                    ),
            ) {
                val wideLayoutMinWidth = AppTheme.sizes.layoutConstraints.dialogMaxWidth +
                        AppTheme.sizes.layoutConstraints.cardMinWidth +
                        AppTheme.spacings.huge
                if (maxWidth >= wideLayoutMinWidth) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.huge),
                        verticalAlignment = Alignment.Top,
                    ) {
                        FileDownloaderConfigurationPanel(
                            state = state,
                            onAction = onAction,
                            modifier = Modifier.weight(3f),
                        )
                        FileDownloaderStatusPanel(
                            state = state,
                            onAction = onAction,
                            modifier = Modifier.weight(2f),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
                    ) {
                        FileDownloaderConfigurationPanel(
                            state = state,
                            onAction = onAction,
                        )
                        FileDownloaderStatusPanel(
                            state = state,
                            onAction = onAction,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 下载任务配置面板。
 *
 * @param state 页面状态。
 * @param onAction 页面动作回调。
 * @param modifier 面板布局修饰符。
 */
@Composable
private fun FileDownloaderConfigurationPanel(
    state: FileDownloaderDemoUiState,
    onAction: (FileDownloaderDemoAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppTheme.shapes.small,
        color = AppTheme.colorScheme.surface,
        contentColor = AppTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = AppTheme.sizes.strokes.thin,
            color = AppTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacings.large),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.medium),
        ) {
            FileDownloaderPanelHeader(
                title = "任务配置",
                icon = Icons.Filled.Download,
            )
            OutlinedTextField(
                value = state.url,
                onValueChange = { onAction(FileDownloaderDemoAction.UrlChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canEditConfig,
                label = { Text("下载地址") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Link,
                        contentDescription = null,
                    )
                },
                minLines = 2,
                maxLines = 3,
            )
            OutlinedTextField(
                value = state.savePath,
                onValueChange = { onAction(FileDownloaderDemoAction.SavePathChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canEditConfig,
                label = { Text("保存路径") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                    )
                },
                minLines = 2,
                maxLines = 3,
            )

            HorizontalDivider(color = AppTheme.colorScheme.divider)

            Text(
                text = "传输策略",
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colorScheme.textPrimary,
            )
            FileDownloaderOptionRow(
                label = "断点续传",
                checked = state.enableResume,
                enabled = state.canEditConfig,
                onCheckedChange = {
                    onAction(FileDownloaderDemoAction.EnableResumeChange(it))
                },
            )
            FileDownloaderOptionRow(
                label = "分块下载",
                checked = state.enableChunkedDownload,
                enabled = state.canEditConfig,
                onCheckedChange = {
                    onAction(FileDownloaderDemoAction.EnableChunkedDownloadChange(it))
                },
            )
            FileDownloaderOptionRow(
                label = "覆盖已有文件",
                checked = state.overwriteExisting,
                enabled = state.canEditConfig,
                onCheckedChange = {
                    onAction(FileDownloaderDemoAction.OverwriteExistingChange(it))
                },
            )
        }
    }
}

/**
 * 下载状态与任务控制面板。
 *
 * @param state 页面状态。
 * @param onAction 页面动作回调。
 * @param modifier 面板布局修饰符。
 */
@Composable
private fun FileDownloaderStatusPanel(
    state: FileDownloaderDemoUiState,
    onAction: (FileDownloaderDemoAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppTheme.shapes.small,
        color = AppTheme.colorScheme.surface,
        contentColor = AppTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = AppTheme.sizes.strokes.thin,
            color = AppTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacings.large),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
        ) {
            FileDownloaderStatusHeader(state.phase)
            FileDownloaderProgress(state)
            FileDownloaderMetrics(state)
            FileDownloaderOutcome(state)

            HorizontalDivider(color = AppTheme.colorScheme.divider)

            FileDownloaderPrimaryAction(
                enabled = state.canStart,
                onClick = { onAction(FileDownloaderDemoAction.StartClick) },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FileDownloaderControlButton(
                    label = "暂停",
                    icon = Icons.Filled.Pause,
                    enabled = state.canPause,
                    onClick = { onAction(FileDownloaderDemoAction.PauseClick) },
                )
                FileDownloaderControlButton(
                    label = "恢复",
                    icon = Icons.Filled.PlayArrow,
                    enabled = state.canResume,
                    onClick = { onAction(FileDownloaderDemoAction.ResumeClick) },
                )
                FileDownloaderControlButton(
                    label = "取消",
                    icon = Icons.Filled.Close,
                    enabled = state.canCancel,
                    onClick = { onAction(FileDownloaderDemoAction.CancelClick) },
                )
                FileDownloaderControlButton(
                    label = "重新开始",
                    icon = Icons.Filled.Refresh,
                    enabled = state.canRestart,
                    onClick = { onAction(FileDownloaderDemoAction.RestartClick) },
                )
            }
        }
    }
}

/**
 * 面板标题。
 *
 * @param title 标题文本。
 * @param icon 标题图标。
 */
@Composable
private fun FileDownloaderPanelHeader(
    title: String,
    icon: ImageVector,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(AppTheme.sizes.icon.large),
            tint = AppTheme.colorScheme.primary,
        )
        Text(
            text = title,
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colorScheme.textPrimary,
        )
    }
}

/**
 * 可点击的下载选项行。
 *
 * @param label 选项名称。
 * @param checked 当前开关值。
 * @param enabled 是否允许修改。
 * @param onCheckedChange 开关变化回调。
 */
@Composable
private fun FileDownloaderOptionRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(vertical = AppTheme.spacings.small),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = AppTheme.typography.bodyLarge,
            color = if (enabled) {
                AppTheme.colorScheme.textPrimary
            } else {
                AppTheme.colorScheme.textDisabled
            },
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
        )
    }
}

/**
 * 下载阶段标题区。
 *
 * @param phase 当前下载阶段。
 */
@Composable
private fun FileDownloaderStatusHeader(phase: FileDownloaderDemoPhase) {
    val statusColor = fileDownloaderStatusColor(phase)
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = fileDownloaderStatusIcon(phase),
            contentDescription = null,
            modifier = Modifier.size(AppTheme.sizes.icon.large),
            tint = statusColor,
        )
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.tiny)) {
            Text(
                text = "实时状态",
                style = AppTheme.typography.labelLarge,
                color = AppTheme.colorScheme.textSecondary,
            )
            Text(
                text = phase.displayName,
                style = AppTheme.typography.titleLarge,
                color = statusColor,
            )
        }
    }
}

/**
 * 下载进度展示区。
 *
 * @param state 页面状态。
 */
@Composable
private fun FileDownloaderProgress(state: FileDownloaderDemoUiState) {
    val progress = state.progress?.coerceIn(0f, 1f)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "进度",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.textSecondary,
            )
            Text(
                text = progress?.let { "${(it * 100f).toInt()}%" } ?: "--",
                style = AppTheme.typography.headlineSmall,
                color = AppTheme.colorScheme.textPrimary,
            )
        }
        when {
            progress != null -> LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )

            state.phase == FileDownloaderDemoPhase.Starting -> LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )

            else -> LinearProgressIndicator(
                progress = { 0f },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * 下载量与速度指标区。
 *
 * @param state 页面状态。
 */
@Composable
private fun FileDownloaderMetrics(state: FileDownloaderDemoUiState) {
    val downloadedText = if (state.totalBytes > 0L) {
        "${state.downloadedBytes.formatDownloadBytes()} / ${state.totalBytes.formatDownloadBytes()}"
    } else {
        state.downloadedBytes.formatDownloadBytes()
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
    ) {
        FileDownloaderMetric(
            label = "已下载",
            value = downloadedText,
            modifier = Modifier.weight(1f),
        )
        FileDownloaderMetric(
            label = "当前速度",
            value = "${state.speedBytesPerSecond.formatDownloadBytes()}/s",
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * 单项下载指标。
 *
 * @param label 指标名称。
 * @param value 指标值。
 * @param modifier 指标布局修饰符。
 */
@Composable
private fun FileDownloaderMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.extraSmall),
    ) {
        Text(
            text = label,
            style = AppTheme.typography.labelMedium,
            color = AppTheme.colorScheme.textSecondary,
        )
        Text(
            text = value,
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colorScheme.textPrimary,
        )
    }
}

/**
 * 下载结果或错误信息区。
 *
 * @param state 页面状态。
 */
@Composable
private fun FileDownloaderOutcome(state: FileDownloaderDemoUiState) {
    state.completedPath?.let { path ->
        FileDownloaderOutcomeMessage(
            title = "结果路径",
            message = path,
            icon = Icons.Filled.CheckCircle,
            color = AppTheme.colorScheme.success,
        )
    }
    state.errorMessage?.let { message ->
        FileDownloaderOutcomeMessage(
            title = "错误信息",
            message = message,
            icon = Icons.Filled.Error,
            color = AppTheme.colorScheme.error,
        )
    }
}

/**
 * 带语义图标的终态信息。
 *
 * @param title 信息标题。
 * @param message 信息正文。
 * @param icon 语义图标。
 * @param color 语义颜色。
 */
@Composable
private fun FileDownloaderOutcomeMessage(
    title: String,
    message: String,
    icon: ImageVector,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(AppTheme.sizes.icon.medium),
            tint = color,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.extraSmall),
        ) {
            Text(
                text = title,
                style = AppTheme.typography.labelLarge,
                color = color,
            )
            SelectionContainer {
                Text(
                    text = message,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.textPrimary,
                )
            }
        }
    }
}

/**
 * 开始下载主操作。
 *
 * @param enabled 是否允许开始任务。
 * @param onClick 点击回调。
 */
@Composable
private fun FileDownloaderPrimaryAction(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
    ) {
        Icon(
            imageVector = Icons.Filled.Download,
            contentDescription = null,
            modifier = Modifier.size(AppTheme.sizes.icon.medium),
        )
        Text(
            text = "开始下载",
            modifier = Modifier.padding(start = AppTheme.spacings.small),
        )
    }
}

/**
 * 带无障碍描述与悬停提示的下载控制按钮。
 *
 * @param label 操作名称。
 * @param icon 操作图标。
 * @param enabled 是否允许操作。
 * @param onClick 点击回调。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileDownloaderControlButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above,
        ),
        tooltip = {
            PlainTooltip {
                Text(label)
            }
        },
        state = rememberTooltipState(),
    ) {
        OutlinedIconButton(
            onClick = onClick,
            enabled = enabled,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(AppTheme.sizes.icon.medium),
            )
        }
    }
}

/**
 * 返回下载阶段对应的状态图标。
 *
 * @param phase 下载阶段。
 * @return 对应状态图标。
 */
private fun fileDownloaderStatusIcon(phase: FileDownloaderDemoPhase): ImageVector = when (phase) {
    FileDownloaderDemoPhase.Idle -> Icons.Filled.Schedule
    FileDownloaderDemoPhase.Starting -> Icons.Filled.Download
    FileDownloaderDemoPhase.Downloading -> Icons.Filled.Download
    FileDownloaderDemoPhase.Paused -> Icons.Filled.Pause
    FileDownloaderDemoPhase.Resumed -> Icons.Filled.PlayArrow
    FileDownloaderDemoPhase.Cancelled -> Icons.Filled.Close
    FileDownloaderDemoPhase.Complete -> Icons.Filled.CheckCircle
    FileDownloaderDemoPhase.Error -> Icons.Filled.Error
}

/**
 * 返回下载阶段对应的主题语义色。
 *
 * @param phase 下载阶段。
 * @return 对应主题颜色。
 */
@Composable
private fun fileDownloaderStatusColor(phase: FileDownloaderDemoPhase): Color = when (phase) {
    FileDownloaderDemoPhase.Idle -> AppTheme.colorScheme.textSecondary
    FileDownloaderDemoPhase.Starting -> AppTheme.colorScheme.info
    FileDownloaderDemoPhase.Downloading -> AppTheme.colorScheme.primary
    FileDownloaderDemoPhase.Paused -> AppTheme.colorScheme.warning
    FileDownloaderDemoPhase.Resumed -> AppTheme.colorScheme.primary
    FileDownloaderDemoPhase.Cancelled -> AppTheme.colorScheme.warning
    FileDownloaderDemoPhase.Complete -> AppTheme.colorScheme.success
    FileDownloaderDemoPhase.Error -> AppTheme.colorScheme.error
}

/** 下载中状态页面预览。 */
@AppPreview
@Composable
private fun FileDownloaderDemoDownloadingPreview() {
    AppTheme {
        FileDownloaderDemoContent(
            state = FileDownloaderDemoUiState(
                savePath = "cache/nomikit-readme.md",
                phase = FileDownloaderDemoPhase.Downloading,
                progress = 0.42f,
                downloadedBytes = 430_080L,
                totalBytes = 1_024_000L,
                speedBytesPerSecond = 81_920L,
            ),
            onAction = {},
        )
    }
}

/** 下载完成状态页面预览。 */
@AppPreview
@Composable
private fun FileDownloaderDemoCompletePreview() {
    AppTheme {
        FileDownloaderDemoContent(
            state = FileDownloaderDemoUiState(
                savePath = "cache/nomikit-readme.md",
                phase = FileDownloaderDemoPhase.Complete,
                progress = 1f,
                downloadedBytes = 1_024_000L,
                totalBytes = 1_024_000L,
                completedPath = "cache/nomikit-readme.md",
            ),
            onAction = {},
        )
    }
}

/** 下载失败状态页面预览。 */
@AppPreview
@Composable
private fun FileDownloaderDemoErrorPreview() {
    AppTheme {
        FileDownloaderDemoContent(
            state = FileDownloaderDemoUiState(
                savePath = "cache/nomikit-readme.md",
                phase = FileDownloaderDemoPhase.Error,
                downloadedBytes = 327_680L,
                totalBytes = 1_024_000L,
                errorMessage = "服务器返回 503，下载任务未完成。",
            ),
            onAction = {},
        )
    }
}
