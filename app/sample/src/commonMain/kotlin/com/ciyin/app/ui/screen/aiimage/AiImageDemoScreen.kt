package com.ciyin.app.ui.screen.aiimage

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel
import org.jetbrains.compose.ui.tooling.preview.AppPreview

private val AiImageDemoBodySmallStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
)

private val AiImageDemoBodyMediumStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
)

private val AiImageDemoErrorColor = Color(0xFFB3261E)

/**
 * 文生图最小示例（经 [ciyin.ai.facade.AiImage]）。
 *
 * **使用前**：请在运行设备可访问的地址启动 [AUTOMATIC1111 WebUI](https://github.com/AUTOMATIC1111/stable-diffusion-webui)，
 * 默认监听 `7860`；「主机 / IP」可在界面填写（例如 Android 模拟器访宿主机常用 `10.0.2.2`）。
 */
@Composable
internal fun AiImageDemoScreen(
    onBack: () -> Unit,
    viewModel: AiImageDemoViewModel = viewModel(::AiImageDemoViewModel),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.collectSideEffects { effect ->
        when (effect) {
            AiImageDemoEffect.NavigateBack -> onBack()
        }
    }

    AiImageDemoContent(
        state = state,
        onAction = viewModel.dispatchAction,
    )
}

/**
 * 文生图演示页面的纯 UI。
 *
 * 仅依赖 [AiImageDemoUiState] 与 [onAction]；交互统一通过 [onAction] 回传，不持有 ViewModel。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiImageDemoContent(
    state: AiImageDemoUiState,
    onAction: (AiImageDemoAction) -> Unit,
) {
    val bitmap: ImageBitmap? = remember(state.resultBytes, state.resultMimeType) {
        val bytes = state.resultBytes
        val mime = state.resultMimeType
        if (bytes != null && mime != null) decodeGeneratedImageBytes(bytes, mime) else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("文生图演示") },
                navigationIcon = {
                    IconButton(onClick = { onAction(AiImageDemoAction.BackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(paddingValues)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "依赖本地 SD WebUI（--api）。未启动服务或地址不可达时会显示错误信息。",
                style = AiImageDemoBodySmallStyle,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.serverHost,
                onValueChange = { onAction(AiImageDemoAction.ServerHostChange(it)) },
                enabled = !state.isLoading,
                label = { Text("WebUI 主机 / IP") },
                placeholder = { Text("127.0.0.1") },
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.prompt,
                maxLines = 2,
                onValueChange = { onAction(AiImageDemoAction.PromptChange(it)) },
                label = { Text("提示词") },
            )
            Button(
                onClick = { onAction(AiImageDemoAction.GenerateClick) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("生成")
            }
            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val p = state.progress
                    if (p != null) {
                        LinearProgressIndicator(
                            progress = { p },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    state.progressMessage?.let { msg ->
                        Text(
                            text = msg,
                            style = AiImageDemoBodySmallStyle,
                        )
                    }
                }
            }
            state.errorMessage?.let { err ->
                Text(
                    text = err,
                    color = AiImageDemoErrorColor,
                    style = AiImageDemoBodyMediumStyle,
                )
            }
            bitmap?.let { bmp ->
                Image(
                    painter = BitmapPainter(bmp),
                    contentDescription = "生成结果",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Fit,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@AppPreview
@Composable
private fun AiImageDemoContentPreview() {
    AiImageDemoContent(
        state = AiImageDemoUiState(),
        onAction = {},
    )
}
