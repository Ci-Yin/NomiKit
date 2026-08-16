package ciyin.file.downloader

import ciyin.file.downloader.core.FileDownloader
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope

/** 使用 OkHttp 引擎创建 JVM 与 Android 下载器。 */
actual fun fileDownloader(scope: CoroutineScope): FileDownloader {
    return CommonFileDownloader(OkHttp, scope)
}
