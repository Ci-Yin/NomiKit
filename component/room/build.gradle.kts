plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    id(libs.plugins.room)
}

android {
    namespace = "ciyin.component.room"
}

// Room 不支持 JS/WASM 平台
// 解决方案：已在 multiplatform-lib-targets 插件中为 :component:room 项目禁用了 JS/WASM 目标
commonMainDependencies {
    implementation(projects.component.koin)
    api(libs.androidx.room.runtime)
    api(libs.sqlite.bundled)
}

// 以下room配置和 `androidx.room` 与 `com.google.devtools.ksp` 插件都是用于测试演示的
room {
    schemaDirectory("${projectDir}/schemas")
}
dependencies {
    listOf(libs.androidx.room.compiler)
        .forEach {
            add("kspDesktopTest", it)
        }
}

