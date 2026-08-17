package ciyin.media.library

import ciyin.koin.configuration.koinAutoConfiguration
import ciyin.koin.configuration.onMissInstances
import org.koin.core.module.dsl.singleOf

/** 系统媒体库 KoinBoot 自动配置。 */
internal val MediaLibraryAutoConfiguration = koinAutoConfiguration {
    module {
        onMissInstances<MediaLibrary> {
            singleOf(::createMediaLibrary)
        }
    }
}
