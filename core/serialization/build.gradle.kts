plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ciyin.core.serialization"
}

androidMainDependencies {

}

commonMainDependencies {
    api(libs.kotlinx.serialization.json)
    api(projects.core.io)
}

jvmMainDependencies {
    api(libs.gson)
    api(libs.moshi.kotlin)
}
