package ciyin.file.picker

import androidx.compose.runtime.Composable
import ciyin.io.File

/**
 * 当前平台是否支持拍照。
 *
 * Android / iOS 为 `true`；Desktop 为 `false`（FileKit 0.12.0 相机仅移动端）。
 * UI 可据此决定是否展示拍照入口，避免在桌面端出现点击无响应的按钮。
 */
expect val isCameraPickerSupported: Boolean

/**
 * 创建拍照启动器（仅移动端 Android / iOS 支持）。
 *
 * 说明：FileKit 0.12.0 相机仅支持拍照（`FileKitCameraType.Photo`），**不支持录像**；
 * Desktop 平台的 actual 为不支持桩，[PickerResultLauncher.launch] 调用后无动作，
 * 应配合 [isCameraPickerSupported] 在 UI 中隐藏入口。
 *
 * @param onResult 拍摄结果回调，返回落盘后的 [File]；用户取消时回调 `null`。
 * @return 拍照启动器，调用 [PickerResultLauncher.launch] 打开系统相机。
 */
@Composable
expect fun rememberCameraPicker(
    onResult: (File?) -> Unit,
): PickerResultLauncher
