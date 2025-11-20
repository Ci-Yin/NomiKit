package com.ciyin.app.ui.screen.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.compose.SettingDefaults
import ciyin.compose.SettingItem
import ciyin.compose.SettingSwitch
import ciyin.foundation.viewmodel.viewModel
import com.ciyin.app.ui.component.AppPreview
import com.ciyin.app.ui.component.CustomTextField
import com.ciyin.app.ui.component.IconButton2
import com.ciyin.app.ui.component.Screen
import com.ciyin.app.ui.component.common.Toolbar
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview
import rpa.app.shared.generated.resources.Res
import rpa.app.shared.generated.resources.settings_screen_title

private val SettingStyle
    @Composable
    get() = SettingDefaults.settingStyle(
        titleTextStyle = SettingDefaults.TitleTextStyle.copy(fontSize = 16.sp)
    )

private val SettingItemContentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
private val SettingModifier = Modifier.height(45.dp).clip(RoundedCornerShape(10.dp))

@Composable
fun SettingsScreen(
    vm: SettingsViewModel = viewModel(::SettingsViewModel)
) {
    val uiState by vm.state.collectAsStateWithLifecycle()

    SettingsContent(
        uiState = uiState,
        onSettingItemClick = vm::onSettingItemClick,
        onSwitchChanged = vm::onSwitchChanged,
        onPathChanged = vm::onPathChanged,
        onChoiceSelected = vm::onChoiceSelected
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onSettingItemClick: (SettingItem) -> Unit,
    onSwitchChanged: (SettingItem.Switch, Boolean) -> Unit,
    onPathChanged: (SettingItem, String) -> Unit,
    onChoiceSelected: (SettingItem.Choice, Int) -> Unit
) = Screen(
    title = stringResource(Res.string.settings_screen_title),
    toolbar = { Toolbar() }
) {
    uiState.settings.forEach { setting ->
        when (setting) {
            is SettingItem.Panel -> {
                // TODO: 实现面板类型
            }

            is SettingItem.Directory -> {
                DirectorySetting(
                    setting = setting,
                    onSettingItemClick = onSettingItemClick,
                    onPathChanged = onPathChanged
                )
            }

            is SettingItem.FileSelect -> {
                FileSetting(
                    setting = setting,
                    onSettingItemClick = onSettingItemClick,
                    onPathChanged = onPathChanged
                )
            }

            is SettingItem.Choice -> {
                // TODO: 实现单选类型
            }

            is SettingItem.Switch -> {
                SettingSwitch(
                    modifier = SettingModifier,
                    title = setting.title,
                    checked = setting.checked,
                    contentPadding = SettingItemContentPadding,
                    style = SettingStyle,
                    onClick = {
                        onSettingItemClick(setting)
                    },
                    onCheckedChange = {
                        onSwitchChanged(setting, it)
                    }
                )
            }

            is SettingItem.Navigation -> {
                // TODO: 实现导航类型
            }

            is SettingItem.Generic -> {
                // TODO: 实现通用类型
            }
        }
    }
}

@Composable
private fun DirectorySetting(
    setting: SettingItem.Directory,
    onSettingItemClick: (SettingItem) -> Unit,
    onPathChanged: (SettingItem, String) -> Unit
) {
    val directoryPickerLauncher = rememberDirectoryPickerLauncher(
        directory = setting.file?.let { PlatformFile(setting.path) }
    ) { platformFile ->
        platformFile?.let {
            onPathChanged(setting, it.path)
        }
    }

    PathSettingContent(
        setting = setting,
        path = setting.path,
        onSettingItemClick = onSettingItemClick,
        onPathChanged = onPathChanged,
        onPickerClick = { directoryPickerLauncher.launch() }
    )
}

@Composable
private fun FileSetting(
    setting: SettingItem.FileSelect,
    onSettingItemClick: (SettingItem) -> Unit,
    onPathChanged: (SettingItem, String) -> Unit
) {
    val filePickerLauncher = rememberFilePickerLauncher { platformFile ->
        platformFile?.let {
            onPathChanged(setting, it.path)
        }
    }

    PathSettingContent(
        setting = setting,
        path = setting.path,
        onSettingItemClick = onSettingItemClick,
        onPathChanged = onPathChanged,
        onPickerClick = { filePickerLauncher.launch() }
    )
}

@Composable
private fun PathSettingContent(
    setting: SettingItem,
    path: String,
    onSettingItemClick: (SettingItem) -> Unit,
    onPathChanged: (SettingItem, String) -> Unit,
    onPickerClick: () -> Unit
) {
    SettingItem(
        modifier = SettingModifier,
        contentPadding = SettingItemContentPadding,
        title = setting.title,
        style = SettingStyle,
        onClick = {
            onSettingItemClick(setting)
        }
    ) {
        CustomTextField(
            modifier = Modifier.width(600.dp),
            value = path,
            singleLine = true,
            onValueChange = {
                onPathChanged(setting, it)
            },
            trailingIcon = {
                IconButton2(Icons.Default.AddCard, fraction = 0.7f) {
                    onPickerClick()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@AppPreview
@Composable
fun SettingsScreenPreview() = AppPreview {
    SettingsContent(
        uiState = SettingsUiState(),
        onSettingItemClick = {},
        onSwitchChanged = { _, _ -> },
        onPathChanged = { _, _ -> },
        onChoiceSelected = { _, _ -> }
    )
}