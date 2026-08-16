package ciyin.file.downloader

import ciyin.file.downloader.core.DownloadManager
import ciyin.file.downloader.core.DownloadState
import ciyin.file.downloader.core.DownloadTaskState
import ciyin.file.downloader.core.FileDownloader
import ciyin.file.downloader.model.DownloadConfig
import ciyin.file.downloader.model.QueueTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob

/**
 * 创建一个 [FileDownloader] 实例。
 *
 * 这是一个工厂函数，用于获取文件下载器的具体实现。
 *
 * ### 使用示例
 *
 * ```kotlin
 * // 在 CoroutineScope 中执行
 * val scope = CoroutineScope(Dispatchers.Default)
 *
 * // 1. 创建 FileDownloader 实例
 * val downloader = fileDownloader()
 *
 * // 2. 配置下载任务
 * val config = DownloadConfig(
 *     url = "https://example.com/archive.zip",         // 下载链接
 *     savePath = "/path/to/save/archive.zip",          // 保存路径
 *     enableResume = true,                             // 开启断点续传
 *     maxRetries = 3,                                  // 下载失败时最多重试3次
 *     retryDelayMs = 2000L                             // 每次重试间隔2秒
 * )
 *
 * // 3. 监听下载状态
 * scope.launch {
 *     downloader.state.collect { state ->
 *         when (state) {
 *             DownloadState.Idle -> println("下载任务已创建，等待开始...")
 *             DownloadState.Start -> println("下载任务开始初始化")
 *             is DownloadState.Downloading -> {
 *                 val progress = (state.progress * 100).toInt()
 *                 println("下载中... 进度: $progress%, 速度: ${state.speed / 1024} KB/s")
 *             }
 *             DownloadState.Paused -> println("下载已暂停")
 *             is DownloadState.Complete -> {
 *                 println("下载成功！文件保存在: ${state.filePath}")
 *                 // 下载成功后可以关闭并清理资源
 *                 downloader.cleanup()
 *             }
 *             is DownloadState.Error -> {
 *                 println("下载失败: ${state.message}")
 *                 downloader.cleanup()
 *             }
 *             DownloadState.Cancelled -> println("下载已取消")
 *             DownloadState.Resumed -> println("下载已恢复")
 *         }
 *     }
 * }
 *
 * // 4. 启动下载
 * scope.launch {
 *     downloader.download(config)
 * }
 *
 * // 5. 控制下载（在需要时调用）
 * downloader.pause()
 * scope.launch { downloader.resume() }
 * downloader.cancel()
 *
 * // 6. 程序退出或不再需要下载器时，清理资源
 * downloader.cleanup()
 * ```
 *
 * @return [FileDownloader] 的一个新实例。
 * @param scope 下载任务使用的协程作用域。
 * @see FileDownloader
 * @see DownloadConfig
 * @see DownloadState
 */
expect fun fileDownloader(scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)): FileDownloader

/**
 * 创建一个 [DownloadManager] 实例。
 *
 * 这是一个工厂函数，用于获取下载管理器的具体实现。
 * DownloadManager 用于管理多个下载任务，支持任务队列、并发控制、优先级设置等功能。
 *
 * ### 使用示例
 *
 * ```kotlin
 * // 在 CoroutineScope 中执行
 * val scope = CoroutineScope(Dispatchers.IO)
 *
 * // 1. 创建 DownloadManager 实例（最大并发数为3）
 * val manager = downloadManager(
 *     maxConcurrentDownloads = 3,
 *     scope = scope
 * )
 *
 * // 2. 配置多个下载任务
 * val config1 = DownloadConfig(
 *     id = 1,
 *     url = "https://example.com/file1.zip",
 *     savePath = "/path/to/save/file1.zip",
 *     enableResume = true,
 *     maxRetries = 3,
 *     retryDelayMs = 2000L,
 *     enableDownloadProgress = true
 * )
 *
 * val config2 = DownloadConfig(
 *     id = 2,
 *     url = "https://example.com/file2.zip",
 *     savePath = "/path/to/save/file2.zip",
 *     enableResume = true,
 *     maxRetries = 3,
 *     retryDelayMs = 2000L
 * )
 *
 * // 3. 添加任务到队列（可设置优先级，数值越大优先级越高）
 * manager.addToQueue(config1, priority = 10)
 * manager.addToQueue(config2, priority = 5)
 *
 * // 4. 监听队列状态
 * scope.launch {
 *     manager.queueState.collect { queue ->
 *         println("当前队列任务数: ${queue.size}")
 *         queue.forEach { task ->
 *             println("任务ID: ${task.taskId}, 优先级: ${task.priority}")
 *         }
 *     }
 * }
 *
 * // 5. 监听任务事件
 * scope.launch {
 *     manager.taskEvents.collect { event ->
 *         when (event) {
 *             is DownloadTaskState.Create -> println("任务创建: ${event.taskId}")
 *             is DownloadTaskState.AddToQueue -> println("任务加入队列: ${event.queueTask.taskId}")
 *             is DownloadTaskState.Start -> println("任务开始: ${event.queueTask.taskId}")
 *             is DownloadTaskState.DownloadState -> {
 *                 val state = event.downloadState
 *                 if (state is DownloadState.Downloading) {
 *                     val progress = (state.progress * 100).toInt()
 *                     println("任务 ${event.queueTask.taskId} 下载中... 进度: $progress%")
 *                 }
 *             }
 *             is DownloadTaskState.Pause -> println("任务暂停: ${event.queueTask.taskId}")
 *             is DownloadTaskState.Resume -> println("任务恢复: ${event.queueTask.taskId}")
 *             is DownloadTaskState.Cancel -> println("任务取消: ${event.queueTask.taskId}")
 *             is DownloadTaskState.RemoveQueue -> println("任务移出队列: ${event.queueTask.taskId}")
 *             DownloadTaskState.ClearQueue -> println("队列已清空")
 *             DownloadTaskState.AllTasksCompleted -> println("所有任务已完成")
 *         }
 *     }
 * }
 *
 * // 6. 启动队列处理（按优先级和并发数自动执行）
 * manager.startQueue()
 *
 * // 7. 单独控制某个任务
 * manager.pause(taskId = 1)
 * manager.resume(taskId = 1)
 * manager.restart(taskId = 1)
 * manager.cancel(taskId = 1)
 *
 * // 8. 获取指定任务的下载状态
 * scope.launch {
 *     manager.getDownloadState(taskId = 1)?.collect { state ->
 *         when (state) {
 *             is DownloadState.Complete -> println("任务1下载完成")
 *             is DownloadState.Error -> println("任务1下载失败: ${state.message}")
 *             else -> {}
 *         }
 *     }
 * }
 *
 * // 9. 队列管理操作
 * manager.removeFromQueue(taskId = 2)  // 从队列中移除指定任务
 * manager.clearQueue()                  // 清空整个队列
 *
 * // 10. 批量操作
 * manager.cancelAll()  // 取消所有正在进行的下载任务
 *
 * // 11. 程序退出或不再需要管理器时，清理资源
 * manager.cleanup()
 * ```
 *
 * @return [DownloadManager] 的一个新实例。
 * @param maxConcurrentDownloads 最大并发下载数。
 * @param scope 下载管理器使用的协程作用域。
 * @see DownloadManager
 * @see DownloadConfig
 * @see DownloadTaskState
 * @see QueueTask
 */
fun downloadManager(
    maxConcurrentDownloads: Int = 3,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
): DownloadManager = DownloadManager(
    maxConcurrentDownloads = maxConcurrentDownloads,
    scope = scope,
)
