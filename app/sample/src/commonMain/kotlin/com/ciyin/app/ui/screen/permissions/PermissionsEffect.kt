package com.ciyin.app.ui.screen.permissions

/** 权限管理示例页面副作用。 */
internal sealed interface PermissionsEffect {
    /** 返回 sample 首页。 */
    data object NavigateBack : PermissionsEffect
}
