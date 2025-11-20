plugins {
    `multiplatform-lib-targets`
}

android {
    namespace = "ciyin.core.io"
}

commonMainDependencies {
    api(projects.core.lang)
    api(libs.kotlinx.coroutines.core)
    api("com.squareup.okio:okio:3.11.0")
}

webMainDependencies {

}

