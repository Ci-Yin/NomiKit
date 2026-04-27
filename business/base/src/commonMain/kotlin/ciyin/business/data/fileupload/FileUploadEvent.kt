package ciyin.business.data.fileupload

import ciyin.business.error.DataError

/**
 * 图片上传事件（Data 层）
 *
 * 该事件用于描述上传过程中的状态变化（开始、进度、成功、失败），仅在 data 层使用；
 * Repository 会将其转换为 domain 层事件对外暴露。
 */
sealed interface FileUploadEvent<D> {

    /**
     * 上传开始
     *
     * @property fileCount 本次上传的文件数量
     */
    data class Started<D>(
        val fileCount: Int,
    ) : FileUploadEvent<D>

    /**
     * 上传进度
     *
     * 注意：当 [contentLength] 为 null 时表示总大小未知。
     *
     * @property bytesSentTotal 已上传字节数（累计）
     * @property contentLength 总字节数（可能未知）
     */
    data class Progress<D>(
        val bytesSentTotal: Long,
        val contentLength: Long?,
    ) : FileUploadEvent<D>

    /**
     * 上传成功
     *
     * @property result 服务端返回的图片地址列表
     */
    data class Success<D>(
        val result: D,
    ) : FileUploadEvent<D>

    /**
     * 上传失败
     *
     * @property error 通用领域错误（data 层只产出 [DataError]）
     */
    data class Failed<D>(
        val error: DataError,
    ) : FileUploadEvent<D>
}

