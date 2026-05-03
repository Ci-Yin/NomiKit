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
fun File.md5() = hash(HashingSink.md5(blackholeSink()))

/**
 * 计算文件的 SHA-1 值
 *
 * @return SHA-1 字符串（小写十六进制），若计算失败则返回空字符串
 */
fun File.sha1() = hash(HashingSink.sha1(blackholeSink()))

/**
 * 获取文件的 SHA-256 值
 *
 * @return SHA-256 值（小写十六进制），若计算失败则返回空字符串
 */
fun File.sha256() = hash(HashingSink.sha256(blackholeSink()))

/**
 * 获取文件的 SHA-512 值
 *
 * @return SHA-512 值（小写十六进制），若计算失败则返回空字符串
 */
fun File.sha512() = hash(HashingSink.sha512(blackholeSink()))

/**
 * 计算文件的哈希值
 *
 * @param hashingSink 输出流，用于接收计算结果
 * @return 计算结果（小写十六进制），若计算失败则返回空字符串
 */
private fun File.hash(hashingSink: HashingSink): String {
    return try {
        SystemFileSystem.source(toPath()).use { source ->
            source.readAll(hashingSink)
        }
        hashingSink.hash.hex()
    } catch (e: Exception) {
        ""
    }
}
