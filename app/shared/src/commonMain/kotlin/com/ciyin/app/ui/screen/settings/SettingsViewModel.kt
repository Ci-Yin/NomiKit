package com.ciyin.app.ui.screen.settings

import androidx.lifecycle.viewModelScope
import ciyin.foundation.viewmodel.AbsMvvmViewModel
import ciyin.platform.platform
import ciyin.system.coroutines.IO
import com.ciyin.app.data.setting.datasource.SettingsDataStorage
import com.ciyin.app.data.setting.model.SettingLocalData
import com.ciyin.app.util.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import rpa.app.shared.generated.resources.Res
import rpa.app.shared.generated.resources.settings_jdk_dir
import rpa.app.shared.generated.resources.settings_start_in_tray
import rpa.app.shared.generated.resources.settings_startup
import rpa.app.shared.generated.resources.settings_web_driver
import rpa.app.shared.generated.resources.settings_windows_driver

/**
 * 设置页面 ViewModel
 */
class SettingsViewModel : AbsMvvmViewModel<SettingsUiState, SettingsEffect>(null), KoinComponent {

    private val dataStorage by inject<SettingsDataStorage>()

    private val data get() = dataStorage.snapshot

    override fun initState(): SettingsUiState = SettingsUiState(
        settings = listOf(
            SettingItem.Switch(
                id = SettingLocalData::isStartup.name,
                title = Res.string.settings_startup.value,
                checked = data.isStartup
            ),
            SettingItem.Switch(
                id = SettingLocalData::startInTray.name,
                title = Res.string.settings_start_in_tray.value,
                checked = data.startInTray
            ),
            SettingItem.Directory(
                id = SettingLocalData::jdkDir.name,
                title = Res.string.settings_jdk_dir.value,
                path = data.jdkDir
            ),
            SettingItem.FileSelect(
                id = SettingLocalData::windowsDriverPath.name,
                title = Res.string.settings_windows_driver.value,
                path = data.windowsDriverPath
            ),
            SettingItem.FileSelect(
                id = SettingLocalData::webDriverPath.name,
                title = Res.string.settings_web_driver.value,
                path = data.webDriverPath
            )
        )
    )


    fun onSettingItemClick(setting: SettingItem) {
        // 处理点击事件
    }

    fun onSwitchChanged(setting: SettingItem.Switch, checked: Boolean) {

        // 设置开机启动
        if (setting.id == SettingLocalData::isStartup.name) {
            viewModelScope.launch(Dispatchers.IO) {
                platform.setAutoStartup(checked)
            }
        }

        updateSettingItem(setting.id) { item ->
            if (item is SettingItem.Switch) item.copy(checked = checked) else item
        }

        dataStorage.updateProperty(setting.id, checked)
    }

    fun onPathChanged(setting: SettingItem, path: String) {
        updateSettingItem(setting.id) { item ->
            when (item) {
                is SettingItem.Directory -> item.copy(path = path)
                is SettingItem.FileSelect -> item.copy(path = path)
                else -> item
            }
        }

        dataStorage.updateProperty(setting.id, path)
    }

    fun onChoiceSelected(setting: SettingItem.Choice, index: Int) {
        updateSettingItem(setting.id) { item ->
            if (item is SettingItem.Choice) item.copy(selectedIndex = index) else item
        }
        dataStorage.updateProperty(setting.id, index)
    }

    private fun updateSettingItem(id: String, transform: (SettingItem) -> SettingItem) {
        updateState {
            copy(
                settings = settings.map { item ->
                    if (item.id == id) transform(item) else item
                }
            )
        }
    }

}