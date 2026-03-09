import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}
kotlin {

    androidLibrary {
        namespace = "ciyin.core.serialization"
    }

    sourceSets.commonMain.dependencies {
        api(libs.kotlinx.serialization.core)
        api(libs.kotlinx.serialization.json)
        api(projects.core.io)
    }

    sourceSets.jvmMain.dependencies {
        api(libs.gson)
        api(libs.moshi.kotlin)
    }

}
