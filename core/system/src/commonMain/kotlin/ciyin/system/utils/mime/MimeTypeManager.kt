package ciyin.system.utils.mime


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/1 14:55
 * @version: 1.0
 */

/**
 * MIME类型管理器
 *
 * 提供MIME类型的注册、查询和管理功能
 */
object MimeTypeManager {
    private val customMimeTypes = mutableMapOf<String, String>()

    /**
     * 注册自定义MIME类型
     *
     * @param extension 文件扩展名（不含点）
     * @param mimeType MIME类型
     */
    fun register(extension: String, mimeType: String) {
        customMimeTypes[extension.lowercase()] = mimeType
    }

    /**
     * 批量注册MIME类型
     *
     * @param types 扩展名到MIME类型的映射
     */
    fun registerAll(types: Map<String, String>) {
        types.forEach { (ext, mime) ->
            customMimeTypes[ext.lowercase()] = mime
        }
    }

    /**
     * 移除自定义MIME类型
     */
    fun unregister(extension: String) {
        customMimeTypes.remove(extension.lowercase())
    }

    /**
     * 清除所有自定义MIME类型
     */
    fun clearCustomTypes() {
        customMimeTypes.clear()
    }

    /**
     * 获取MIME类型
     *
     * @param filename 文件名
     * @param defaultMime 默认MIME类型
     * @return MIME类型字符串
     */
    fun getMimeType(filename: String, defaultMime: String = DEFAULT_MIME_TYPE): String {
        // 清理文件名：去除前后空格和 URL 查询参数
        val cleanFilename = filename.trim()
            .substringBefore('?')  // 移除 URL 查询参数
            .substringBefore('#')  // 移除 URL fragment

        val extension = cleanFilename.substringAfterLast('.', "").lowercase()
        if (extension.isEmpty()) return defaultMime

        // 优先返回自定义类型
        return customMimeTypes[extension]
            ?: builtInMimeTypes[extension]
            ?: defaultMime
    }

    /**
     * 根据MIME类型获取扩展名
     */
    fun getExtension(mimeType: String): String? {
        // 先查自定义映射
        customMimeTypes.entries.firstOrNull { it.value == mimeType }?.let {
            return it.key
        }
        // 再查内置映射 - 使用首选扩展名
        return preferredExtensionMap[mimeType] ?: mimeToExtensionMap[mimeType]
    }

    /**
     * 检查是否为图片类型
     */
    fun isImage(mimeType: String): Boolean {
        return mimeType.startsWith("image/")
    }

    /**
     * 检查是否为视频类型
     */
    fun isVideo(mimeType: String): Boolean {
        return mimeType.startsWith("video/")
    }

    /**
     * 检查是否为音频类型
     */
    fun isAudio(mimeType: String): Boolean {
        return mimeType.startsWith("audio/")
    }

    /**
     * 检查是否为文本类型
     */
    fun isText(mimeType: String): Boolean {
        return mimeType.startsWith("text/")
    }

    /**
     * 获取所有已注册的扩展名
     */
    fun getAllExtensions(): Set<String> {
        return builtInMimeTypes.keys + customMimeTypes.keys
    }

    /**
     * 获取所有已注册的MIME类型
     */
    fun getAllMimeTypes(): Set<String> {
        return builtInMimeTypes.values.toSet() + customMimeTypes.values.toSet()
    }

    /**
     * 检查是否有自定义类型注册
     */
    fun hasCustomType(extension: String): Boolean {
        return customMimeTypes.containsKey(extension.lowercase())
    }

    internal val builtInMimeTypes: Map<String, String>
        get() = BUILT_IN_MIME_TYPES

    private val mimeToExtensionMap by lazy {
        builtInMimeTypes.entries.associate { (k, v) -> v to k }
    }

    /**
     * 为常见的多扩展名MIME类型指定首选扩展名
     * 用于解决一个MIME类型对应多个扩展名的情况
     */
    private val preferredExtensionMap = mapOf(
        // 图片
        "image/jpeg" to "jpg",
        "image/tiff" to "tiff",

        // 文本
        "text/html" to "html",
        "text/javascript" to "js",
        "text/markdown" to "md",
        "text/yaml" to "yaml",

        // 视频
        "video/mpeg" to "mpeg",

        // 代码
        "text/x-c" to "c"
    )
}