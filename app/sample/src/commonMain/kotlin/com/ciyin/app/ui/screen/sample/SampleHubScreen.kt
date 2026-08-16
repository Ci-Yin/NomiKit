package com.ciyin.app.ui.screen.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.permissions_hub_description
import com.ciyin.app.sample.permissions_title
import com.ciyin.app.sample.video_player_demo_description
import com.ciyin.app.sample.video_player_demo_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SampleHubScreen(
    onExitSampleModule: () -> Unit,
    toNavigate: (NavRouter) -> Unit,
) {
    val entries = rememberSampleHubEntries()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("示例 / 样例") },
                navigationIcon = {
                    IconButton(onClick = onExitSampleModule) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "退出示例模块"
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(
                items = entries,
                key = { it.title },
            ) { item ->
                ListItem(
                    headlineContent = { Text(item.title) },
                    supportingContent = { Text(item.description) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { toNavigate(item.navRouter) },
                )
                HorizontalDivider()
            }
        }
    }
}

private data class SampleHubEntry(
    val title: String,
    val description: String,
    val navRouter: NavRouter,
)

@Composable
private fun rememberSampleHubEntries(): List<SampleHubEntry> {
    val permissionsTitle = stringResource(Res.string.permissions_title)
    val permissionsDescription = stringResource(Res.string.permissions_hub_description)
    val videoPlayerTitle = stringResource(Res.string.video_player_demo_title)
    val videoPlayerDescription = stringResource(Res.string.video_player_demo_description)
    return remember(
        permissionsTitle,
        permissionsDescription,
        videoPlayerTitle,
        videoPlayerDescription,
    ) {
        listOf(
            SampleHubEntry(
                title = "文生图（AiImageIntegrate + SD WebUI）",
                description = "feature/ai-integrate 最小示例，需本地 WebUI --api",
                navRouter = AiImageDemoRouter,
            ),
            SampleHubEntry(
                title = "AI 聊天（AiChatIntegrate + OpenAI 兼容）",
                description = "类 ChatGPT 最小流式聊天示例，支持本地 Ollama 或云端兼容端点。",
                navRouter = AiChatRouter,
            ),
            SampleHubEntry(
                title = "文件下载",
                description = "断点续传、分块下载、覆盖策略与任务控制示例。",
                navRouter = FileDownloaderDemoRouter,
            ),
            SampleHubEntry(
                title = permissionsTitle,
                description = permissionsDescription,
                navRouter = PermissionsRouter,
            ),
            SampleHubEntry(
                title = videoPlayerTitle,
                description = videoPlayerDescription,
                navRouter = VideoPlayerDemoRouter,
            ),
            SampleHubEntry(
                title = "运行环境信息",
                description = "展示当前平台、构建配置、应用目录与时间参数。",
                navRouter = RuntimeInfoRouter,
            ),
            SampleHubEntry(
                title = "占位示例 A",
                description = "第二个占位入口。",
                navRouter = SampleExamplePlaceholderARouter,
            ),
        )
    }
}
