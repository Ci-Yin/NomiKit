package ciyin.media.directory

import ciyin.io.File

/**
 * 用户媒体目录授权会话。
 *
 * 一个会话对应一次"用户选目录 → 拿到令牌 → 多次 commit/delete/exists"。
 * 令牌本身可跨进程持久化，调用方负责保存到自己的 DataStore / Room。
 *
 * Android 实现的 [requestAccess] 在宿主提供 [DirectoryPickerLauncher] 时可直接拉起系统选择器；
 * 未提供时仍可构造会话，并使用已获得的 Tree URI 执行其余操作。
 */
interface MediaDirectorySession {
    /** 拉起平台目录选择器；用户取消时抛 [MediaDirectoryException] 携带 [MediaDirectoryError.Cancelled]。 */
    suspend fun requestAccess(): MediaDirectoryAccess

    /**
     * 验证已保存令牌仍对应一个可读写目录；失效时抛 [MediaDirectoryException] 携带 [MediaDirectoryError.AccessRevoked]。
     *
     * 应在每次启动恢复目录列表时调用一次，作为失效检测的廉价探针。
     */
    suspend fun validate(accessToken: MediaDirectoryAccessToken)

    /**
     * 把本地源文件提交到已授权目录。
     *
     * - 写入采用 staging → rename 语义：先写到临时文件名，校验 size 后再 commit。
     * - 提交前由调用方保证 [fileName] 已按应用命名协议生成。
     * - 同一目录内 [fileName] 已存在时抛 [MediaDirectoryException] 携带 [MediaDirectoryError.AlreadyExists]；
     *   覆盖由调用方显式删后重试。
     */
    suspend fun commit(
        accessToken: MediaDirectoryAccessToken,
        source: File,
        fileName: String,
        mimeType: String,
    ): MediaDirectoryEntry

    /** 删除授权目录中的一个应用管理文件；不存在时按成功处理（幂等）。 */
    suspend fun delete(
        accessToken: MediaDirectoryAccessToken,
        location: String,
    )

    /**
     * 检查文件是否仍存在。
     *
     * 注意：仅检查"应用管理文件"是否仍位于授权目录中，不扫描用户文件。
     */
    suspend fun exists(
        accessToken: MediaDirectoryAccessToken,
        location: String,
    ): Boolean
}
