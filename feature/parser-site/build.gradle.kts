import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    androidLibrary {
        namespace = "ciyin.feature.parser.site"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.feature.parser)
    }
    sourceSets.androidDeviceTest.dependencies {
        api(kotlin("test"))
        api(libs.androidx.test.junit)
        api(libs.androidx.espresso.core)
        api(libs.kotlinx.coroutines.test)
    }
}
