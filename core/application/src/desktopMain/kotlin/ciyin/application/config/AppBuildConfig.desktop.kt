package ciyin.application.config

import ciyin.system.storage.AppInfo

/**
 * 将桌面端构建配置转换为目录解析所需的应用身份。
 */
fun AppBuildConfig.toDesktopAppInfo(): AppInfo {
    val desktopBuildConfig = this as? DesktopAppBuildConfig
    return if (
        desktopBuildConfig != null &&
        !desktopBuildConfig.organization.isNullOrBlank() &&
        !desktopBuildConfig.name.isNullOrBlank()
    ) {
        AppInfo.OrganizationName(
            qualifier = id.substringBefore('.'),
            organization = desktopBuildConfig.organization,
            name = desktopBuildConfig.name,
        )
    } else {
        AppInfo.ApplicationId(id)
    }
}
