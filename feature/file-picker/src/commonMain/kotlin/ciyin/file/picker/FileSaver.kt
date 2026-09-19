package ciyin.file.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ciyin.io.File
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher

/**
 * 文件保存启动器：调用 [launch] 弹出「保存到」对话框并写入字节。
 */
class FileSaverLauncher(private val onLaunch: (ByteArray, String, String?) -> Unit) {
    /**
     * 弹出保存对话框并写入内容。
     *
     * @param bytes 要写入的内容
     * @param suggestedName 建议文件名（不含扩展名）
     * @param extension 扩展名（不含点），可空
     */
    fun launch(bytes: ByteArray, suggestedName: String, extension: String? = null) {
        onLaunch(bytes, suggestedName, extension)
    }
}

/**
 * 创建文件保存器。用户选定目标位置后写入 [FileSaverLauncher.launch] 提供的字节。
 *
 * @param onResult 保存成功回调，返回落盘后的 [File]；用户取消时不回调
 * @return 文件保存启动器，调用 [FileSaverLauncher.launch] 来显示保存对话框
 */
@Suppress("DEPRECATION")
@Composable
fun rememberFileSaver(
    onResult: (File?) -> Unit,
): FileSaverLauncher {
    val saver = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault(),
        onResult = { platformFile -> platformFile.toIoFileOrNull()?.let(onResult) },
    )
    return remember(saver) {
        FileSaverLauncher { bytes, suggestedName, extension ->
            saver.launch(bytes, suggestedName, extension)
        }
    }
}
