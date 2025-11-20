plugins {
    `multiplatform-lib-targets`
}

android {
    namespace = "ciyin.core.lang"
}

commonMainDependencies {
    api("org.jetbrains:annotations:26.0.0")
}
