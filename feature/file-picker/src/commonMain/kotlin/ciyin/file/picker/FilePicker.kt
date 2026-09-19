package ciyin.file.picker

import androidx.compose.runtime.Composable
import ciyin.io.File
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

/**
 * 创建文件选择器启动器。
 *
 * 这是一个通用的文件选择器函数，支持自定义选择模式和结果类型。
 * 使用 [FileMode] 来定义选择行为（单选或多选）。
 *
 * @param type 文件类型，默认为任意文件
 * @param mode 选择模式，决定返回结果的类型
 *   - [FileMode.Single]：返回 `File?`（单个文件）
 *   - [FileMode.Multiple]：返回 `List<File?>`（文件列表）
 * @param title 对话框标题，null 时使用系统默认标题
 * @param directory 初始目录，null 时从系统默认位置开始
 * @param onResult 选择结果回调函数
 * @return 文件选择器启动器，调用 [PickerResultLauncher.launch] 来显示选择器
 */
@Composable
fun <Result> rememberFilePicker(
    type: FileType = FileType.File(),
    mode: FileMode<Result>,
    title: String? = null,
    directory: File? = null,
    onResult: (Result) -> Unit,
): PickerResultLauncher = rememberFilePickerLauncher(
    type = type.toFileKitType(),
    mode = mode.toFileKitMode(),
    title = title,
    directory = directory?.let { PlatformFile(it.path) },
    dialogSettings = FileKitDialogSettings.createDefault(),
    onResult = onResult.toResult(mode)
).toPickerResultLauncher()

/**
 * 文件选择器（简化版，返回单个文件）。
 *
 * 这是单选模式的便捷函数，直接返回选中的文件。
 * 如果用户取消选择，回调不会被调用。
 *
 * @param type 文件类型，默认为任意文件
 * @param title 对话框标题，null 时使用系统默认标题
 * @param directory 初始目录，null 时从系统默认位置开始
 * @param onResult 选择结果回调函数，参数为选中的文件
 * @return 文件选择器启动器，调用 [PickerResultLauncher.launch] 来显示选择器
 */
@Composable
fun rememberFilePicker(
    type: FileType = FileType.File(),
    title: String? = null,
    directory: File? = null,
    onResult: (File?) -> Unit,
): PickerResultLauncher = rememberFilePicker(
    type = type,
    mode = FileMode.Single,
    title = title,
    directory = directory,
    onResult = onResult
)
