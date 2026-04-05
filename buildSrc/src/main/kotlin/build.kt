import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetsContainer
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.io.File
import kotlin.jvm.optionals.getOrNull

/**
 * Gradle 子工程通用构建辅助：Kotlin 语言版本与 opt-in、JVM 目标与工具链、JUnit5 测试依赖、
 * Android KMP 库 `namespace` 推导，以及 `:app:shared` 的 ProGuard 规则收集等。
 *
 * 供 `buildSrc` 内其它预编译脚本与约定插件复用。
 */

/**
 * 收集 `:app:shared` 目录下已存在的 ProGuard / 规则文件，供 Android 打包时一并引用。
 *
 * @return 存在的规则文件数组；若目录下没有任何预期文件则抛出异常
 */
fun Project.sharedAndroidProguardRules(): Array<File> {
    val dir = project(":app:shared").projectDir
    return listOf(
        dir.resolve("proguard-rules.pro"),
        dir.resolve("kotlinx-coroutines.pro"),
        dir.resolve("kotlinx-serialization.pro"),
        dir.resolve("proguard-rules-keep-names.pro"),
    ).filter {
        it.exists()
    }.toTypedArray().also {
        check(it.isNotEmpty()) {
            "No proguard rules found in $dir"
        }
    }
}

/**
 * 应用于**测试相关**源码集（名称含 `test`）的额外 `optIn` 注解全限定名列表。
 */
val testOptInAnnotations = arrayOf(
    "kotlin.ExperimentalUnsignedTypes",
    "kotlin.time.ExperimentalTime",
    "io.ktor.util.KtorExperimentalAPI",
    "kotlin.io.path.ExperimentalPathApi",
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "kotlinx.serialization.ExperimentalSerializationApi",
    "ciyin.core.testing.annotations.TestOnly",
    "androidx.compose.ui.test.ExperimentalTestApi",
)

/**
 * 应用于**主源码**（非测试专用列表）的 `optIn` 注解全限定名列表，与工程内 kotlinx/Compose 等实验 API 使用保持一致。
 */
val optInAnnotations = arrayOf(
    "kotlin.contracts.ExperimentalContracts",
    "kotlin.experimental.ExperimentalTypeInference",
    "kotlin.uuid.ExperimentalUuidApi",
    "kotlinx.serialization.ExperimentalSerializationApi",
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "kotlinx.coroutines.FlowPreview",
    "kotlinx.cinterop.ExperimentalForeignApi",
    "androidx.compose.foundation.layout.ExperimentalLayoutApi",
    "androidx.compose.foundation.ExperimentalFoundationApi",
    "androidx.compose.material3.ExperimentalMaterial3Api",
    "androidx.compose.ui.ExperimentalComposeUiApi",
//    "org.jetbrains.compose.resources.ExperimentalResourceApi",
    "kotlin.ExperimentalStdlibApi",
//    "androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
    "androidx.compose.animation.ExperimentalSharedTransitionApi",
//    "androidx.paging.ExperimentalPagingApi",
    "kotlin.ExperimentalSubclassOptIn",
)

/**
 * 仅在**测试源码集**上启用的 Kotlin 语言特性名称，由 [Project.configureKotlinOptIns] 内部对测试源集调用启用逻辑。
 */
val testLanguageFeatures: List<String> = listOf(
    "ContextParameters",
)

/**
 * 为当前工程所有 Kotlin 源码集递归配置 [configureKotlinOptIns]，并根据 Version Catalog 中的 Kotlin 版本
 * 统一设置 [KotlinCommonCompilerOptions.languageVersion]（含 `KotlinCompile` 任务）。
 *
 * 同时为 [testLanguageFeatures] 中列出的特性在测试源集上启用语言特性。
 */
fun Project.configureKotlinOptIns() {
    val sourceSets = kotlinSourceSets ?: return
    sourceSets.all {
        configureKotlinOptIns()
    }

    val libs = versionCatalogLibs()
    val (major, minor) = libs["kotlin"].split('.')
    val kotlinVersion = KotlinVersion.valueOf("KOTLIN_${major}_${minor}")

    val options = kotlinCommonCompilerOptions()
    options.apply {
        languageVersion.set(kotlinVersion)
    }
    // ksp task extends KotlinCompile
    project.tasks.withType(KotlinCompile::class.java) {
        @Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")
        compilerOptions.languageVersion.set(kotlinVersion)
    }

    for (name in testLanguageFeatures) {
        enableLanguageFeatureForTestSourceSets(name)
    }
}

/**
 * 获取本工程使用的 `libs` [VersionCatalog]。
 */
private fun Project.versionCatalogLibs(): VersionCatalog =
    project.extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * 按版本别名读取显示版本字符串（如 `libs.versions.toml` 中的 `kotlin`）。
 */
private operator fun VersionCatalog.get(name: String): String = findVersion(name).get().displayName

/**
 * 解析 Version Catalog 中的库坐标字符串；不存在时抛出异常。
 */
private fun VersionCatalog.getLibrary(name: String): String =
    findLibrary(name).getOrNull()?.orNull?.toString()
        ?: error("Library $name not found in version catalog")

/**
 * 解析应用 `multiplatform-lib-targets` 的模块在 Android KMP 目标上应使用的 `namespace`。
 *
 * 按 [Project.path] 推导默认 Android `namespace`（不含属性与例外表覆盖）。
 * 规则：`path` 以 `:` 分段并去掉首空段；首段为 `app` 时使用前缀 `com.ciyin`，否则为 `ciyin`；各段内若有 `-`，则按小驼峰拼接（例如 `ui-foundation` → `uiFoundation`），无 `-` 的段保持原样；段与段之间仍以 `.` 连接。
 *
 * 模块仍可在自身 `kotlin { android { namespace = ... } }` 中最后赋值以覆盖本函数的解析结果。
 */
fun Project.resolveAndroidKmpLibraryNamespace(): String {
    fun String.pathSegmentToNamespacePiece(): String {
        val parts = split('-')
        if (parts.size <= 1) return this
        return buildString {
            append(parts[0])
            for (i in 1 until parts.size) {
                val piece = parts[i]
                if (piece.isNotEmpty()) {
                    append(piece.replaceFirstChar { it.uppercaseChar() })
                }
            }
        }
    }

    val segments = path.removePrefix(":").split(':').filter { it.isNotEmpty() }
    check(segments.isNotEmpty()) { "无法从 path=$path 推导 Android KMP namespace" }
    val prefix = if (segments.first() == "app") "com.ciyin" else "ciyin"
    val tail = segments.joinToString(".") { it.pathSegmentToNamespacePiece() }
    return "$prefix.$tail"
}

/**
 * 根据当前工程已应用的 Kotlin Gradle 扩展类型，返回共用的 [KotlinCommonCompilerOptions]。
 */
private fun Project.kotlinCommonCompilerOptions(): KotlinCommonCompilerOptions =
    when (val ext = kotlinExtension) {
        is KotlinJvmProjectExtension -> ext.compilerOptions
        is KotlinAndroidProjectExtension -> ext.compilerOptions
        is KotlinMultiplatformExtension -> ext.compilerOptions
        else -> error("Unsupported kotlinExtension: ${ext::class}")
    }

/**
 * 为单个 [KotlinSourceSet] 开启 progressive 模式，并注册 [optInAnnotations]；
 * 若源集名称包含 `test`（忽略大小写），额外注册 [testOptInAnnotations]。
 */
fun KotlinSourceSet.configureKotlinOptIns() {
    languageSettings.progressiveMode = true
    optInAnnotations.forEach { a ->
        languageSettings.optIn(a)
    }
    if (name.contains("test", ignoreCase = true)) {
        testOptInAnnotations.forEach { a ->
            languageSettings.optIn(a)
        }
    }
}

/**
 * 从 `jvm.toolchain.vendor` 工程属性解析得到的 JVM 工具链厂商；未配置时为 `null`。
 */
val Project.DEFAULT_JVM_TOOLCHAIN_VENDOR
    get() = getPropertyOrNull("jvm.toolchain.vendor")?.let { JvmVendorSpec.matching(it) }

/**
 * 解析本工程优先使用的 Java 语言级别：优先读取 `extra["multiplatform.jvm.target"]`，否则使用 `jvm.toolchain.version`（缺省 17）。
 */
private fun Project.getProjectPreferredJvmTargetVersion() =
    extra.runCatching { get("multiplatform.jvm.target") }.fold(
        onSuccess = { JavaVersion.toVersion(it.toString()) },
        onFailure = {
            JavaVersion.toVersion(
                getPropertyOrNull("jvm.toolchain.version")?.toInt() ?: 17
            )
        },
    )

/**
 * 将 [getProjectPreferredJvmTargetVersion] 同步到 Kotlin JVM/Android 编译任务、JavaCompile、
 * Kotlin/Java 插件扩展的工具链，以及各 Kotlin 目标的编译参数（含部分 `freeCompilerArgs`）和 AGP [CommonExtension]。
 */
fun Project.configureJvmTarget() {
    val ver = getProjectPreferredJvmTargetVersion()
    logger.info("JVM target for project ${this.path} is: $ver")

    // 我也不知道到底设置谁就够了, 反正全都设置了

    tasks.withType(KotlinJvmCompile::class.java) {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(ver.toString()))
    }

    tasks.withType(KotlinCompile::class.java) {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(ver.toString()))
    }

    tasks.withType(JavaCompile::class.java) {
        sourceCompatibility = ver.toString()
        targetCompatibility = ver.toString()
    }

    extensions.findByType(KotlinProjectExtension::class)?.apply {
        jvmToolchain {
            vendor.set(DEFAULT_JVM_TOOLCHAIN_VENDOR)
            languageVersion.set(JavaLanguageVersion.of(ver.getMajorVersion()))
        }
    }

    extensions.findByType(JavaPluginExtension::class)?.apply {
        toolchain {
            vendor.set(DEFAULT_JVM_TOOLCHAIN_VENDOR)
            languageVersion.set(JavaLanguageVersion.of(ver.getMajorVersion()))
            sourceCompatibility = ver
            targetCompatibility = ver
        }
    }

    withKotlinTargets {
        it.compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xdont-warn-on-error-suppression")
                    freeCompilerArgs.add("-Xannotation-target-all")
                    freeCompilerArgs.add("-Xmulti-dollar-interpolation")
                }
            }
            if (this is KotlinJvmAndroidCompilation) {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.fromTarget(ver.toString()))
                    }
                }
            }
        }
    }

    extensions.findByType(JavaPluginExtension::class.java)?.run {
        sourceCompatibility = ver
        targetCompatibility = ver
    }

    // AGP 9+ 在 buildSrc 编译 classpath 上通常不提供带 lambda 的 Kotlin DSL 扩展，需直接访问 API 属性。
    extensions.findByType(CommonExtension::class.java)?.run {
        compileOptions.sourceCompatibility = ver
        compileOptions.targetCompatibility = ver
    }
}

/**
 * 将本工程所有 [JavaCompile] 任务的源文件编码设为 UTF-8。
 */
fun Project.configureEncoding() {
    tasks.withType(JavaCompile::class.java) {
        options.encoding = "UTF8"
    }
}

/**
 * 统一测试运行器与依赖：JUnit Platform、`jvm` 目标测试任务，以及纯 JVM / KMP（含 Android 仪器测试）场景下的
 * JUnit5 与 kotlinx 测试相关坐标。
 */
fun Project.configureKotlinTestSettings() {
    tasks.withType(Test::class) {
        useJUnitPlatform()
    }

    val libs = versionCatalogLibs()

    allKotlinTargets().all {
        if (this !is KotlinJvmTarget) return@all
        this.testRuns["test"].executionTask.configure { useJUnitPlatform() }
    }

    val b = "Auto-set for project '${project.path}'. (configureKotlinTestSettings)"
    when {
        isKotlinJvmProject -> {
            dependencies {
                "testImplementation"(kotlin("test-junit5"))?.because(b)

                "testImplementation"(libs.getLibrary("junit5-jupiter-api"))?.because(b)
                "testRuntimeOnly"(libs.getLibrary("junit5-jupiter-engine"))?.because(b)
            }
        }

        isKotlinMpp -> {
            if (allKotlinTargets().any { it.platformType == KotlinPlatformType.androidJvm }) {
                // has android target, configure instrumented test
                // this must be added to `androidTest`, instead of just `androidInstrumentedTest`
                project.dependencies {
                    "androidTestImplementation"(libs.getLibrary("androidx-test-runner"))
                    "androidTestImplementation"(libs.getLibrary("junit5-android-test-core"))
                    "androidTestRuntimeOnly"(libs.getLibrary("junit5-android-test-runner"))

                    "androidTestImplementation"(libs.getLibrary("junit5-jupiter-api"))
                    "androidTestRuntimeOnly"(libs.getLibrary("junit5-jupiter-engine"))
                }
            }

            kotlinSourceSets?.all {
                val sourceSet = this

                val target = allKotlinTargets()
                    .find {
                        it.name == sourceSet.name.substringBeforeLast("Main")
                            .substringBeforeLast("Test")
                    }

                if (sourceSet.name.contains("test", ignoreCase = true)) {
                    when {
                        target?.platformType == KotlinPlatformType.jvm -> {
                            // For android, this should be done differently. See Android.kt
                            sourceSet.configureJvmTest(b)
                        }

                        sourceSet.name == "commonTest" -> {
                            sourceSet.dependencies {
                                implementation(kotlin("test-annotations-common"))?.because(b)
                            }
                        }

                        target?.platformType == KotlinPlatformType.androidJvm
                                || sourceSet.name == "androidInstrumentedTest"
                                || sourceSet.name == "androidUnitTest" -> {
                            sourceSet.configureJvmTest(b)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 为基于 JVM 的测试源码集添加 JUnit5 与 Kotlin `test-junit5` 依赖。
 *
 * @param because 写入 Gradle 依赖原因字段，便于构建扫描中追溯来源
 */
fun KotlinSourceSet.configureJvmTest(because: String) {
    val libs = project.versionCatalogLibs()
    dependencies {
        implementation(kotlin("test-junit5"))?.because(because)

        // also see above for androidInstrumentedTest
        implementation(libs.getLibrary("junit5-jupiter-api"))?.because(because)
        runtimeOnly(libs.getLibrary("junit5-jupiter-engine"))?.because(because)

        // TODO: if we need to run junit4 tests (especially ui tests), add this.
//        runtimeOnly("junit:junit:4.13.2")?.because(because)
//        runtimeOnly("org.junit.vintage:junit-vintage-engine:${JUNIT_VERSION}")?.because(because)
    }
}

/**
 * 若当前工程存在 [KotlinTargetsContainer] 扩展，则对其中的每个 [KotlinTarget] 执行 [fn]。
 *
 * @param fn 针对每个目标的回调
 */
fun Project.withKotlinTargets(fn: (KotlinTarget) -> Unit) {
    extensions.findByType(KotlinTargetsContainer::class.java)?.let { kotlinExtension ->
        // find all compilations given sourceSet belongs to
        kotlinExtension.targets
            .all {
                fn(this)
            }
    }
}