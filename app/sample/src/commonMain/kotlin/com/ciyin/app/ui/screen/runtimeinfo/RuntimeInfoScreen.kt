package com.ciyin.app.ui.screen.runtimeinfo

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
import androidx.compose.ui.unit.dp
import ciyin.application.config.AppBuildConfig
import ciyin.application.config.LocalAppBuildConfig
import ciyin.io.File
import ciyin.platform.Context
import ciyin.platform.LocalContext
import ciyin.platform.Platform
import ciyin.platform.context.ContextFiles
import ciyin.platform.currentPlatform
import ciyin.platform.files
import ciyin.platform.is64bit
import ciyin.platform.isAArch
import ciyin.platform.isDesktop
import ciyin.platform.isMobile
import ciyin.platform.time.currentTimeMillis
import ciyin.platform.time.currentTimeSecond
import ciyin.platform.time.format
import ciyin.platform.time.nowLocal
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 运行环境信息示例页入口。
 *
 * @param onBack 返回上一页的回调。
 */
@Composable
internal fun RuntimeInfoScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val appBuildConfig = LocalAppBuildConfig.current
    val currentPlatform = remember { currentPlatform() }
    val sections = remember(context, appBuildConfig, currentPlatform) {
        createRuntimeInfoSections(
            context = context,
            appBuildConfig = appBuildConfig,
            currentPlatform = currentPlatform,
        )
    }

    RuntimeInfoContent(
        sections = sections,
        onBack = onBack,
    )
}

/**
 * 运行环境信息页面的纯 UI。
 *
 * @param sections 参数展示分组。
 * @param onBack 返回上一页的回调。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RuntimeInfoContent(
    sections: List<RuntimeInfoSection>,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("运行环境信息") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
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
            sections.forEach { section ->
                item(key = section.title) {
                    ListItem(
                        headlineContent = { Text(section.title) },
                    )
                    HorizontalDivider()
                }
                items(
                    items = section.items,
                    key = { "${section.title}-${it.label}" },
                ) { item ->
                    ListItem(
                        headlineContent = { Text(item.label) },
                        trailingContent = { Text(item.value) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * 构建运行环境信息分组。
 *
 * @param context 当前平台上下文。
 * @param appBuildConfig 当前应用构建配置。
 * @param currentPlatform 当前平台信息。
 * @return 可供页面展示的参数分组。
 */
@OptIn(ExperimentalTime::class)
private fun createRuntimeInfoSections(
    context: Context,
    appBuildConfig: AppBuildConfig,
    currentPlatform: Platform,
): List<RuntimeInfoSection> {
    val localTime = Clock.System.nowLocal().format()
    val contextFiles = context.files
    return listOf(
        createAppBuildConfigSection(appBuildConfig),
        createPlatformSection(currentPlatform),
        createContextFilesSection(contextFiles),
        RuntimeInfoSection(
            title = "时间",
            items = listOf(
                RuntimeInfoItem(
                    label = "本地时间",
                    value = localTime,
                ),
                RuntimeInfoItem(
                    label = "时间戳（毫秒）",
                    value = currentTimeMillis().toString(),
                ),
                RuntimeInfoItem(
                    label = "时间戳（秒）",
                    value = currentTimeSecond().toString(),
                ),
            ),
        ),
    )
}

/**
 * 创建平台参数分组。
 *
 * @param platform 当前平台信息。
 * @return 平台参数分组。
 */
private fun createPlatformSection(platform: Platform): RuntimeInfoSection {
    return RuntimeInfoSection(
        title = "平台",
        items = listOf(
            RuntimeInfoItem(
                label = "名称",
                value = platform.name,
            ),
            RuntimeInfoItem(
                label = "架构",
                value = platform.arch.displayName,
            ),
            RuntimeInfoItem(
                label = "名称与架构",
                value = platform.nameAndArch,
            ),
            RuntimeInfoItem(
                label = "架构族",
                value = platform.arch.family.name,
            ),
            RuntimeInfoItem(
                label = "地址位数",
                value = platform.arch.addressSizeBits.toString(),
            ),
            RuntimeInfoItem(
                label = "是否 64 位",
                value = platform.is64bit().toDisplayText(),
            ),
            RuntimeInfoItem(
                label = "是否 AArch",
                value = platform.isAArch().toDisplayText(),
            ),
            RuntimeInfoItem(
                label = "是否移动端",
                value = platform.isMobile().toDisplayText(),
            ),
            RuntimeInfoItem(
                label = "是否桌面端",
                value = platform.isDesktop().toDisplayText(),
            ),
        ),
    )
}

/**
 * 创建应用构建配置分组。
 *
 * @param appBuildConfig 当前应用构建配置。
 * @return 应用构建配置分组。
 */
private fun createAppBuildConfigSection(appBuildConfig: AppBuildConfig): RuntimeInfoSection {
    val baseItems = listOf(
        RuntimeInfoItem(
            label = "应用 ID",
            value = appBuildConfig.id,
        ),
        RuntimeInfoItem(
            label = "版本名",
            value = appBuildConfig.versionName,
        ),
        RuntimeInfoItem(
            label = "Properties 数量",
            value = appBuildConfig.properties.size.toString(),
        ),
    )
    val propertyItems = appBuildConfig.properties.entries
        .sortedBy { it.key }
        .map { (key, value) ->
            RuntimeInfoItem(
                label = "property.$key",
                value = maskSensitiveValue(key = key, value = value),
            )
        }

    return RuntimeInfoSection(
        title = "构建配置",
        items = baseItems + propertyItems,
    )
}

/**
 * 创建应用目录分组。
 *
 * @param contextFiles 当前平台上下文目录集合。
 * @return 应用目录分组。
 */
private fun createContextFilesSection(contextFiles: ContextFiles): RuntimeInfoSection {
    return RuntimeInfoSection(
        title = "应用目录",
        items = listOf(
            createDirectoryItem(
                label = "缓存目录",
                file = contextFiles.cacheDir,
            ),
            createDirectoryItem(
                label = "数据目录",
                file = contextFiles.dataDir,
            ),
            createDirectoryItem(
                label = "默认媒体缓存目录",
                file = contextFiles.defaultBaseMediaCacheDir,
            ),
        ),
    )
}

/**
 * 创建目录状态展示项。
 *
 * @param label 展示名称。
 * @param file 目录文件对象。
 * @return 目录状态展示项。
 */
private fun createDirectoryItem(
    label: String,
    file: File,
): RuntimeInfoItem {
    return RuntimeInfoItem(
        label = label,
        value = listOf(
            "路径：${file.absolutePath}",
            "存在：${file.exists().toDisplayText()}",
            "目录：${file.isDirectory.toDisplayText()}",
            "可读：${file.canRead().toDisplayText()}",
            "可写：${file.canWrite().toDisplayText()}",
        ).joinToString(separator = "\n"),
    )
}

/**
 * 根据配置键名脱敏敏感配置值。
 *
 * @param key 配置键名。
 * @param value 配置值。
 * @return 可展示的配置值。
 */
private fun maskSensitiveValue(
    key: String,
    value: String,
): String {
    val normalizedKey = key.lowercase()
    val isSensitive = SensitivePropertyKeywords.any(normalizedKey::contains)
    return when {
        !isSensitive -> value
        value.isBlank() -> ""
        value.length <= VisibleSecretEdgeLength * 2 -> "****"
        else -> "${value.take(VisibleSecretEdgeLength)}****${value.takeLast(VisibleSecretEdgeLength)}"
    }
}

/**
 * 将布尔值转换为页面展示文本。
 *
 * @receiver 待展示的布尔值。
 * @return 中文展示文本。
 */
private fun Boolean.toDisplayText(): String = if (this) "是" else "否"

/**
 * 运行环境信息页面的分组模型。
 *
 * @property title 分组标题。
 * @property items 分组内的参数项。
 */
@Immutable
private data class RuntimeInfoSection(
    val title: String,
    val items: List<RuntimeInfoItem>,
)

/**
 * 运行环境信息页面的参数项模型。
 *
 * @property label 参数名称。
 * @property value 参数值。
 */
@Immutable
private data class RuntimeInfoItem(
    val label: String,
    val value: String,
)

/**
 * 敏感配置键名片段。
 */
private val SensitivePropertyKeywords = listOf("key", "token", "secret", "password")

/**
 * 敏感配置明文边缘保留长度。
 */
private const val VisibleSecretEdgeLength = 4
