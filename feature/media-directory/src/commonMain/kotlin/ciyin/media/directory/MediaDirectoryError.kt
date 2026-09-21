package ciyin.media.directory

/**
 * 用户媒体目录模块结构化错误。
 *
 * `Cancelled` 是用户主动选择取消，不应作为业务错误向 UI 提示；
 * 其他子类型都需要消费方映射为场景错误并驱动 UI。
 */
sealed interface MediaDirectoryError {
    /** 用户取消目录选择。 */
    data object Cancelled : MediaDirectoryError

    /** 当前平台不支持此能力。 */
    data class Unsupported(
        /** 不支持原因。 */
        val reason: String,
    ) : MediaDirectoryError

    /** 用户拒绝授权（Android 永久拒绝 SAF、iOS 拒绝 Photos/Documents 访问）。 */
    data class PermissionDenied(
        /** 拒绝原因。 */
        val reason: String,
    ) : MediaDirectoryError

    /** 授权令牌失效：目录被卸载、权限被撤销、路径不再存在。 */
    data class AccessRevoked(
        /** 失效原因。 */
        val reason: String,
    ) : MediaDirectoryError

    /** 目标条目已存在；调用方可决定覆盖或跳过。 */
    data class AlreadyExists(
        /** 冲突文件名。 */
        val fileName: String,
    ) : MediaDirectoryError

    /** 目标卷空间不足。 */
    data object NoSpace : MediaDirectoryError

    /** 底层 IO 错误。 */
    data class Io(
        /** 错误原因。 */
        val reason: String,
        /** 原始底层异常。 */
        val cause: Throwable? = null,
    ) : MediaDirectoryError

    /** 文件不存在。 */
    data class NotFound(
        /** 平台位置标识。 */
        val location: String,
    ) : MediaDirectoryError
}
