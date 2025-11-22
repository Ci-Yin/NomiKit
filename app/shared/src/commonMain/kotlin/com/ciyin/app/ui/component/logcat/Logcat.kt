package com.ciyin.app.ui.component.logcat


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/10/24 14:35
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// 日志级别枚举
enum class LogLevel(val displayName: String, val shortName: String) {
    All("All", "ALL"),
    Verbose("Verbose", "V"),
    Debug("Debug", "D"),
    Info("Info", "I"),
    Warn("Warn", "W"),
    Error("Error", "E"),
    Assert("Assert", "A")
}

// 日志数据模型
@OptIn(ExperimentalTime::class)
data class LogItem(
    val timestamp: String,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val id: String = "${Clock.System.now().toEpochMilliseconds()}-${(0..999999).random()}"
)

// 折叠组数据模型
private data class CollapsedGroup(
    val tag: String,
    val level: LogLevel,
    val startIndex: Int,
    val endIndex: Int,
    val logs: List<LogItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Logcat(
    logs: List<LogItem>,
    modifier: Modifier = Modifier,
    initialLevel: LogLevel = LogLevel.All,
    autoScrollEnabled: Boolean = true,
    onClear: () -> Unit = {},
    onLevelChange: (LogLevel) -> Unit = {},
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    var selectedLevel by remember { mutableStateOf(initialLevel) }
    var searchQuery by remember { mutableStateOf("") }
    var autoScroll by remember { mutableStateOf(autoScrollEnabled) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    // 折叠状态管理
    val collapsedStates = remember { mutableStateMapOf<String, Boolean>() }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 过滤日志
    val filteredLogs = remember(logs, selectedLevel, searchQuery) {
        logs.filter { log ->
            val levelMatch = selectedLevel == LogLevel.All || log.level == selectedLevel
            val searchMatch = searchQuery.isEmpty() ||
                    log.message.contains(searchQuery, ignoreCase = true) ||
                    log.tag.contains(searchQuery, ignoreCase = true)
            levelMatch && searchMatch
        }
    }

    // 处理折叠逻辑：连续相同 tag 和 level 的日志大于 3 条时折叠
    val displayItems = remember(filteredLogs, collapsedStates) {
        val items = mutableListOf<Any>()
        var i = 0

        while (i < filteredLogs.size) {
            val current = filteredLogs[i]
            var j = i + 1

            // 查找连续相同 tag 和 level 的日志
            while (j < filteredLogs.size &&
                filteredLogs[j].tag == current.tag &&
                filteredLogs[j].level == current.level
            ) {
                j++
            }

            val count = j - i
            if (count >= 3) {
                // 可折叠的组
                val groupKey = "${current.tag}-${current.level}-$i"
                val group = CollapsedGroup(
                    tag = current.tag,
                    level = current.level,
                    startIndex = i,
                    endIndex = j - 1,
                    logs = filteredLogs.subList(i, j)
                )

                items.add(group)
                i = j
            } else {
                // 不足 3 条，直接显示
                items.add(current)
                i++
            }
        }

        items
    }

    // 自动滚动到底部
    LaunchedEffect(logs.size, autoScroll) {
        if (autoScroll && logs.isNotEmpty()) {
            listState.animateScrollToItem(displayItems.size)
        }
    }

    // 检测用户手动滚动
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            if (lastVisibleItem < displayItems.size - 1) {
                autoScroll = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (darkTheme) Color(0xFF1E1E1E) else Color(0xFFFAFAFA))
    ) {
        // 顶部工具栏
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 日志级别过滤
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { filterMenuExpanded = true },
                        label = { Text(selectedLevel.displayName) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false }
                    ) {
                        LogLevel.values().forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level.displayName) },
                                onClick = {
                                    selectedLevel = level
                                    onLevelChange(level)
                                    filterMenuExpanded = false
                                },
                                leadingIcon = if (selectedLevel == level) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }

                // 搜索框
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search in logs...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // 滚动到底部
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(displayItems.size)
                        }
                    }
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, "Scroll to bottom")
                }

                // 自动滚动切换
                IconButton(
                    onClick = { autoScroll = !autoScroll }
                ) {
                    Icon(
                        if (autoScroll) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        "Auto scroll",
                        tint = if (autoScroll) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }

                // 清空日志
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, "Clear logs")
                }

                // 日志数量
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${filteredLogs.size} 条",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // 日志列表
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(displayItems, key = { item ->
                when (item) {
                    is CollapsedGroup -> "${item.tag}-${item.level}-${item.startIndex}"
                    is LogItem -> item.id
                    else -> item.hashCode()
                }
            }) { item ->
                when (item) {
                    is CollapsedGroup -> {
                        CollapsedGroupItem(
                            group = item,
                            isExpanded = collapsedStates["${item.tag}-${item.level}-${item.startIndex}"]
                                ?: false,
                            onToggle = {
                                val key = "${item.tag}-${item.level}-${item.startIndex}"
                                collapsedStates[key] = !(collapsedStates[key] ?: false)
                            },
                            darkTheme = darkTheme
                        )
                    }

                    is LogItem -> {
                        LogItemRow(log = item, darkTheme = darkTheme)
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsedGroupItem(
    group: CollapsedGroup,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    darkTheme: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        // 折叠标题行
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle),
            color = if (darkTheme) Color(0xFF2D2D2D) else Color(0xFFEEEEEE),
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 展开/折叠图标
                val rotation by animateFloatAsState(if (isExpanded) 90f else 0f)
                Icon(
                    Icons.Default.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 日志级别标签
                LogLevelBadge(level = group.level)

                // Tag
                Text(
                    text = group.tag,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // 数量
                Text(
                    text = "(${group.logs.size} 条)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.weight(1f))

                // 最后一条日志预览
                Text(
                    text = group.logs.last().message,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(2f, fill = false)
                )
            }
        }

        // 展开的日志内容
        AnimatedVisibility(visible = isExpanded) {
            Column {
                group.logs.forEach { log ->
                    LogItemRow(log = log, darkTheme = darkTheme, isInGroup = true)
                }
            }
        }
    }
}

@Composable
private fun LogItemRow(
    log: LogItem,
    darkTheme: Boolean,
    isInGroup: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isInGroup) 32.dp else 8.dp,
                end = 8.dp,
                top = 2.dp,
                bottom = 2.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 时间戳
        Text(
            text = log.timestamp,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = if (darkTheme) Color(0xFF808080) else Color(0xFF666666),
            modifier = Modifier.width(90.dp)
        )

        // 日志级别
        LogLevelBadge(level = log.level)

        // Tag
        Text(
            text = log.tag,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(120.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 消息内容
        Text(
            text = log.message,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = getLogLevelColor(log.level, darkTheme),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LogLevelBadge(level: LogLevel) {
    val (bgColor, textColor) = when (level) {
        LogLevel.Verbose -> Color(0xFF757575) to Color.White
        LogLevel.Debug -> Color(0xFF2196F3) to Color.White
        LogLevel.Info -> Color(0xFF4CAF50) to Color.White
        LogLevel.Warn -> Color(0xFFFF9800) to Color.White
        LogLevel.Error -> Color(0xFFF44336) to Color.White
        LogLevel.Assert -> Color(0xFF9C27B0) to Color.White
        else -> Color.Gray to Color.White
    }

    Surface(
        shape = RoundedCornerShape(3.dp),
        color = bgColor,
        modifier = Modifier.size(width = 18.dp, height = 18.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = level.shortName,
                fontSize = 10.sp,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getLogLevelColor(level: LogLevel, darkTheme: Boolean): Color {
    return when (level) {
        LogLevel.Verbose -> if (darkTheme) Color(0xFFBBBBBB) else Color(0xFF757575)
        LogLevel.Debug -> if (darkTheme) Color(0xFF64B5F6) else Color(0xFF1976D2)
        LogLevel.Info -> if (darkTheme) Color(0xFF81C784) else Color(0xFF388E3C)
        LogLevel.Warn -> if (darkTheme) Color(0xFFFFB74D) else Color(0xFFF57C00)
        LogLevel.Error -> if (darkTheme) Color(0xFFE57373) else Color(0xFFD32F2F)
        LogLevel.Assert -> if (darkTheme) Color(0xFFBA68C8) else Color(0xFF7B1FA2)
        else -> if (darkTheme) Color.White else Color.Black
    }
}