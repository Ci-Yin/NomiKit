package com.ciyin.app.ui.screen.aichat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AiChatTopBar(
                onAction = onAction,
                onOpenConfig = { showConfigDialog = true },
            )
            AiChatMessageList(
                messages = state.messages,
                isStreaming = state.isStreaming,
                modifier = Modifier.weight(1f),
            )
            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
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
private fun AiChatTopBar(
    onAction: (AiChatAction) -> Unit,
    onOpenConfig: () -> Unit,
) {
    Row(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = { onAction(AiChatAction.BackClick) }) {
            Text("返回")
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "AI 聊天示例",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onOpenConfig) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "聊天配置",
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun AiChatConfigDialog(
    state: AiChatUiState,
    onAction: (AiChatAction) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("聊天配置") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.baseUrl,
                    onValueChange = { onAction(AiChatAction.BaseUrlChange(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Base URL") },
                    placeholder = { Text("例如 http://localhost:11434/v1") },
                )
                OutlinedTextField(
                    value = state.apiKey,
                    onValueChange = { onAction(AiChatAction.ApiKeyChange(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("API Key") },
                    placeholder = { Text("本地 Ollama 通常可留空") },
                    visualTransformation = PasswordVisualTransformation(),
                )
                OutlinedTextField(
                    value = state.model,
                    onValueChange = { onAction(AiChatAction.ModelChange(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("模型名") },
                    placeholder = { Text("例如 llama3.1 / gpt-4o-mini") },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        },
    )
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
                Text(
                    text = "输入一条消息，开始一次类 ChatGPT 的流式对话。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
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
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .sizeIn(minHeight = 40.dp),
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isUser) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(
                text = when {
                    showThinking -> "正在思考..."
                    message.text.isBlank() -> " "
                    else -> message.text
                },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AiChatInputBar(
    state: AiChatUiState,
    onAction: (AiChatAction) -> Unit,
) {
    HorizontalDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = state.input,
            onValueChange = { onAction(AiChatAction.InputChange(it)) },
            modifier = Modifier.weight(1f),
            enabled = !state.isStreaming,
            label = { Text("消息") },
            placeholder = { Text("向助手提问") },
        )
        Button(
            onClick = { onAction(AiChatAction.SendClick) },
            enabled = state.canSend,
        ) {
            Text(if (state.isStreaming) "生成中" else "发送")
        }
    }
}

@AppPreview
@Composable
private fun AiChatScreenPreview() {
    MaterialTheme {
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
