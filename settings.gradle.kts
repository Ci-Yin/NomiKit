rootProject.name = "NomiKit"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":app")
include(":app:android")
include(":app:desktop")
include(":app:ios")
include(":app:web")
include(":app:shared")

include(":component")
include(":component:koin")
include(":component:room")
include(":component:data-store")

include(":core")
include(":core:io")
include(":core:lang")
include(":core:platform")
include(":core:datastore")
include(":core:coroutines")
include(":core:application")
include(":core:serialization")
include(":core:system")
include(":core:testing")
include(":core:ui-preview")
include(":core:ui-foundation")

include(":feature")
include(":feature:kotlin-script")
include(":feature:parser")
include(":feature:parser-site")
include(":feature:sample")
include(":feature:sdwebui")
include(":feature:ai-core")
include(":feature:ai-facade")
include(":feature:ai-image-sdwebui-engine")
include(":feature:ai-chat-openai-engine")
