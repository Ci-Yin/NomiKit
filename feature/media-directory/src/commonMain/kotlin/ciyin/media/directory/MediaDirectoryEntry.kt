package ciyin.media.directory

/** 提交后的应用管理文件引用。 */
data class MediaDirectoryEntry(
    /** 提交时使用的目录授权令牌。 */
    val accessToken: MediaDirectoryAccessToken,
    /** 应用可见文件名（含扩展名），与 [displayName] 等价。 */
    val fileName: String,
    /** 平台不透明位置标识：Android 为 DocumentFile 单文件 URI 字符串；iOS / Desktop 与 [fileName] 同。 */
    val location: String,
    /** 提交时记录的 MIME 类型。 */
    val mimeType: String,
    /** 提交完成时记录的字节大小。 */
    val size: Long,
)
