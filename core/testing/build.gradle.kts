plugins {
    `multiplatform-lib-targets`
}

commonMainDependencies {
    api(kotlin("test-annotations-common", libs.versions.kotlin.get()))
    api(libs.kotlinx.coroutines.test)
}

jvmMainDependencies {
    api(kotlin("test-junit5", libs.versions.kotlin.get()))
}

android {
    namespace = "ciyin.core.testing"
}