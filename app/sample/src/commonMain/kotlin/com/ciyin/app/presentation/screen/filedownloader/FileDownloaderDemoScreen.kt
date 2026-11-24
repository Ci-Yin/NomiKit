package com.ciyin.app.presentation.screen.filedownloader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.CheckmarkCircle
import io.github.composefluent.icons.regular.Dismiss
import io.github.composefluent.icons.regular.ArrowDownload
import io.github.composefluent.icons.regular.ErrorCircle
import io.github.composefluent.icons.regular.Folder
import io.github.composefluent.icons.regular.Link
import io.github.composefluent.icons.regular.Pause
import io.github.composefluent.icons.regular.Play
import io.github.composefluent.icons.regular.ArrowSync
import io.github.composefluent.icons.regular.Clock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiIconButton
import ciyin.designsystem.component.control.NomiSwitch
import ciyin.designsystem.component.divider.NomiDivider
import ciyin.designsystem.component.feedback.NomiProgressIndicator
import ciyin.designsystem.component.feedback.NomiProgressIndicatorType
import ciyin.designsystem.component.feedback.NomiTooltip
import ciyin.designsystem.component.icon.NomiIcon
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.component.text.NomiTextField
import ciyin.designsystem.component.text.NomiTextFieldVariant
import ciyin.io.resolve
import ciyin.material.theme.AppTheme
import ciyin.platform.LocalContext
import ciyin.platform.files
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.presentation.component.SamplePageScaffold
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
@Composable
private fun FileDownloaderDemoContent(
    state: FileDownloaderDemoUiState,
    onAction: (FileDownloaderDemoAction) -> Unit,
) {
    SamplePageScaffold(
        title = "文件下载",
        onBack = { onAction(FileDownloaderDemoAction.BackClick) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
    NomiSurface(
        modifier = modifier.fillMaxWidth(),
        shape = AppTheme.shapes.small,
        colors = NomiSurfaceColors(
            containerColor = AppTheme.colorScheme.surface,
            contentColor = AppTheme.colorScheme.onSurface,
        ),
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
            FileDownloaderPanelHeader(
                title = "任务配置",
                icon = Icons.Regular.ArrowDownload,
            )
            NomiTextField(
                value = state.url,
                onValueChange = { onAction(FileDownloaderDemoAction.UrlChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canEditConfig,
                variant = NomiTextFieldVariant.Outlined,
                label = { NomiText("下载地址") },
                leadingIcon = {
                    NomiIcon(
                        imageVector = Icons.Regular.Link,
                        contentDescription = null,
                    )
                },
                maxLines = 3,
            )
            NomiTextField(
                value = state.savePath,
                onValueChange = { onAction(FileDownloaderDemoAction.SavePathChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canEditConfig,
                variant = NomiTextFieldVariant.Outlined,
                label = { NomiText("保存路径") },
                leadingIcon = {
                    NomiIcon(
                        imageVector = Icons.Regular.Folder,
                        contentDescription = null,
                    )
                },
                maxLines = 3,
            )

            NomiDivider(color = AppTheme.colorScheme.divider)

            NomiText(
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
    NomiSurface(
        modifier = modifier.fillMaxWidth(),
        shape = AppTheme.shapes.small,
        colors = NomiSurfaceColors(
            containerColor = AppTheme.colorScheme.surface,
            contentColor = AppTheme.colorScheme.onSurface,
        ),
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
            FileDownloaderStatusHeader(state.phase)
            FileDownloaderProgress(state)
            FileDownloaderMetrics(state)
            FileDownloaderOutcome(state)

            NomiDivider(color = AppTheme.colorScheme.divider)

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
                    icon = Icons.Regular.Pause,
                    enabled = state.canPause,
                    onClick = { onAction(FileDownloaderDemoAction.PauseClick) },
                )
                FileDownloaderControlButton(
                    label = "恢复",
                    icon = Icons.Regular.Play,
                    enabled = state.canResume,
                    onClick = { onAction(FileDownloaderDemoAction.ResumeClick) },
                )
                FileDownloaderControlButton(
                    label = "取消",
                    icon = Icons.Regular.Dismiss,
                    enabled = state.canCancel,
                    onClick = { onAction(FileDownloaderDemoAction.CancelClick) },
                )
                FileDownloaderControlButton(
                    label = "重新开始",
                    icon = Icons.Regular.ArrowSync,
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
        NomiIcon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(AppTheme.sizes.icon.large),
            tint = AppTheme.colorScheme.primary,
        )
        NomiText(
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
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = AppTheme.spacings.small),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NomiText(
            text = label,
            modifier = Modifier.weight(1f),
            style = AppTheme.typography.bodyLarge,
            color = if (enabled) {
                AppTheme.colorScheme.textPrimary
            } else {
                AppTheme.colorScheme.textDisabled
            },
        )
        NomiSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
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
        NomiIcon(
            imageVector = fileDownloaderStatusIcon(phase),
            contentDescription = null,
            modifier = Modifier.size(AppTheme.sizes.icon.large),
            tint = statusColor,
        )
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.tiny)) {
            NomiText(
                text = "实时状态",
                style = AppTheme.typography.labelLarge,
                color = AppTheme.colorScheme.textSecondary,
            )
            NomiText(
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
            NomiText(
                text = "进度",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.textSecondary,
            )
            NomiText(
                text = progress?.let { "${(it * 100f).toInt()}%" } ?: "--",
                style = AppTheme.typography.headlineSmall,
                color = AppTheme.colorScheme.textPrimary,
            )
        }
        when {
            progress != null -> NomiProgressIndicator(
                progress = progress,
                type = NomiProgressIndicatorType.Linear,
                modifier = Modifier.fillMaxWidth(),
            )

            state.phase == FileDownloaderDemoPhase.Starting -> NomiProgressIndicator(
                progress = null,
                type = NomiProgressIndicatorType.Linear,
                modifier = Modifier.fillMaxWidth(),
            )

            else -> NomiProgressIndicator(
                progress = 0f,
                type = NomiProgressIndicatorType.Linear,
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
        NomiText(
            text = label,
            style = AppTheme.typography.labelMedium,
            color = AppTheme.colorScheme.textSecondary,
        )
        NomiText(
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
            icon = Icons.Regular.CheckmarkCircle,
            color = AppTheme.colorScheme.success,
        )
    }
    state.errorMessage?.let { message ->
        FileDownloaderOutcomeMessage(
            title = "错误信息",
            message = message,
            icon = Icons.Regular.ErrorCircle,
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
        NomiIcon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(AppTheme.sizes.icon.medium),
            tint = color,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.extraSmall),
        ) {
            NomiText(
                text = title,
                style = AppTheme.typography.labelLarge,
                color = color,
            )
            SelectionContainer {
                NomiText(
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
    NomiButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
    ) {
        NomiIcon(
            imageVector = Icons.Regular.ArrowDownload,
            contentDescription = null,
            modifier = Modifier.size(AppTheme.sizes.icon.medium),
        )
        NomiText(
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
@Composable
private fun FileDownloaderControlButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    NomiTooltip(tooltip = { NomiText(label) }) {
        NomiIconButton(
            onClick = onClick,
            enabled = enabled,
        ) {
            NomiIcon(
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
    FileDownloaderDemoPhase.Idle -> Icons.Regular.Clock
    FileDownloaderDemoPhase.Starting -> Icons.Regular.ArrowDownload
    FileDownloaderDemoPhase.Downloading -> Icons.Regular.ArrowDownload
    FileDownloaderDemoPhase.Paused -> Icons.Regular.Pause
    FileDownloaderDemoPhase.Resumed -> Icons.Regular.Play
    FileDownloaderDemoPhase.Cancelled -> Icons.Regular.Dismiss
    FileDownloaderDemoPhase.Complete -> Icons.Regular.CheckmarkCircle
    FileDownloaderDemoPhase.Error -> Icons.Regular.ErrorCircle
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
