package ciyin.file.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ciyin.io.File

/** Desktop 不支持拍照（FileKit 0.12.0 相机仅移动端）。 */
actual val isCameraPickerSupported: Boolean = false

/** Desktop 拍照不支持桩：返回无动作启动器，UI 应通过 [isCameraPickerSupported] 隐藏入口。 */
@Suppress("UNUSED_PARAMETER")
@Composable
actual fun rememberCameraPicker(
    onResult: (File?) -> Unit,
): PickerResultLauncher = remember { PickerResultLauncher { } }
