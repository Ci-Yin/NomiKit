package ciyin

import ciyin.koin.KoinBootInitializer
import ciyin.media.library.MediaLibraryAutoConfiguration

/** 注册系统媒体库 KoinBoot 自动配置。 */
val MediaLibraryBootInitializer: KoinBootInitializer = {
    autoConfigurations(MediaLibraryAutoConfiguration)
}
