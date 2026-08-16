plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(mediampLibs.mediamp.api)
        api(mediampLibs.mediamp.source.ktxio)
        implementation(projects.core.lang)
        implementation(projects.core.platform)
        implementation(projects.core.uiFoundation)
        implementation(projects.core.material)
        implementation(libs.androidx.navigation.compose)
        implementation(libs.androidx.navigation.runtime)
    }

    sourceSets.androidMain.dependencies {
        api(mediampLibs.mediamp.exoplayer)
        implementation(libs.androidx.media3.ui)
        implementation(libs.androidx.media3.ui.compose)
        implementation(libs.androidx.media3.exoplayer)
        implementation(libs.androidx.media3.exoplayer.dash)
        implementation(libs.androidx.media3.exoplayer.hls)
    }

    sourceSets.desktopMain.dependencies {
        api(mediampLibs.mediamp.vlc)
        implementation(libs.vlcj)
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
