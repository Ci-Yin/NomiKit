package ciyin.file.picker

import ciyin.io.File
import ciyin.io.toFile
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.path

/**
 * 将 FileKit 的 PickerResultLauncher 转换为项目内部的 PickerResultLauncher。
 *
 * @return 项目内部的 PickerResultLauncher 实例
 */
internal fun io.github.vinceglb.filekit.dialogs.compose.PickerResultLauncher.toPickerResultLauncher(): PickerResultLauncher {
    return PickerResultLauncher(this::launch)
}

/**
 * 将 FileKit 的 [PlatformFile] 结果转换为项目内部的 [File]。
 *
 * @return 对应的 [File]，入参为 null 时返回 null
 */
internal fun PlatformFile?.toIoFileOrNull(): File? = this?.path?.toFile()

/**
 * 将项目内部的 FileType 转换为 FileKit 的 FileKitType。
 *
 * @return 对应的 FileKitType 实例
 */
internal fun FileType.toFileKitType(): FileKitType = when (this) {
    is FileType.Image -> FileKitType.Image
    is FileType.Video -> FileKitType.Video
    is FileType.ImageAndVideo -> FileKitType.ImageAndVideo
    is FileType.File -> FileKitType.File(extensions)
}

/**
 * 将项目内部的 FileMode 转换为 FileKit 的 FileKitMode。
 *
 * @return 对应的 FileKitMode 实例
 */
internal fun <Result> FileMode<Result>.toFileKitMode() = when (this) {
    is FileMode.Single -> FileKitMode.Single
    is FileMode.Multiple -> FileKitMode.Multiple(maxItems)
}

/**
 * 将回调函数转换为 FileKit 的结果处理函数。
 *
 * 根据 FileMode 的类型，将 PlatformFile 或 List<PlatformFile> 转换为对应的结果类型。
 *
 * @param mode 文件选择模式，用于确定结果类型
 * @return 适配后的结果处理函数
 */
@Suppress("UNCHECKED_CAST")
internal fun <Result> ((Result) -> Unit).toResult(mode: FileMode<Result>) = { result: Any? ->
    when (result) {

        is PlatformFile -> {
            // Single 模式：返回单个文件
            require(mode is FileMode.Single) { "Single file result requires Single mode" }
            this(result.path.toFile() as Result)
        }

        is List<*> -> {
            // Multiple 模式：返回文件列表
            require(mode is FileMode.Multiple) { "Multiple files result requires Multiple mode" }
            val files = result.mapNotNull { file ->
                (file as? PlatformFile)?.path?.toFile()
            }
            this(files as Result)
        }

    }
}
