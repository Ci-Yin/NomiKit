package com.ciyin.app.ui.component.logcat

//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.delay
//import kotlinx.datetime.DateTimeUnit
//import kotlinx.datetime.LocalDateTime
//import kotlinx.datetime.LocalTime
//
//
///**
// * Logcat 组件的完整测试演示
// * 展示了所有功能的使用场景
// */
//@Composable
//fun LogcatDemo() {
//    var logs by remember { mutableStateOf(listOf<LogItem>()) }
//    var isAutoGenerating by remember { mutableStateOf(false) }
//    var darkTheme by remember { mutableStateOf(false) }
//    val coroutineScope = rememberCoroutineScope()
//
//    MaterialTheme(
//        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            // 测试控制面板
//            TestControlPanel(
//                onAddSingleLog = { level, tag, message ->
//                    logs = logs + createLogItem(level, tag, message)
//                },
//                onAddBatch = { count ->
//                    logs = logs + generateBatchLogs(count)
//                },
//                onToggleAutoGenerate = {
//                    isAutoGenerating = !isAutoGenerating
//                },
//                isAutoGenerating = isAutoGenerating,
//                onToggleTheme = { darkTheme = !darkTheme },
//                isDarkTheme = darkTheme,
//                onClearAll = { logs = emptyList() }
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Logcat 组件
//            Logcat(
//                logs = logs,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f),
//                initialLevel = LogLevel.All,
//                autoScrollEnabled = true,
//                onClear = { logs = emptyList() },
//                onLevelChange = { level ->
//                    println("日志级别切换到: ${level.displayName}")
//                },
//                darkTheme = darkTheme
//            )
//        }
//    }
//
//    // 自动生成日志
//    LaunchedEffect(isAutoGenerating) {
//        while (isAutoGenerating) {
//            delay(800) // 每 800ms 生成一条日志
//            logs = logs + generateRandomLog()
//        }
//    }
//}
//
//@Composable
//private fun TestControlPanel(
//    onAddSingleLog: (LogLevel, String, String) -> Unit,
//    onAddBatch: (Int) -> Unit,
//    onToggleAutoGenerate: () -> Unit,
//    isAutoGenerating: Boolean,
//    onToggleTheme: () -> Unit,
//    isDarkTheme: Boolean,
//    onClearAll: () -> Unit
//) {
//    Surface(
//        tonalElevation = 2.dp,
//        shadowElevation = 4.dp,
//        shape = MaterialTheme.shapes.medium
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Text(
//                text = "测试控制面板",
//                style = MaterialTheme.typography.titleMedium
//            )
//
//            // 第一行：快速添加不同级别的日志
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Button(
//                    onClick = {
//                        onAddSingleLog(
//                            LogLevel.Verbose,
//                            "System",
//                            "这是一条 Verbose 日志"
//                        )
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = androidx.compose.ui.graphics.Color(
//                            0xFF757575
//                        )
//                    ),
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("V", fontSize = 12.sp)
//                }
//                Button(
//                    onClick = {
//                        onAddSingleLog(
//                            LogLevel.Debug,
//                            "Network",
//                            "HTTP 请求成功: 200 OK"
//                        )
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = androidx.compose.ui.graphics.Color(
//                            0xFF2196F3
//                        )
//                    ),
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("D", fontSize = 12.sp)
//                }
//                Button(
//                    onClick = { onAddSingleLog(LogLevel.Info, "MainActivity", "Activity 已创建") },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = androidx.compose.ui.graphics.Color(
//                            0xFF4CAF50
//                        )
//                    ),
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("I", fontSize = 12.sp)
//                }
//                Button(
//                    onClick = { onAddSingleLog(LogLevel.Warn, "Database", "查询耗时过长: 1523ms") },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = androidx.compose.ui.graphics.Color(
//                            0xFFFF9800
//                        )
//                    ),
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("W", fontSize = 12.sp)
//                }
//                Button(
//                    onClick = {
//                        onAddSingleLog(
//                            LogLevel.Error,
//                            "Network",
//                            "连接失败: TimeoutException"
//                        )
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = androidx.compose.ui.graphics.Color(
//                            0xFFF44336
//                        )
//                    ),
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("E", fontSize = 12.sp)
//                }
//                Button(
//                    onClick = {
//                        onAddSingleLog(
//                            LogLevel.Assert,
//                            "System",
//                            "断言失败: value != null"
//                        )
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = androidx.compose.ui.graphics.Color(
//                            0xFF9C27B0
//                        )
//                    ),
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("A", fontSize = 12.sp)
//                }
//            }
//
//            // 第二行：批量操作
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Button(
//                    onClick = { onAddBatch(10) },
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("添加 10 条")
//                }
//                Button(
//                    onClick = { onAddBatch(50) },
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("添加 50 条")
//                }
//                Button(
//                    onClick = { onAddBatch(100) },
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("添加 100 条")
//                }
//            }
//
//            // 第三行：测试折叠功能
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Button(
//                    onClick = {
//                        // 添加连续相同的日志以测试折叠
//                        repeat(5) {
//                            onAddSingleLog(
//                                LogLevel.Debug,
//                                "Network",
//                                "重复日志 $it - 用于测试折叠功能"
//                            )
//                        }
//                    },
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("测试折叠")
//                }
//                Button(
//                    onClick = {
//                        // 添加多组不同的折叠日志
//                        repeat(5) {
//                            onAddSingleLog(LogLevel.Info, "Database", "数据库操作 $it")
//                        }
//                        repeat(4) {
//                            onAddSingleLog(LogLevel.Warn, "Cache", "缓存命中 $it")
//                        }
//                        repeat(6) {
//                            onAddSingleLog(LogLevel.Error, "Network", "网络错误 $it")
//                        }
//                    },
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("多组折叠")
//                }
//            }
//
//            // 第四行：其他控制
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Button(
//                    onClick = onToggleAutoGenerate,
//                    colors = if (isAutoGenerating) {
//                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
//                    } else {
//                        ButtonDefaults.buttonColors()
//                    },
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text(if (isAutoGenerating) "停止自动生成" else "开始自动生成")
//                }
//
//                Button(
//                    onClick = onToggleTheme,
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text(if (isDarkTheme) "浅色主题" else "深色主题")
//                }
//
//                Button(
//                    onClick = onClearAll,
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.errorContainer,
//                        contentColor = MaterialTheme.colorScheme.onErrorContainer
//                    ),
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("清空所有")
//                }
//            }
//        }
//    }
//}
//
//// 创建单条日志
//private fun createLogItem(level: LogLevel, tag: String, message: String): LogItem {
//    return LogItem(
//        timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")),
//        level = level,
//        tag = tag,
//        message = message
//    )
//}
//
//// 生成随机日志
//private fun generateRandomLog(): LogItem {
//    val levels = listOf(
//        LogLevel.Verbose,
//        LogLevel.Debug,
//        LogLevel.Info,
//        LogLevel.Warn,
//        LogLevel.Error,
//        LogLevel.Assert
//    )
//    val tags = listOf(
//        "Network",
//        "Database",
//        "UI",
//        "System",
//        "Cache",
//        "MainActivity",
//        "Fragment",
//        "ViewModel",
//        "Repository"
//    )
//    val messages = listOf(
//        "初始化完成",
//        "HTTP 请求开始: GET /api/users",
//        "数据加载成功: 42 条记录",
//        "缓存命中率: 87.5%",
//        "渲染耗时: 16ms",
//        "内存使用: 128MB / 512MB",
//        "连接超时，正在重试...",
//        "用户点击了按钮",
//        "Fragment 生命周期: onResume",
//        "数据库查询: SELECT * FROM users WHERE id = ?",
//        "WebSocket 连接已建立",
//        "图片加载完成: https://example.com/image.jpg",
//        "位置更新: 纬度 35.6762, 经度 139.6503",
//        "蓝牙设备已连接: Device-001",
//        "传感器数据: 加速度 9.81 m/s²"
//    )
//
//    return LogItem(
//        timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")),
//        level = levels.random(),
//        tag = tags.random(),
//        message = messages.random()
//    )
//}
//
//// 批量生成日志
//private fun generateBatchLogs(count: Int): List<LogItem> {
//    return List(count) { index ->
//        val level = LogLevel.entries[index % 6 + 1] // 跳过 All
//        val tag = listOf("Network", "Database", "UI", "System")[index % 4]
//
//        LogItem(
//            timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")),
//            level = level,
//            tag = tag,
//            message = "批量生成的日志 #$index - ${generateRandomMessage()}"
//        )
//    }
//}
//
//private fun generateRandomMessage(): String {
//    val templates = listOf(
//        "处理请求耗时: ${(10..500).random()}ms",
//        "内存分配: ${(1..100).random()}MB",
//        "线程池状态: ${(1..10).random()}/${(10..20).random()} 活跃",
//        "缓存大小: ${(100..9999).random()} 条目",
//        "FPS: ${(30..60).random()}",
//        "网络延迟: ${(10..200).random()}ms"
//    )
//    return templates.random()
//}
//
//// 用于预览的组合函数
//@Composable
//fun LogcatDemoPreview() {
//    LogcatDemo()
//}