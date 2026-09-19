package ciyin.file.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ciyin.io.File
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher

/** iOS 支持拍照。 */
actual val isCameraPickerSupported: Boolean = true

/** iOS 拍照实现：调用 FileKit 系统相机（UIImagePickerController），结果转换为 [File]。 */
@Composable
actual fun rememberCameraPicker(
    onResult: (File?) -> Unit,
): PickerResultLauncher {
    val launcher = rememberCameraPickerLauncher(
        onResult = { platformFile -> onResult(platformFile.toIoFileOrNull()) },
    )
    return remember(launcher) { PickerResultLauncher { launcher.launch() } }
}
