plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.parcelize)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.core.platform)
    }

    sourceSets.androidMain.dependencies {
        // 显式传递 Parcelize 运行时，确保消费方的 Release R8 能解析注解类型。
        implementation(libs.kotlin.parcelize.runtime)
        implementation(libs.xxpermissions.get().toString()) {
            exclude(group = "com.android.support")
        }
    }
}
