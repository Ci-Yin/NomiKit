import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    androidLibrary {
        namespace = "ciyin.component.koin"
    }

    sourceSets.commonMain.dependencies {
        api(libs.koin.core)
        api(projects.core.serialization)
    }

}
