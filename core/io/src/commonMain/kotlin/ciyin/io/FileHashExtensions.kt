package ciyin.io

import okio.HashingSink
import okio.blackholeSink
import okio.use


/**
 * 文件哈希计算相关扩展函数
 */

/**
 * 计算文件的 MD5 值
 *
 * @return MD5 字符串（小写十六进制），若计算失败则返回空字符串
 */
fun File.md5(): String {
    return try {
        val hashingSink = HashingSink.md5(blackholeSink())
        SystemFileSystem.source(toPath()).use { source ->
            source.readAll(hashingSink)
        }
        hashingSink.hash.hex()
    } catch (e: Exception) {
        ""
    }
}

/**
 * 计算文件的 SHA-1 值
 *
 * @return SHA-1 字符串（小写十六进制），若计算失败则返回空字符串
 */
fun File.sha1(): String {
    return try {
        val hashingSink = HashingSink.sha1(blackholeSink())
        SystemFileSystem.source(toPath()).use { source ->
            source.readAll(hashingSink)
        }
        hashingSink.hash.hex()
    } catch (e: Exception) {
        ""
    }
}


