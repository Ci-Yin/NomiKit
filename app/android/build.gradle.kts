import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(libs.plugins.android.application)
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
    id(libs.plugins.kotlin.android)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

android {
    namespace = getProperty("android.namespace")

    defaultConfig {
        applicationId = getProperty("android.applicationid")
        minSdk = getIntProperty("android.min.sdk")
        compileSdk = getIntProperty("android.compile.sdk")
        versionCode = getIntProperty("android.version.code")
        versionName = getProperty("android.version.name")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
            pickFirsts += "META-INF/io.netty.versions.properties"
            pickFirsts += "META-INF/some/other-duplicate.properties"
        }
    }
    signingConfigs {
        release {
            storeFile = file(getLocalProperty("android.store.file") ?: "")
            storePassword = getLocalProperty("android.store.password")
            keyAlias = getLocalProperty("android.key.alias")
            keyPassword = getLocalProperty("android.key.password")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.release
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

dependencies {
    implementation(projects.app.shared)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}