package ciyin.io

import okio.IOException
import okio.Path.Companion.toPath
import okio.Sink
import okio.use


/**
 * 递归删除文件或文件夹及其内容
 *
 * @param onProgress 回调函数，用于报告进度，默认为空操作
 * @return 如果成功删除则返回`true`，否则返回`false`
 */
fun File.delDir(onProgress: (File) -> Unit = {}): Boolean = walkBottomUp().fold(true) { res, it ->
    onProgress(it)
    (it.delete() || !it.exists()) && res
}


/**
 * 复制文件夹到指定位置
 *
 * @param to 目标文件夹路径
 * @param overwrite 是否覆盖目标文件夹
 * @param onProgress 回调函数，用于报告进度，默认为空操作
 * @return 如果成功复制则返回`true`，否则返回`false`
 */
fun File.copyDir(to: String, overwrite: Boolean = false, onProgress: (File) -> Unit = {}): Boolean {
    return copyDir(to.toFile(), overwrite, onProgress)
}

/**
 * 复制文件夹到指定位置
 *
 * @param to 目标文件夹路径
 * @param overwrite 是否覆盖目标文件夹
 * @param onProgress 回调函数，用于报告进度，默认为空操作
 * @return 如果成功复制则返回`true`，否则返回`false`
 */
fun File.copyDir(to: File, overwrite: Boolean = false, onProgress: (File) -> Unit = {}): Boolean {
    if (isFile()) {
        return copy(to.path)
    }
    try {
        for (src in walkTopDown()) {
            val relPath = src.toRelativeString(this)
            val dstFile = File(to, relPath)
            if (dstFile.exists() && !(src.isDirectory && dstFile.isDirectory)) {
                if (overwrite) {
                    if (dstFile.isDirectory) dstFile.deleteRecursively() else dstFile.delete()
                }
            }
            if (src.isDirectory) {
                dstFile.mkdirs()
            } else {
                if (src.copyTo(dstFile, overwrite).length() != src.length()) {
                    return false
                }
            }
            onProgress(dstFile)
        }
        return true
    } catch (e: IOException) {
        return false
    }
}

/**
 * 复制文件到指定路径
 *
 * @param to 文件的目标路径
 * @return 如果成功复制则返回`true`，否则返回`false`
 */
fun File.copy(to: String): Boolean {
    return try {
        SystemFileSystem.copy(this.toPath(), to.toPath())
        true
    } catch (e: IOException) {
        false
    }
}

/**
 * 复制文件到指定路径
 *
 * @param to 文件的目标路径
 * @param onProgress 回调函数，用于报告进度，默认为空操作
 * @param onProgress 进度回调，参数为已复制字节数，返回 `false` 可取消复制
 */
fun File.copy(
    targetPath: String,
    overwrite: Boolean = true,
    bufferSize: Long = DEFAULT_BUFFER_SIZE,
    progressInterval: Long = 1024 * 1024,
    onProgress: (Long) -> Boolean = { true }
): Boolean = copy(
    targetPath = targetPath.toFile(),
    overwrite = overwrite,
    bufferSize = bufferSize,
    progressInterval = progressInterval,
    onProgress = onProgress
)

/**
 * 复制文件到指定文件对象
 *
 * @param targetPath 目标文件对象
 * @param overwrite 是否覆盖目标文件
 * @param bufferSize 缓冲区大小，默认为 [DEFAULT_BUFFER_SIZE]。
 * @param onProgress 进度回调，参数为已复制字节数，返回 `false` 可取消复制
 * @return 如果成功复制则返回`true`，否则返回`false`
 */
fun File.copy(
    targetPath: File,
    overwrite: Boolean = true,
    bufferSize: Long = DEFAULT_BUFFER_SIZE,
    progressInterval: Long = 1024 * 1024,
    onProgress: (Long) -> Boolean = { true }
): Boolean {
    if (overwrite.not() && targetPath.exists()) return false
    return try {
        SystemFileSystem.sink(targetPath.toPath()).use { sink ->
            copy(
                sink = sink,
                bufferSize = bufferSize,
                progressInterval = progressInterval,
                onProgress = onProgress
            )
        }
    } catch (e: IOException) {
        false
    }
}

/**
 * 将文件复制到输出流
 *
 * @param sink 输出流
 * @param bufferSize 缓冲区大小，默认为 [DEFAULT_BUFFER_SIZE]。
 * @param onProgress 进度回调，参数为已复制字节数，返回 `false` 可取消复制
 * @return 如果成功复制则返回`true`，否则返回`false`
 */
fun File.copy(
    sink: Sink,
    bufferSize: Long = DEFAULT_BUFFER_SIZE,
    progressInterval: Long = 1024 * 1024,
    onProgress: (Long) -> Boolean = { true }
): Boolean {
    return try {
        SystemFileSystem.source(toPath()).use { source ->
            source.copyTo(
                sink,
                bufferSize = bufferSize,
                progressInterval = progressInterval,
                onProgress = onProgress
            )
        }
        true
    } catch (e: Exception) {
        return false
    }
}
