package ciyin.io

import ciyin.lang.matchIn
import okio.Path


fun String.toFile(): File {
    return File(this)
}

fun Path.toFile(): File = File(this)

/**
 * 某些情况需要文件和URL混用的时候
 * 可以用这个转换成正确的URL
 * 如果是文件的还是会输出路径
 *
 * @return URL字符串
 */
fun File.toUrl(): String {
    if (path.matchIn("^https:/")) {
        return path.replaceFirst("^https:/".toRegex(), "https://")
    }
    if (path.matchIn("^http:/")) {
        return path.replaceFirst("^http:/".toRegex(), "http://")
    }
    if (path.matchIn("^ftp:/")) {
        return path.replaceFirst("^ftp:/".toRegex(), "ftp://")
    }
    return path
}