import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
}

kotlin {

    androidLibrary {
        namespace = "ciyin.core.lang"
    }

    sourceSets.commonMain.dependencies {
        api(libs.jetbrains.annotations)
    }

}
