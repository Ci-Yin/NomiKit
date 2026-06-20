package ciyin.application.config

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 当前应用的编译期注入构建配置。
 */
val LocalAppBuildConfig = staticCompositionLocalOf<AppBuildConfig> {
    AppBuildConfig
}
