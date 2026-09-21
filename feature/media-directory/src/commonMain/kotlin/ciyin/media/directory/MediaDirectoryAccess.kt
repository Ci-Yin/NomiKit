package ciyin.media.directory

/** 用户授权的目录信息。 */
data class MediaDirectoryAccess(
    /** 持久化授权令牌；应用层负责将其写入自己的 DataStore / Room 等存储。 */
    val accessToken: MediaDirectoryAccessToken,
    /** 用户在选择器中看到的目录名。 */
    val displayName: String,
    /** 若平台能给出规范化物理路径则填写；Android / iOS 通常为 `null`。 */
    val canonicalPath: String?,
)
