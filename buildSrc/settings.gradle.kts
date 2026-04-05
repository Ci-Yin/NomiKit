/** 显式设置 buildSrc 工程名，避免类型安全项目访问器在 Gradle 9+ 下的缓存/生成路径随检出目录漂移。 */
rootProject.name = "buildSrc"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}