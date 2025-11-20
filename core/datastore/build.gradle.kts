plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ciyin.core.datastore"
}

commonMainDependencies {
    implementation(projects.core.serialization)
}