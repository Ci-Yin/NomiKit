package ciyin.business.base.data.fileupload

import ciyin.business.base.data.ApiClient
import ciyin.business.base.error.DataError
import ciyin.business.base.util.unwrapApiData
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray


/**
 * 上传图片（支持多文件）
 *
 * @param url 上传接口地址
 * @param files 本地图片路径列表
 * @param block 文件上传参数配置
 */
inline fun <reified T> ApiClient.uploadFiles(
    url: String,
    files: List<String>,
    crossinline block: FormBuilder.(file: String) -> Unit = { file ->
        val filePath = Path(file)
        val bytes = SystemFileSystem
            .source(filePath)
            .buffered()
            .use { it.readByteArray() }
        append(
            key = "files",
            value = bytes,
            headers = Headers.build {
                append(HttpHeaders.ContentDisposition, "filename=${filePath.name}")
            }
        )
    }
): Flow<FileUploadEvent<T>> = channelFlow {

    if (files.isEmpty()) {
        trySend(FileUploadEvent.Failed(DataError.Unknown(message = "没有可上传的图片")))
        close()
        return@channelFlow
    }

    trySend(FileUploadEvent.Started(fileCount = files.size))
    try {
        val result = httpClient.submitFormWithBinaryData(
            url = url,
            formData = formData { files.forEach { block(it) } }
        ) {
            onUpload { bytesSentTotal, contentLength ->
                trySend(
                    FileUploadEvent.Progress(
                        bytesSentTotal = bytesSentTotal,
                        contentLength = contentLength,
                    )
                )
            }
        }.unwrapApiData<T>()
        trySend(FileUploadEvent.Success(result = result))
        close()
    } catch (e: Exception) {
        _logger.e(e) { "上传图片失败" }
        trySend(FileUploadEvent.Failed(mapExceptionToDataError(e)))
        close()
    }

    awaitClose()
}

