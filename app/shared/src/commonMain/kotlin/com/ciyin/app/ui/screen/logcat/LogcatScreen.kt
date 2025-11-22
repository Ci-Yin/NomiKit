package com.ciyin.app.ui.screen.logcat


import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.platform.Log
import com.ciyin.app.ui.component.AppPreview
import com.ciyin.app.ui.component.Screen
import com.ciyin.app.ui.component.common.Toolbar
import com.ciyin.app.util.value
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview
import rpa.app.shared.generated.resources.Res
import rpa.app.shared.generated.resources.logcat_default_log
import rpa.app.shared.generated.resources.logcat_screen_title


/**
 *
 * 日志查看界面
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/11 上午12:29
 */

/**
 * 日志界面预览
 * 用于 Compose Preview，包含测试日志数据
 */
@AppPreview
@Composable
fun LogcatScreenPreview() = AppPreview {
    // 测试日志文本
    var logText by remember { mutableStateOf("这是测试日志\n第二行日志\n第三行日志") }

    LogcatContent(
        logText = logText,
        onClearLog = { logText = "" }
    )
}

/**
 * 日志主界面
 * 连接全局日志状态和 UI 的桥接层
 */
@Composable
fun LogcatScreen() {

    var appLog by remember { mutableStateOf(Res.string.logcat_default_log.value) }

    LaunchedEffect(Unit) {
        Log.log.collect { log ->
            appLog = appLog + log + "\n"
        }
    }

    LogcatContent(
        logText = appLog,
        onClearLog = { appLog = "" }
    )

}

/**
 * 日志内容组件
 * 纯 UI 组件，显示日志文本和清空功能
 *
 * @param logText 日志文本内容
 * @param onClearLog 清空日志回调
 */
@Composable
private fun LogcatContent(
    logText: String,
    onClearLog: () -> Unit
) = Screen(
    title = stringResource(Res.string.logcat_screen_title),
    maxWidth = null,
    toolbar = {
        // 顶部工具栏
        Toolbar("清空日志记录", onClearLog)
    }
) {
    Column {
        // 日志显示区域
        Logcat(logText)
    }
}

/**
 * 日志显示组件
 * 功能强大的日志输出组件，支持多种日志级别、过滤、搜索、自动滚动等功能
 *
 * @param logText 要显示的日志文本
 */
@Composable
private fun Logcat(logText: String) {
    // 日志级别过滤状态
    var selectedLevel by remember { mutableStateOf(LogLevel.ALL) }
    // 搜索关键词
    var searchText by remember { mutableStateOf("") }
    // 是否自动滚动到底部
    var autoScroll by remember { mutableStateOf(true) }
    // 是否显示时间戳
    var showTimestamp by remember { mutableStateOf(true) }
    // 垂直滚动状态
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // 解析日志文本为日志条目列表
    val logEntries = remember(logText) {
        parseLogText(logText)
    }

    // 过滤后的日志
    val filteredLogs = remember(logEntries, selectedLevel, searchText) {
        logEntries.filter { entry ->
            val levelMatch = selectedLevel == LogLevel.ALL || entry.level == selectedLevel
            val searchMatch = searchText.isEmpty() ||
                    entry.message.contains(searchText, ignoreCase = true) ||
                    entry.tag.contains(searchText, ignoreCase = true)
            levelMatch && searchMatch
        }
    }

    // 自动滚动到底部
    LaunchedEffect(logText, autoScroll) {
        if (autoScroll) {
            verticalScrollState.animateScrollTo(verticalScrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 工具栏：过滤和控制选项
        LogcatToolbar(
            selectedLevel = selectedLevel,
            onLevelChange = { selectedLevel = it },
            searchText = searchText,
            onSearchChange = { searchText = it },
            autoScroll = autoScroll,
            onAutoScrollChange = { autoScroll = it },
            showTimestamp = showTimestamp,
            onShowTimestampChange = { showTimestamp = it },
            logCount = filteredLogs.size,
            totalCount = logEntries.size
        )

        // 日志内容区域
        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1E1E))
                    .verticalScroll(verticalScrollState)
                    .horizontalScroll(horizontalScrollState)
                    .padding(10.dp)
            ) {
                filteredLogs.forEach { entry ->
                    LogEntry(
                        entry = entry,
                        showTimestamp = showTimestamp,
                        searchText = searchText
                    )
                }
            }
        }
    }
}

/**
 * 日志级别枚举
 */
enum class LogLevel(val displayName: String, val color: Color) {
    ALL("全部", Color.White),
    VERBOSE("详细", Color(0xFF888888)),
    DEBUG("调试", Color(0xFF2196F3)),
    INFO("信息", Color(0xFF4CAF50)),
    WARN("警告", Color(0xFFFFC107)),
    ERROR("错误", Color(0xFFF44336)),
    FATAL("致命", Color(0xFFD32F2F))
}

/**
 * 日志条目数据类
 */
data class LogEntry(
    val timestamp: String,
    val level: LogLevel,
    val tag: String,
    val message: String
)

/**
 * 日志工具栏组件
 */
@Composable
private fun LogcatToolbar(
    selectedLevel: LogLevel,
    onLevelChange: (LogLevel) -> Unit,
    searchText: String,
    onSearchChange: (String) -> Unit,
    autoScroll: Boolean,
    onAutoScrollChange: (Boolean) -> Unit,
    showTimestamp: Boolean,
    onShowTimestampChange: (Boolean) -> Unit,
    logCount: Int,
    totalCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(8.dp)
    ) {
        // 第一行：日志级别过滤
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("级别:", fontSize = 12.sp)
            LogLevel.entries.forEach { level ->
                FilterChip(
                    selected = selectedLevel == level,
                    onClick = { onLevelChange(level) },
                    label = { Text(level.displayName, fontSize = 11.sp) },
                    leadingIcon = if (selectedLevel == level) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 第二行：搜索和选项
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 搜索框
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchChange,
                placeholder = { Text("搜索日志...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                trailingIcon = if (searchText.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(18.dp))
                        }
                    }
                } else null,
                modifier = Modifier.weight(1f).height(40.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                singleLine = true
            )

            // 自动滚动开关
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = autoScroll,
                    onCheckedChange = onAutoScrollChange,
                    modifier = Modifier.size(20.dp)
                )
                Text("自动滚动", fontSize = 11.sp)
            }

            // 显示时间戳开关
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = showTimestamp,
                    onCheckedChange = onShowTimestampChange,
                    modifier = Modifier.size(20.dp)
                )
                Text("时间戳", fontSize = 11.sp)
            }

            // 日志计数
            Text(
                text = "$logCount / $totalCount",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * 单条日志条目组件
 */
@Composable
private fun LogEntry(
    entry: LogEntry,
    showTimestamp: Boolean,
    searchText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        // 时间戳
        if (showTimestamp) {
            Text(
                text = entry.timestamp,
                fontSize = 11.sp,
                color = Color(0xFF888888),
                modifier = Modifier.width(90.dp)
            )
            Spacer(Modifier.width(8.dp))
        }

        // 日志级别标签
        Text(
            text = entry.level.displayName,
            fontSize = 11.sp,
            color = entry.level.color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
        Spacer(Modifier.width(8.dp))

        // Tag
        Text(
            text = entry.tag,
            fontSize = 11.sp,
            color = Color(0xFF64B5F6),
            modifier = Modifier.widthIn(max = 150.dp)
        )
        Spacer(Modifier.width(8.dp))

        // 日志消息（高亮搜索关键词）
        if (searchText.isNotEmpty() && entry.message.contains(searchText, ignoreCase = true)) {
            HighlightedText(
                text = entry.message,
                highlight = searchText,
                normalColor = Color.White,
                highlightColor = Color(0xFFFFEB3B),
                fontSize = 12.sp
            )
        } else {
            Text(
                text = entry.message,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

/**
 * 高亮文本组件
 * 在文本中高亮显示指定的关键词
 */
@Composable
private fun HighlightedText(
    text: String,
    highlight: String,
    normalColor: Color,
    highlightColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        val lowerText = text.lowercase()
        val lowerHighlight = highlight.lowercase()

        while (lastIndex < text.length) {
            val index = lowerText.indexOf(lowerHighlight, lastIndex)
            if (index == -1) {
                withStyle(style = SpanStyle(color = normalColor)) {
                    append(text.substring(lastIndex))
                }
                break
            } else {
                withStyle(style = SpanStyle(color = normalColor)) {
                    append(text.substring(lastIndex, index))
                }
                withStyle(
                    style = SpanStyle(
                        color = Color.Black,
                        background = highlightColor,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                ) {
                    append(text.substring(index, index + highlight.length))
                }
                lastIndex = index + highlight.length
            }
        }
    }

    Text(
        text = annotatedString,
        fontSize = fontSize
    )
}

/**
 * 解析日志文本为日志条目列表
 * 支持多种日志格式的解析
 */
private fun parseLogText(logText: String): List<LogEntry> {
    if (logText.isEmpty()) return emptyList()

    val lines = logText.split("\n")
    val entries = mutableListOf<LogEntry>()

    // Android Logcat 格式正则: 时间 级别/标签: 消息
    val logcatPattern =
        Regex("""(\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}\.\d{3})\s+([VDIWEF])/([^:]+):\s*(.+)""")
    // 简单格式正则: [级别] 消息
    val simplePattern = Regex("""\[([VDIWEF])\]\s*(.+)""")

    lines.forEach { line ->
        if (line.isBlank()) return@forEach

        // 尝试匹配 Android Logcat 格式
        val logcatMatch = logcatPattern.find(line)
        if (logcatMatch != null) {
            val (timestamp, levelChar, tag, message) = logcatMatch.destructured
            entries.add(
                LogEntry(
                    timestamp = timestamp,
                    level = parseLevelFromChar(levelChar),
                    tag = tag.trim(),
                    message = message.trim()
                )
            )
            return@forEach
        }

        // 尝试匹配简单格式
        val simpleMatch = simplePattern.find(line)
        if (simpleMatch != null) {
            val (levelChar, message) = simpleMatch.destructured
            entries.add(
                LogEntry(
                    timestamp = getCurrentTimestamp(),
                    level = parseLevelFromChar(levelChar),
                    tag = "App",
                    message = message.trim()
                )
            )
            return@forEach
        }

        // 默认作为 INFO 级别处理
        entries.add(
            LogEntry(
                timestamp = getCurrentTimestamp(),
                level = LogLevel.INFO,
                tag = "App",
                message = line.trim()
            )
        )
    }

    return entries
}

/**
 * 从字符解析日志级别
 */
private fun parseLevelFromChar(char: String): LogLevel {
    return when (char.uppercase()) {
        "V" -> LogLevel.VERBOSE
        "D" -> LogLevel.DEBUG
        "I" -> LogLevel.INFO
        "W" -> LogLevel.WARN
        "E" -> LogLevel.ERROR
        "F" -> LogLevel.FATAL
        else -> LogLevel.INFO
    }
}

/**
 * 获取当前时间戳字符串
 */
private fun getCurrentTimestamp(): String {
    // 简单的时间戳格式，实际应用中应使用系统时间
    return "00:00:00.000"
}