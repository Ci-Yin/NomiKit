package ciyin.platform.share

import ciyin.io.File
import ciyin.platform.Context

/** 可分享文件的跨平台来源。 */
sealed interface PlatformShareFileSource {

    /**
     * 已由调用方取得访问权限的 URI。
     *
     * @property value URI 字符串
     */
    data class Uri(val value: String) : PlatformShareFileSource

    /**
     * 本地文件。
     *
     * @property value 基于 Okio 的跨平台文件引用
     */
    data class LocalFile(val value: File) : PlatformShareFileSource
}

/**
 * 单个可分享文件。
 *
 * @property source 文件来源
 * @property mimeType 文件 MIME 类型
 * @property displayName 分享面板可使用的展示名称
 */
data class PlatformShareFile(
    val source: PlatformShareFileSource,
    val mimeType: String,
    val displayName: String? = null,
)

/** 系统分享内容。 */
sealed interface PlatformSharePayload {

    /** 系统分享面板可使用的内容标题。 */
    val title: String?

    /**
     * 纯文本分享内容。
     *
     * @property value 要分享的文本
     * @property title 系统分享面板可使用的内容标题
     */
    data class Text(
        val value: String,
        override val title: String? = null,
    ) : PlatformSharePayload

    /**
     * 单文件分享内容。
     *
     * @property value 要分享的文件
     * @property title 系统分享面板可使用的内容标题
     */
    data class File(
        val value: PlatformShareFile,
        override val title: String? = null,
    ) : PlatformSharePayload

    /**
     * 多文件分享内容。
     *
     * @property values 要分享的非空文件列表
     * @property title 系统分享面板可使用的内容标题
     */
    data class Files(
        val values: List<PlatformShareFile>,
        override val title: String? = null,
    ) : PlatformSharePayload
}

/** 系统分享入口调用结果。 */
enum class PlatformShareResult {
    /** 系统分享面板已经打开。 */
    Opened,

    /** 当前平台没有可用的系统分享能力。 */
    Unsupported,
}

/** 系统分享技术失败原因。 */
enum class PlatformShareFailureReason {
    /** 分享载荷不符合接口约束。 */
    InvalidPayload,

    /** URI 格式无效或不是当前平台支持的文件 URI。 */
    InvalidUri,

    /** 本地文件不存在、不是普通文件或无法读取。 */
    FileUnavailable,

    /** URI 指向的内容不存在或无法读取。 */
    UriUnavailable,

    /** 当前进程无法向分享目标授予文件读取权限。 */
    PermissionDenied,

    /** 当前平台没有可用于展示分享面板的控制器或窗口。 */
    PresenterUnavailable,

    /** 系统分享面板启动失败。 */
    LaunchFailed,
}

/**
 * 系统分享技术异常。
 *
 * 业务 Data 层应根据 [reason] 映射为自己的通用错误，不应把本异常直接透传给 UI。
 *
 * @property reason 稳定的技术失败原因
 * @param message 供日志与诊断使用的技术信息
 * @param cause 原始平台异常
 */
class PlatformShareException(
    val reason: PlatformShareFailureReason,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * 调用当前平台的系统分享入口。
 *
 * @param context 平台上下文
 * @param payload 要交给系统分享面板的内容
 * @return 分享面板打开结果
 * @throws PlatformShareException 分享内容无效、文件不可访问或平台面板展示失败
 */
expect suspend fun sharePlatformContent(
    context: Context,
    payload: PlatformSharePayload,
): PlatformShareResult

/** 校验并规范化可选系统分享标题。 */
internal fun PlatformSharePayload.validatedPlatformShareTitleOrNull(): String? {
    val currentTitle = title ?: return null
    val normalizedTitle = currentTitle.trim()
    if (normalizedTitle.isEmpty()) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidPayload,
            message = "系统分享标题不能为空白内容",
        )
    }
    return normalizedTitle
}

/** 校验系统分享文本内容。 */
internal fun String.validatedPlatformShareText(): String {
    if (isBlank()) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidPayload,
            message = "系统分享文本不能为空白内容",
        )
    }
    return this
}

/** 校验并规范化系统分享 MIME 类型。 */
internal fun String.validatedPlatformShareMimeType(): String {
    val normalizedMimeType = trim()
    val components = normalizedMimeType.split('/')
    if (
        components.size != MIME_COMPONENT_COUNT ||
        components.any(String::isBlank) ||
        normalizedMimeType.any(Char::isWhitespace)
    ) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidPayload,
            message = "文件 MIME 类型无效: $this",
        )
    }
    return normalizedMimeType
}

/** 计算多文件分享使用的最窄公共 MIME 类型。 */
internal fun List<String>.resolveCommonPlatformShareMimeType(): String {
    if (isEmpty()) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidPayload,
            message = "多文件分享 MIME 类型列表不能为空",
        )
    }

    val distinctMimeTypes = map(String::validatedPlatformShareMimeType).distinct()
    if (distinctMimeTypes.size == 1) return distinctMimeTypes.single()

    val topLevelTypes = distinctMimeTypes.map { it.substringBefore('/') }.distinct()
    return if (topLevelTypes.size == 1) {
        "${topLevelTypes.single()}/*"
    } else {
        ANY_PLATFORM_SHARE_MIME_TYPE
    }
}

/** MIME 类型的固定组成部分数量。 */
private const val MIME_COMPONENT_COUNT: Int = 2

/** 任意内容 MIME 类型。 */
private const val ANY_PLATFORM_SHARE_MIME_TYPE: String = "*/*"
