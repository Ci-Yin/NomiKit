plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

kotlin {
    sourceSets.commonMain.dependencies {
        // Mediamp publishes Compose UI Test as a desktop runtime dependency; it belongs only to test configurations.
        api(mediampLibs.mediamp.api.get().toString()) {
            exclude(group = "org.jetbrains.compose.ui", module = "ui-test-junit4")
        }
        api(mediampLibs.mediamp.source.ktxio.get().toString()) {
            exclude(group = "org.jetbrains.compose.ui", module = "ui-test-junit4")
        }
        implementation(projects.core.lang)
        implementation(projects.core.platform)
        implementation(projects.core.uiFoundation)
        implementation(projects.core.material)
        implementation(libs.androidx.navigation.compose)
        implementation(libs.androidx.navigation.runtime)
    }

    sourceSets.androidMain.dependencies {
        api(mediampLibs.mediamp.exoplayer.asProvider().get().toString()) {
            exclude(group = "org.jetbrains.compose.ui", module = "ui-test-junit4")
        }
        implementation(libs.androidx.media3.ui)
        implementation(libs.androidx.media3.ui.compose)
        implementation(libs.androidx.media3.exoplayer)
        implementation(libs.androidx.media3.exoplayer.dash)
        implementation(libs.androidx.media3.exoplayer.hls)
    }

    sourceSets.desktopMain.dependencies {
        api(mediampLibs.mediamp.vlc.asProvider().get().toString()) {
            exclude(group = "org.jetbrains.compose.ui", module = "ui-test-junit4")
            exclude(group = "net.java.dev.jna", module = "jna-jpms")
            exclude(group = "net.java.dev.jna", module = "jna-platform-jpms")
        }
        implementation(libs.vlcj.get().toString()) {
            exclude(group = "net.java.dev.jna", module = "jna-jpms")
            exclude(group = "net.java.dev.jna", module = "jna-platform-jpms")
        }
    }

    sourceSets.iosMain.dependencies {
        api(mediampLibs.mediamp.avkit)
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "ciyin.video.player.generated.resources"
    generateResClass = always
}
