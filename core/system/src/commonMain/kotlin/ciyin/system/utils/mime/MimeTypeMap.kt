package ciyin.system.utils.mime


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/1 14:56
 * @version: 1.0
 */

/**
 * 内置MIME类型映射表
 */
internal val BUILT_IN_MIME_TYPES = mapOf(
    // 文本
    "txt" to "text/plain",
    "html" to "text/html",
    "htm" to "text/html",
    "css" to "text/css",
    "js" to "text/javascript",
    "mjs" to "text/javascript",
    "json" to "application/json",
    "xml" to "application/xml",
    "csv" to "text/csv",
    "md" to "text/markdown",
    "markdown" to "text/markdown",
    "yml" to "text/yaml",
    "yaml" to "text/yaml",

    // 图片
    "jpg" to "image/jpeg",
    "jpeg" to "image/jpeg",
    "png" to "image/png",
    "gif" to "image/gif",
    "bmp" to "image/bmp",
    "webp" to "image/webp",
    "svg" to "image/svg+xml",
    "ico" to "image/x-icon",
    "heic" to "image/heic",
    "heif" to "image/heif",
    "tiff" to "image/tiff",
    "tif" to "image/tiff",

    // 音频
    "mp3" to "audio/mpeg",
    "wav" to "audio/wav",
    "ogg" to "audio/ogg",
    "m4a" to "audio/mp4",
    "flac" to "audio/flac",
    "aac" to "audio/aac",
    "wma" to "audio/x-ms-wma",
    "opus" to "audio/opus",

    // 视频
    "mp4" to "video/mp4",
    "avi" to "video/x-msvideo",
    "mkv" to "video/x-matroska",
    "mov" to "video/quicktime",
    "wmv" to "video/x-ms-wmv",
    "flv" to "video/x-flv",
    "webm" to "video/webm",
    "m4v" to "video/x-m4v",
    "3gp" to "video/3gpp",
    "mpeg" to "video/mpeg",
    "mpg" to "video/mpeg",

    // 文档
    "pdf" to "application/pdf",
    "doc" to "application/msword",
    "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "xls" to "application/vnd.ms-excel",
    "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "ppt" to "application/vnd.ms-powerpoint",
    "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "odt" to "application/vnd.oasis.opendocument.text",
    "ods" to "application/vnd.oasis.opendocument.spreadsheet",
    "odp" to "application/vnd.oasis.opendocument.presentation",
    "rtf" to "application/rtf",

    // 压缩文件
    "zip" to "application/zip",
    "rar" to "application/x-rar-compressed",
    "7z" to "application/x-7z-compressed",
    "tar" to "application/x-tar",
    "gz" to "application/gzip",
    "bz2" to "application/x-bzip2",
    "xz" to "application/x-xz",

    // APK 和安装包
    "apk" to "application/vnd.android.package-archive",
    "ipa" to "application/octet-stream",
    "exe" to "application/x-msdownload",
    "msi" to "application/x-msdownload",
    "dmg" to "application/x-apple-diskimage",
    "deb" to "application/x-debian-package",
    "rpm" to "application/x-rpm",

    // 字体
    "ttf" to "font/ttf",
    "otf" to "font/otf",
    "woff" to "font/woff",
    "woff2" to "font/woff2",
    "eot" to "application/vnd.ms-fontobject",

    // 代码
    "c" to "text/x-c",
    "cpp" to "text/x-c",
    "h" to "text/x-c",
    "java" to "text/x-java",
    "kt" to "text/x-kotlin",
    "swift" to "text/x-swift",
    "py" to "text/x-python",
    "rb" to "text/x-ruby",
    "php" to "text/x-php",
    "go" to "text/x-go",
    "rs" to "text/x-rust",
    "sh" to "text/x-shellscript",
    "sql" to "text/x-sql",

    // 其他
    "bin" to "application/octet-stream",
    "torrent" to "application/x-bittorrent",
    "iso" to "application/x-iso9660-image",
    "psd" to "image/vnd.adobe.photoshop",
    "ai" to "application/postscript",
    "eps" to "application/postscript"
)