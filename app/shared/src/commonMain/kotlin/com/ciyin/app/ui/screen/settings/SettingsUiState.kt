package com.ciyin.app.ui.screen.settings

import androidx.compose.runtime.Immutable

/**
 * 设置页面 UI 状态
 */
@Immutable
data class SettingsUiState(
    val settings: List<SettingItem> = emptyList(),
    val isLoading: Boolean = false
)