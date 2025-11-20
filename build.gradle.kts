plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    id(libs.plugins.android.application) apply false
    id(libs.plugins.android.library) apply false
    id(libs.plugins.jetbrains.compose) apply false
    id(libs.plugins.compose.compiler) apply false
    id(libs.plugins.kotlin.multiplatform) apply false
    id(libs.plugins.kotlin.jvm) apply false
    id(libs.plugins.kotlin.android) apply false
    id(libs.plugins.kotlin.serialization) version libs.versions.kotlin apply false
    id(libs.plugins.ksp) version libs.versions.ksp apply false
}
