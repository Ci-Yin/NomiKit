plugins {
    `multiplatform-lib-targets`
}

kotlin {

    sourceSets.commonMain.dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(projects.core.platform)
    }

    sourceSets.desktopMain.dependencies {
        implementation(libs.ivy)
        implementation(kotlin("script-runtime"))
        implementation(libs.bundles.kotlin.scripting)
    }

}
