import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
}

kotlin {

    androidLibrary {
        namespace = "ciyin.core.testing"
    }

    sourceSets.commonMain.dependencies {
        api(kotlin("test-annotations-common", libs.versions.kotlin.get()))
        api(libs.kotlinx.coroutines.test)
    }

    sourceSets.jvmMain.dependencies {
        api(kotlin("test-junit5", libs.versions.kotlin.get()))
    }

}
