package com.ciyin.app.presentation.screen.aichat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.MoreVertical
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.button.NomiIconButton
import ciyin.designsystem.component.divider.NomiDivider
import ciyin.designsystem.component.icon.NomiIcon
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.component.text.NomiTextField
import ciyin.designsystem.component.text.NomiTextFieldVariant
import ciyin.designsystem.theme.NomiTheme
import ciyin.material.theme.AppTheme
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.presentation.component.SampleMaterialTheme
import com.ciyin.app.presentation.component.SamplePageScaffold
import org.jetbrains.compose.ui.tooling.preview.Preview as AppPreview

/**
 * AI 聊天示例页面入口。
 *
 * @param onBack 外层导航返回回调。
 * @param viewModel 页面 ViewModel。
 */
@Composable
internal fun AiChatScreen(
    onBack: () -> Unit,
    viewModel: AiChatViewModel = viewModel(::AiChatViewModel),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.collectSideEffects { effect ->
        when (effect) {
            AiChatEffect.NavigateBack -> onBack()
        }
    }

    AiChatContent(
        state = state,
        onAction = viewModel.dispatchAction,
    )
}

@Composable
private fun AiChatContent(
    state: AiChatUiState,
    onAction: (AiChatAction) -> Unit,
) {
    var showConfigDialog by remember { mutableStateOf(false) }

    SamplePageScaffold(
        title = "AI 聊天示例",
        onBack = { onAction(AiChatAction.BackClick) },
        actions = {
            NomiIconButton(onClick = { showConfigDialog = true }) {
                NomiIcon(
                    imageVector = Icons.Regular.MoreVertical,
                    contentDescription = "聊天配置",
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AiChatMessageList(
                messages = state.messages,
                isStreaming = state.isStreaming,
                modifier = Modifier.weight(1f),
            )
            state.errorMessage?.let { message ->
                NomiText(
                    text = message,
                    color = NomiTheme.colorScheme.error,
                    style = NomiTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            AiChatInputBar(state = state, onAction = onAction)
        }
    }

    if (showConfigDialog) {
        AiChatConfigDialog(
            state = state,
            onAction = onAction,
            onDismiss = { showConfigDialog = false },
        )
    }
}

@Composable
private fun AiChatConfigDialog(
    state: AiChatUiState,
    onAction: (AiChatAction) -> Unit,
    onDismiss: () -> Unit,
) {
    SampleMaterialTheme {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                NomiText(
                    text = "聊天配置",
                    style = NomiTheme.typography.titleMedium,
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NomiTextField(
                        value = state.baseUrl,
                        onValueChange = { onAction(AiChatAction.BaseUrlChange(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        variant = NomiTextFieldVariant.Outlined,
                        label = { NomiText("Base URL") },
                        placeholder = { NomiText("例如 http://localhost:11434/v1") },
                    )
                    NomiTextField(
                        value = state.apiKey,
                        onValueChange = { onAction(AiChatAction.ApiKeyChange(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        variant = NomiTextFieldVariant.Outlined,
                        label = { NomiText("API Key") },
                        placeholder = { NomiText("本地 Ollama 通常可留空") },
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    NomiTextField(
                        value = state.model,
                        onValueChange = { onAction(AiChatAction.ModelChange(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        variant = NomiTextFieldVariant.Outlined,
                        label = { NomiText("模型名") },
                        placeholder = { NomiText("例如 llama3.1 / gpt-4o-mini") },
                    )
                }
            },
            confirmButton = {
                NomiButton(
                    onClick = onDismiss,
                    variant = NomiButtonVariant.Text,
                ) {
                    NomiText("完成")
                }
            },
        )
    }
}

@Composable
private fun AiChatMessageList(
    messages: List<AiChatMessageItem>,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (messages.isEmpty()) {
            item(key = "empty") {
                NomiText(
                    text = "输入一条消息，开始一次类 ChatGPT 的流式对话。",
                    color = NomiTheme.colorScheme.textSecondary,
                    style = NomiTheme.typography.bodyMedium,
                )
            }
        }
        items(
            items = messages,
            key = { it.id },
        ) { message ->
            AiChatMessageBubble(
                message = message,
                showThinking = isStreaming && message == messages.lastOrNull() && message.text.isBlank(),
            )
        }
    }
}

@Composable
private fun AiChatMessageBubble(
    message: AiChatMessageItem,
    showThinking: Boolean,
) {
    val isUser = message.role == AiChatMessageRole.User
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        NomiSurface(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .sizeIn(minHeight = 40.dp),
            colors = NomiSurfaceColors(
                containerColor = if (isUser) {
                    NomiTheme.colorScheme.primary
                } else {
                    NomiTheme.colorScheme.surfaceHigh
                },
                contentColor = if (isUser) {
                    NomiTheme.colorScheme.onPrimary
                } else {
                    NomiTheme.colorScheme.textPrimary
                },
            ),
            shape = RoundedCornerShape(18.dp),
        ) {
            NomiText(
                text = when {
                    showThinking -> "正在思考..."
                    message.text.isBlank() -> " "
                    else -> message.text
                },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = NomiTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AiChatInputBar(
    state: AiChatUiState,
    onAction: (AiChatAction) -> Unit,
) {
    NomiDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NomiTextField(
            value = state.input,
            onValueChange = { onAction(AiChatAction.InputChange(it)) },
            modifier = Modifier.weight(1f),
            enabled = !state.isStreaming,
            variant = NomiTextFieldVariant.Outlined,
            label = { NomiText("消息") },
            placeholder = { NomiText("向助手提问") },
        )
        NomiButton(
            onClick = { onAction(AiChatAction.SendClick) },
            enabled = state.canSend,
        ) {
            NomiText(if (state.isStreaming) "生成中" else "发送")
        }
    }
}

@AppPreview
@Composable
private fun AiChatScreenPreview() {
    AppTheme {
        AiChatContent(
            state = AiChatUiState(
                messages = listOf(
                    AiChatMessageItem(
                        id = 1L,
                        role = AiChatMessageRole.User,
                        text = "请用一句话介绍 NomiKit。",
                    ),
                    AiChatMessageItem(
                        id = 2L,
                        role = AiChatMessageRole.Assistant,
                        text = "NomiKit 是一个用于验证 KMP 架构与 AI 能力接入的示例项目。",
                    ),
                ),
            ),
            onAction = {},
        )
    }
}
