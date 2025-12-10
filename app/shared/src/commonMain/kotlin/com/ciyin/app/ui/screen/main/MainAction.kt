package com.ciyin.app.ui.screen.main

/**
 * MainScreen 的所有 UI 事件和用户交互的封装。
 *
 * 采用密封接口（Sealed Interface）将所有可能的输入事件集中管理，
 * 以便在 ViewModel 中通过一个统一的入口函数处理所有事件。
 */
sealed interface MainAction {
    data class ItemAction(val item: MainUiModel) : MainAction // 对应 Toolbar 中的点击事件
}