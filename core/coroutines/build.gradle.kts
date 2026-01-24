import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
}

kotlin {

    androidLibrary {
        namespace = "ciyin.core.coroutines"
    }

    sourceSets.commonMain.dependencies {
        api(libs.kotlinx.coroutines.core)
    }

}

