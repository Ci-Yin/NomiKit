package ciyin.system.utils.mime

import ciyin.io.File
import ciyin.io.SystemFileSystem
import ciyin.io.extension


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/2 17:11
 */

import okio.Buffer
import okio.use

/**
 * 文件类型判断相关扩展函数
 */

// 文件头标识表（十六进制前缀 → 文件类型）
private val fileTypes = mapOf(
    "FFD8FF" to "jpg",
    "89504E47" to "png",
    "47494638" to "gif",
    "49492A00" to "tif",
    "424D" to "bmp",
    "41433130" to "dwg",
    "38425053" to "psd",
    "7B5C727466" to "rtf",
    "3C3F786D6C" to "xml",
    "68746D6C3E" to "html",
    "44656C69766572792D646174653A" to "eml",
    "D0CF11E0" to "doc",
    "5374616E64617264204A" to "mdb",
    "252150532D41646F6265" to "ps",
    "255044462D312E" to "pdf",
    "504B0304" to "docx",
    "52617221" to "rar",
    "57415645" to "wav",
    "41564920" to "avi",
    "2E524D46" to "rm",
    "000001BA" to "mpg",
    "000001B3" to "mpg",
    "6D6F6F76" to "mov",
    "3026B2758E66CF11" to "asf",
    "4D546864" to "mid",
    "1F8B08" to "gz"
)

/**
 * 是否为视频
 */
fun File.isVideo(): Boolean = extension.lowercase() == "m3u8" || mime().startsWith("video/")

/**
 * 是否为图片
 */
fun File.isImage(): Boolean = mime().startsWith("image/")

/**
 * 是否为动图
 */
fun File.isGif(): Boolean = extension.lowercase() == "gif"

/**
 * 获取文件类型（根据文件头结构）
 *
 * @return 文件类型字符串（如 "png"、"jpg"、"pdf"），若无法识别则返回空字符串
 */
fun File.type(): String {
    if (!exists() || isDirectory()) return ""
    return try {
        SystemFileSystem.source(toPath()).use { source ->
            val buffer = Buffer()
            source.read(buffer, 12) // 读取文件头前 12 个字节
            val headerHex = buffer.snapshot().hex().uppercase()
            fileTypes.entries.firstOrNull { (key, _) ->
                headerHex.startsWith(key)
            }?.value ?: ""
        }
    } catch (e: Exception) {
        ""
    }
}

/**
 * 获取文件MIME类型，根据扩展名
 */
fun File.mime(): String = name.getMime()
