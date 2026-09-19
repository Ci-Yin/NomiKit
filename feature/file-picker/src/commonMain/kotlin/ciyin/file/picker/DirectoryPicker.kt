package ciyin.file.picker

import androidx.compose.runtime.Composable
import ciyin.io.File
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher

/**
 * 创建目录选择器启动器（单选目录）。
 *
 * @param title 对话框标题，null 时使用系统默认标题
 * @param directory 初始目录，null 时从系统默认位置开始
 * @param onResult 选中目录回调；用户取消时不回调
 * @return 目录选择器启动器，调用 [PickerResultLauncher.launch] 来显示选择器
 */
@Composable
fun rememberDirectoryPicker(
    title: String? = null,
    directory: File? = null,
    onResult: (File?) -> Unit,
): PickerResultLauncher = rememberDirectoryPickerLauncher(
    title = title,
    directory = directory?.let { PlatformFile(it.path) },
    dialogSettings = FileKitDialogSettings.createDefault(),
    onResult = { platformFile -> platformFile.toIoFileOrNull()?.let(onResult) },
).toPickerResultLauncher()
