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
        maven("https://jitpack.io") {
            content {
                includeGroup("com.github.getActivity")
            }
        }
    }
    versionCatalogs {
        create("mediampLibs") {
            from("org.openani.mediamp:catalog:0.0.30")
        }
    }
}

include(":app")
include(":app:android")
include(":app:desktop")
include(":app:ios")
include(":app:web")
include(":app:sample")
include(":app:shared")

include(":business")
include(":business:base")

include(":component")
include(":component:koin")
include(":component:room")
include(":component:data-store")
include(":component:media-library")

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
include(":core:material")

include(":feature")
include(":feature:kotlin-script")
include(":feature:serialization")
include(":feature:parser")
include(":feature:parser-site")
include(":feature:permissions")
include(":feature:media-library")
include(":feature:video-player")
include(":feature:sample")
include(":feature:sdwebui")
include(":feature:ai-core")
include(":feature:ai-image-sdwebui-engine")
include(":feature:ai-integrate")
include(":feature:ai-chat-openai-engine")
include(":feature:file-downloader")
