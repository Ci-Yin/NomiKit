plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.parcelize)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.core.platform)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.xxpermissions.get().toString()) {
            exclude(group = "com.android.support")
        }
    }
}
