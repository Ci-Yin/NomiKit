package com.ciyin.app.presentation.screen.filepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.divider.NomiDivider
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import ciyin.file.picker.FileMode
import ciyin.file.picker.FileType
import ciyin.file.picker.isCameraPickerSupported
import ciyin.file.picker.rememberCameraPicker
import ciyin.file.picker.rememberDirectoryPicker
import ciyin.file.picker.rememberFilePicker
import ciyin.file.picker.rememberFileSaver
import ciyin.io.File
import ciyin.material.theme.AppTheme
import com.ciyin.app.presentation.component.SamplePageScaffold
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.file_picker_demo_back
import com.ciyin.app.sample.file_picker_demo_camera_action
import com.ciyin.app.sample.file_picker_demo_camera_result_title
import com.ciyin.app.sample.file_picker_demo_description
import com.ciyin.app.sample.file_picker_demo_directory_action
import com.ciyin.app.sample.file_picker_demo_directory_result_title
import com.ciyin.app.sample.file_picker_demo_gallery_image_video_action
import com.ciyin.app.sample.file_picker_demo_gallery_video_action
import com.ciyin.app.sample.file_picker_demo_multiple_action
import com.ciyin.app.sample.file_picker_demo_result_empty
import com.ciyin.app.sample.file_picker_demo_result_none
import com.ciyin.app.sample.file_picker_demo_result_title
import com.ciyin.app.sample.file_picker_demo_save_action
import com.ciyin.app.sample.file_picker_demo_save_result_title
import com.ciyin.app.sample.file_picker_demo_single_action
import com.ciyin.app.sample.file_picker_demo_title
import org.jetbrains.compose.resources.stringResource

/**
 * 文件选择器示例页面。
 *
 * 演示文件选择（单选/多选图片、相册视频、图片+视频）、目录选择、文件保存，
 * 以及移动端拍照（桌面端隐藏），回调统一返回 [File]。
 *
 * @param onBack 返回上一页回调。
 */
@Composable
internal fun FilePickerDemoScreen(onBack: () -> Unit) {
    var pickedFiles by remember { mutableStateOf<List<File?>>(emptyList()) }
    var pickedDirectory by remember { mutableStateOf<File?>(null) }
    var savedFile by remember { mutableStateOf<File?>(null) }
    var capturedFile by remember { mutableStateOf<File?>(null) }

    val singlePicker = rememberFilePicker(
        type = FileType.File(),
        onResult = { file -> pickedFiles = listOf(file) },
    )
    val multiplePicker = rememberFilePicker(
        type = FileType.Image,
        mode = FileMode.Multiple(),
        onResult = { files -> pickedFiles = files },
    )
    val videoPicker = rememberFilePicker(
        type = FileType.Video,
        mode = FileMode.Multiple(),
        onResult = { files -> pickedFiles = files },
    )
    val imageVideoPicker = rememberFilePicker(
        type = FileType.ImageAndVideo,
        mode = FileMode.Multiple(),
        onResult = { files -> pickedFiles = files },
    )
    val directoryPicker = rememberDirectoryPicker(
        onResult = { directory -> pickedDirectory = directory },
    )
    val fileSaver = rememberFileSaver(
        onResult = { file -> savedFile = file },
    )
    val cameraPicker = rememberCameraPicker(
        onResult = { file -> capturedFile = file },
    )

    SamplePageScaffold(
        title = stringResource(Res.string.file_picker_demo_title),
        onBack = onBack,
        backContentDescription = stringResource(Res.string.file_picker_demo_back),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(AppTheme.spacings.large),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
        ) {
            item {
                NomiText(
                    text = stringResource(Res.string.file_picker_demo_description),
                    color = AppTheme.colorScheme.textSecondary,
                    style = NomiTheme.typography.bodyMedium,
                )
            }
            item {
                NomiButton(
                    onClick = singlePicker::launch,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    NomiText(stringResource(Res.string.file_picker_demo_single_action))
                }
            }
            item {
                NomiButton(
                    onClick = multiplePicker::launch,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    NomiText(stringResource(Res.string.file_picker_demo_multiple_action))
                }
            }
            item {
                NomiButton(
                    onClick = videoPicker::launch,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    NomiText(stringResource(Res.string.file_picker_demo_gallery_video_action))
                }
            }
            item {
                NomiButton(
                    onClick = imageVideoPicker::launch,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    NomiText(stringResource(Res.string.file_picker_demo_gallery_image_video_action))
                }
            }
            item {
                NomiButton(
                    onClick = directoryPicker::launch,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    NomiText(stringResource(Res.string.file_picker_demo_directory_action))
                }
            }
            item {
                NomiButton(
                    onClick = {
                        fileSaver.launch(
                            bytes = "NomiKit 文件保存示例".encodeToByteArray(),
                            suggestedName = "nomikit-save-demo",
                            extension = "txt",
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    NomiText(stringResource(Res.string.file_picker_demo_save_action))
                }
            }
            if (isCameraPickerSupported) {
                item {
                    NomiButton(
                        onClick = cameraPicker::launch,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        NomiText(stringResource(Res.string.file_picker_demo_camera_action))
                    }
                }
            }

            item { NomiDivider() }

            item {
                ResultSection(
                    title = stringResource(Res.string.file_picker_demo_result_title),
                ) {
                    if (pickedFiles.isEmpty()) {
                        NomiText(
                            text = stringResource(Res.string.file_picker_demo_result_empty),
                            color = AppTheme.colorScheme.textSecondary,
                            style = NomiTheme.typography.bodyMedium,
                        )
                    } else {
                        pickedFiles.forEach { file ->
                            NomiText(
                                text = file?.path ?: "-",
                                style = NomiTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            item {
                ResultSection(
                    title = stringResource(Res.string.file_picker_demo_directory_result_title),
                ) {
                    NomiText(
                        text = pickedDirectory?.path
                            ?: stringResource(Res.string.file_picker_demo_result_none),
                        color = if (pickedDirectory == null) {
                            AppTheme.colorScheme.textSecondary
                        } else {
                            NomiTheme.colorScheme.onSurface
                        },
                        style = NomiTheme.typography.bodyMedium,
                    )
                }
            }

            item {
                ResultSection(
                    title = stringResource(Res.string.file_picker_demo_save_result_title),
                ) {
                    NomiText(
                        text = savedFile?.path
                            ?: stringResource(Res.string.file_picker_demo_result_none),
                        color = if (savedFile == null) {
                            AppTheme.colorScheme.textSecondary
                        } else {
                            NomiTheme.colorScheme.onSurface
                        },
                        style = NomiTheme.typography.bodyMedium,
                    )
                }
            }

            if (isCameraPickerSupported) {
                item {
                    ResultSection(
                        title = stringResource(Res.string.file_picker_demo_camera_result_title),
                    ) {
                        NomiText(
                            text = capturedFile?.path
                                ?: stringResource(Res.string.file_picker_demo_result_none),
                            color = if (capturedFile == null) {
                                AppTheme.colorScheme.textSecondary
                            } else {
                                NomiTheme.colorScheme.onSurface
                            },
                            style = NomiTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 结果分组：标题 + 内容。
 *
 * @param title 分组标题。
 * @param content 分组内容。
 */
@Composable
private fun ResultSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
    ) {
        NomiText(
            text = title,
            fontWeight = FontWeight.SemiBold,
            style = NomiTheme.typography.titleMedium,
        )
        content()
    }
}
