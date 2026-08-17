plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(compose.runtime)
        api(compose.ui)
        api(libs.kermit)
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.coroutines.core)
        api(projects.core.lang)
        api(projects.core.io)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.kotlinx.coroutines.android)
    }

    sourceSets.desktopMain.dependencies {
        api(libs.jna)
        api(libs.jna.platform)
        implementation(libs.kotlinx.coroutines.swing)
    }

}
