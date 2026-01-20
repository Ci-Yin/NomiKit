plugins {
    `multiplatform-lib-targets`
}

android {
    namespace = "ciyin.core.lang"
}

commonMainDependencies {
    api(libs.jetbrains.annotations)
}
