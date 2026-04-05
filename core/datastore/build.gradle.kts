plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}
kotlin {

    sourceSets.commonMain.dependencies {
        api(projects.core.serialization)
    }

}

