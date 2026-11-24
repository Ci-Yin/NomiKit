package com.ciyin.app.presentation.screen.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import ciyin.designsystem.component.icon.NomiIcon
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.scaffold.NomiScaffold
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.presentation.component.samplePageBackgroundColor
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_hub_description
import com.ciyin.app.sample.design_system_demo_title
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
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.ArrowDownload
import io.github.composefluent.icons.regular.Chat
import io.github.composefluent.icons.regular.ChevronRight
import io.github.composefluent.icons.regular.Folder
import io.github.composefluent.icons.regular.FolderOpen
import io.github.composefluent.icons.regular.Image
import io.github.composefluent.icons.regular.Info
import io.github.composefluent.icons.regular.Library
import io.github.composefluent.icons.regular.LockClosed
import io.github.composefluent.icons.regular.PaintBrush
import io.github.composefluent.icons.regular.Share
import io.github.composefluent.icons.regular.Video
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 样例入口列表，点击后交由独立样例导航栈处理。
 *
 * 本页是样例子导航栈的根页，身后没有可退回的页面，因此不提供返回入口；
 * 系统返回由宿主 NavDisplay 处理。
 *
 * @param toNavigate 点击样例条目时接收其路由，由宿主维护导航栈。
 */
@Composable
internal fun SampleHubScreen(
    toNavigate: (NavRouter) -> Unit,
) {
    val entries = rememberSampleHubEntries()

    NomiScaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        colors = NomiScaffoldColors(containerColor = samplePageBackgroundColor()),
        topBar = {
            NomiText(
                text = "示例",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = NomiTheme.spacings.huge,
                        vertical = NomiTheme.spacings.medium,
                    ),
                style = NomiTheme.typography.titleLarge,
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = NomiTheme.spacings.huge)
                .padding(bottom = NomiTheme.spacings.huge),
            verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.extraSmall),
        ) {
            items(
                items = entries,
                key = { it.title },
            ) { item ->
                NomiListItem(
                    title = { NomiText(item.title) },
                    subtitle = { NomiText(item.description) },
                    leading = {
                        NomiIcon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(NomiTheme.sizes.icon.large),
                            tint = NomiTheme.colorScheme.textSecondary,
                        )
                    },
                    trailing = {
                        NomiIcon(
                            imageVector = Icons.Regular.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(NomiTheme.sizes.icon.small),
                            tint = NomiTheme.colorScheme.textSecondary,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (item.navRouter == DesignSystemDemoRouter) {
                                Modifier.testTag("design-system-hub-entry")
                            } else Modifier,
                        ),
                    onClick = { toNavigate(item.navRouter) },
                )
            }
        }
    }
}

/**
 * 样例首页展示条目，不持有实际导航副作用。
 *
 * @property title 入口标题；资源条目已按当前语言解析，部分既有条目使用固定文案。
 * @property description 入口能力说明，来源与标题一致。
 * @property icon 前导图标；仅作视觉标识，无障碍名称由标题提供。
 * @property navRouter 点击条目后提交给宿主导航的目标路由。
 */
@Immutable
private data class SampleHubEntry(
    val title: String,
    val description: String,
    val icon: ImageVector,
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
                icon = Icons.Regular.PaintBrush,
                navRouter = DesignSystemDemoRouter,
            ),
            SampleHubEntry(
                title = "文生图（AiImageIntegrate + SD WebUI）",
                description = "feature/ai-integrate 最小示例，需本地 WebUI --api",
                icon = Icons.Regular.Image,
                navRouter = AiImageDemoRouter,
            ),
            SampleHubEntry(
                title = "AI 聊天（AiChatIntegrate + OpenAI 兼容）",
                description = "类 ChatGPT 最小流式聊天示例，支持本地 Ollama 或云端兼容端点。",
                icon = Icons.Regular.Chat,
                navRouter = AiChatRouter,
            ),
            SampleHubEntry(
                title = "文件下载",
                description = "断点续传、分块下载、覆盖策略与任务控制示例。",
                icon = Icons.Regular.ArrowDownload,
                navRouter = FileDownloaderDemoRouter,
            ),
            SampleHubEntry(
                title = permissionsTitle,
                description = permissionsDescription,
                icon = Icons.Regular.LockClosed,
                navRouter = PermissionsRouter,
            ),
            SampleHubEntry(
                title = platformShareTitle,
                description = platformShareDescription,
                icon = Icons.Regular.Share,
                navRouter = PlatformShareDemoRouter,
            ),
            SampleHubEntry(
                title = videoPlayerTitle,
                description = videoPlayerDescription,
                icon = Icons.Regular.Video,
                navRouter = VideoPlayerDemoRouter,
            ),
            SampleHubEntry(
                title = mediaLibraryTitle,
                description = mediaLibraryDescription,
                icon = Icons.Regular.Library,
                navRouter = MediaLibraryDemoRouter,
            ),
            SampleHubEntry(
                title = filePickerTitle,
                description = filePickerDescription,
                icon = Icons.Regular.FolderOpen,
                navRouter = FilePickerDemoRouter,
            ),
            SampleHubEntry(
                title = mediaDirectoryTitle,
                description = mediaDirectoryDescription,
                icon = Icons.Regular.Folder,
                navRouter = MediaDirectoryDemoRouter,
            ),
            SampleHubEntry(
                title = "运行环境信息",
                description = "展示当前平台、构建配置、应用目录与时间参数。",
                icon = Icons.Regular.Info,
                navRouter = RuntimeInfoRouter,
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
    NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
        SampleHubScreen(toNavigate = {})
    }
}
