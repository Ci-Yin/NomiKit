package ciyin.file.downloader.util

import ciyin.io.SystemFileSystem
import ciyin.file.downloader.model.DownloadConfig
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * 将文件路径转换为临时文件路径。
 *
 * 在原始路径后追加 `.tmp` 后缀，用于下载过程中的临时文件。
 *
 * @return 临时文件的 [Path]。
 */
internal fun String.toTempPath(): Path {
    return "${this}.tmp".toPath()
}

/**
 * 生成分块临时目录路径。
 *
 * 在原始路径后追加 `.chunks` 后缀，用于存放分块下载过程中的临时文件。
 *
 * @return 分块临时目录的 [Path]。
 */
internal fun String.toChunkTempDir(): Path {
    return "${this}.chunks".toPath()
}

/**
 * 生成分块目录内的单个分块文件路径。
 *
 * @param index 分块索引。
 * @return 分块文件的 [Path]。
 */
internal fun Path.toChunkFilePath(index: Int): Path {
    return this / "chunk_$index"
}

/**
 * 删除下载完成后的保存文件。
 *
 * 根据 [DownloadConfig.savePath] 构建目标文件路径，
 * 如果文件存在则将其删除。
 *
 * @param fileSystem 使用的文件系统实现，默认使用 [SystemFileSystem]。
 */
fun DownloadConfig.deleteSaveFile(fileSystem: FileSystem = SystemFileSystem) {
    val savePath = this@deleteSaveFile.savePath.toPath()
    if (fileSystem.exists(savePath)) {
        fileSystem.delete(savePath)
    }
}

/**
 * 删除下载过程中产生的临时文件。
 *
 * 临时文件路径通过 [toTempPath] 从 [DownloadConfig.savePath] 推导得到，
 *
 * @param fileSystem 使用的文件系统实现，默认使用 [SystemFileSystem]。
 */
fun DownloadConfig.deleteTempFile(fileSystem: FileSystem = SystemFileSystem) {
    val tempPath = savePath.toTempPath()
    deleteIdempotently(
        delete = { fileSystem.delete(tempPath, mustExist = false) },
        exists = { fileSystem.exists(tempPath) },
    )
}

/**
 * 执行幂等删除，只忽略删除期间目标已被其他清理方移除的竞态错误。
 *
 * @param delete 实际删除操作。
 * @param exists 删除失败后检查目标是否仍存在的操作。
 */
internal inline fun deleteIdempotently(
    delete: () -> Unit,
    exists: () -> Boolean,
) {
    try {
        delete()
    } catch (error: IOException) {
        if (exists()) throw error
    }
}

/**
 * 删除分块临时目录及其所有内容。
 *
 * @param fileSystem 使用的文件系统实现，默认使用 [SystemFileSystem]。
 */
fun DownloadConfig.deleteChunkTempDir(fileSystem: FileSystem = SystemFileSystem) {
    val dir = savePath.toChunkTempDir()
    if (fileSystem.exists(dir)) {
        fileSystem.deleteRecursively(dir)
    }
}
