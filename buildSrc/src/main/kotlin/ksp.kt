import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/4 21:21
 */


fun Project.kspCommonMain(vararg dependencyNotations: Any) {
    if (dependencyNotations.isEmpty()) return
    dependencies {
        dependencyNotations.forEach {
            add("kspCommonMainMetadata", it)
        }
    }
    kotlinSourceSets?.let { sourceSets ->
        sourceSets.named("commonMain").configure {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
    }
    tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }
        .configureEach {
            dependsOn("kspCommonMainKotlinMetadata")
        }
}

fun Project.kspPlatformMain(vararg dependencyNotations: Any) {
    if (dependencyNotations.isEmpty()) return
    dependencies {
        dependencyNotations.forEach {
            add("kspAndroid", it)
            if (enableDesktop) {
                add("kspDesktop", it)
            }
            if (enableIos) {
                add("kspIosArm64", it)
                add("kspIosSimulatorArm64", it)
            }
        }
    }
}