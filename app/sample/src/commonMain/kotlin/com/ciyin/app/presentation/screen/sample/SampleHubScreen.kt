package com.ciyin.app.presentation.screen.sample

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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_title
import com.ciyin.app.sample.design_system_demo_hub_description
import com.ciyin.app.sample.file_picker_demo_hub_description
import com.ciyin.app.sample.file_picker_demo_hub_title
import com.ciyin.app.sample.media_directory_demo_hub_description
import com.ciyin.app.sample.media_directory_demo_hub_title
import com.ciyin.app.sample.media_library_demo_hub_description
import com.ciyin.app.sample.media_library_demo_hub_title
import com.ciyin.app.sample.permissions_hub_description
import com.ciyin.app.sample.permissions_title
import com.ciyin.app.sample.platform_share_demo_hub_description
import com.ciyin.app.sample.platform_share_demo_title
import com.ciyin.app.sample.video_player_demo_description
import com.ciyin.app.sample.video_player_demo_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 样例入口列表，点击后交由独立样例导航栈处理。
 *
 * @param onExitSampleModule 点击顶部返回按钮时调用，由宿主决定如何退出或回退。
 * @param toNavigate 点击样例条目时接收其路由，由宿主维护导航栈。
 */
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
                        .then(
                            if (item.navRouter == DesignSystemDemoRouter) {
                                Modifier.testTag("design-system-hub-entry")
                            } else Modifier,
                        )
                        .clickable { toNavigate(item.navRouter) },
                )
                HorizontalDivider()
            }
        }
    }
}

/**
 * 样例首页展示条目，不持有实际导航副作用。
 *
 * @property title 入口标题；资源条目已按当前语言解析，部分既有条目使用固定文案。
 * @property description 入口能力说明，来源与标题一致。
 * @property navRouter 点击条目后提交给宿主导航的目标路由。
 */
@Immutable
private data class SampleHubEntry(
    val title: String,
    val description: String,
    val navRouter: NavRouter,
)

/**
 * 将资源读取值加入记忆键，资源文案变化后重新建立条目。
 *
 * @return 按首页展示顺序排列的入口列表；资源字符串不变时复用当前组合中的列表实例。
 */
@Composable
private fun rememberSampleHubEntries(): List<SampleHubEntry> {
    val designSystemTitle = stringResource(Res.string.design_system_demo_title)
    val designSystemDescription = stringResource(Res.string.design_system_demo_hub_description)
    val permissionsTitle = stringResource(Res.string.permissions_title)
    val permissionsDescription = stringResource(Res.string.permissions_hub_description)
    val platformShareTitle = stringResource(Res.string.platform_share_demo_title)
    val platformShareDescription = stringResource(Res.string.platform_share_demo_hub_description)
    val videoPlayerTitle = stringResource(Res.string.video_player_demo_title)
    val videoPlayerDescription = stringResource(Res.string.video_player_demo_description)
    val mediaLibraryTitle = stringResource(Res.string.media_library_demo_hub_title)
    val mediaLibraryDescription = stringResource(Res.string.media_library_demo_hub_description)
    val filePickerTitle = stringResource(Res.string.file_picker_demo_hub_title)
    val filePickerDescription = stringResource(Res.string.file_picker_demo_hub_description)
    val mediaDirectoryTitle = stringResource(Res.string.media_directory_demo_hub_title)
    val mediaDirectoryDescription = stringResource(Res.string.media_directory_demo_hub_description)
    return remember(
        designSystemTitle,
        designSystemDescription,
        permissionsTitle,
        permissionsDescription,
        platformShareTitle,
        platformShareDescription,
        videoPlayerTitle,
        videoPlayerDescription,
        mediaLibraryTitle,
        mediaLibraryDescription,
        filePickerTitle,
        filePickerDescription,
        mediaDirectoryTitle,
        mediaDirectoryDescription,
    ) {
        listOf(
            SampleHubEntry(
                title = designSystemTitle,
                description = designSystemDescription,
                navRouter = DesignSystemDemoRouter,
            ),
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
                title = platformShareTitle,
                description = platformShareDescription,
                navRouter = PlatformShareDemoRouter,
            ),
            SampleHubEntry(
                title = videoPlayerTitle,
                description = videoPlayerDescription,
                navRouter = VideoPlayerDemoRouter,
            ),
            SampleHubEntry(
                title = mediaLibraryTitle,
                description = mediaLibraryDescription,
                navRouter = MediaLibraryDemoRouter,
            ),
            SampleHubEntry(
                title = filePickerTitle,
                description = filePickerDescription,
                navRouter = FilePickerDemoRouter,
            ),
            SampleHubEntry(
                title = mediaDirectoryTitle,
                description = mediaDirectoryDescription,
                navRouter = MediaDirectoryDemoRouter,
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

/**
 * 无导航副作用的首页内容预览；使用新主题桥接现有 Material 组件。
 */
@AppPreview
@Composable
private fun SampleHubContentPreview() {
    NomiTheme(designSystem = NomiDesignSystem.Material3) {
        SampleHubScreen(onExitSampleModule = {}, toNavigate = {})
    }
}
