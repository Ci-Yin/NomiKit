plugins {
    `multiplatform-lib-targets`
}

android {
    namespace = "ciyin.core.coroutines"
}

commonMainDependencies {
    api(libs.kotlinx.coroutines.core)
}
