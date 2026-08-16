package ciyin.file.downloader

import ciyin.file.downloader.core.FileDownloader
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineScope

/** 使用 Darwin 引擎创建 iOS 下载器。 */
actual fun fileDownloader(scope: CoroutineScope): FileDownloader {
    return CommonFileDownloader(Darwin, scope)
}
