import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
}

kotlin {

    androidLibrary {
        namespace = "ciyin.feature.kotlinscript"
    }

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
