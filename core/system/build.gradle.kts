plugins {
    `multiplatform-lib-targets`
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(libs.kotlinx.datetime)
        api(libs.directories)
        api(projects.core.platform)
        api(projects.core.io)
    }

    sourceSets.desktopMain.dependencies {
        api(libs.jna)
        api(libs.jna.platform)
    }

}
