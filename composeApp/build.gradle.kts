import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.3.0"
    id("com.google.gms.google-services")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

            // Ktor for HTTP client
            implementation("io.ktor:ktor-client-core:3.0.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
            implementation("io.ktor:ktor-client-logging:3.0.1")

            // Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

            // Coil for image loading (Google profile photo)
            implementation("io.coil-kt.coil3:coil-compose:3.0.4")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.0.4")

            // Navigation Compose (multiplatform)
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.2")
        }

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)

            // Google Sign-In Legacy API
            implementation("com.google.android.gms:play-services-auth:21.2.0")

            // Credential Manager for future use
            implementation("androidx.credentials:credentials:1.3.0")
            implementation("androidx.credentials:credentials-play-services-auth:1.3.0")

            // Firebase Authentication - Android only
            implementation("com.google.firebase:firebase-auth:23.1.0")

            // Ktor Android engine — OkHttp preferred over CIO:
            // better TLS connection reuse, and CIO has a known race on
            // concurrent handshakes that surfaces as `EOFException: Not
            // enough data available`.
            implementation("io.ktor:ktor-client-okhttp:3.0.1")

            // Coroutines Play Services (Task.await())
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

            // ExoPlayer (media3) — MediaPlayer no reproduce bien webm/opus
            // (los audios de las ideas se graban en el web con MediaRecorder).
            implementation("androidx.media3:media3-exoplayer:1.4.1")
        }

        iosMain.dependencies {
            // Ktor iOS engine
            implementation("io.ktor:ktor-client-darwin:3.0.1")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
        }
    }
}

val localProperties = Properties().apply {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) load(localPropsFile.inputStream())
}

android {
    namespace = "com.nitanmal.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.nitanmal.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.1"

        buildConfigField("String", "ADMIN_API_KEY", "\"${localProperties["ADMIN_API_KEY"] ?: ""}\"")
        buildConfigField("String", "WEB_CLIENT_ID", "\"${localProperties["WEB_CLIENT_ID"] ?: ""}\"")
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        // Credenciales en local.properties (no se comitean).
        val ksFile = localProperties["KEYSTORE_FILE"]?.toString()
        if (ksFile != null && file(ksFile).exists()) {
            create("release") {
                storeFile = file(ksFile)
                storePassword = localProperties["KEYSTORE_PASSWORD"]?.toString()
                keyAlias = localProperties["KEY_ALIAS"]?.toString()
                keyPassword = localProperties["KEY_PASSWORD"]?.toString()
            }
        }
    }
    buildTypes {
        getByName("release") {
            // R8: reduce y optimiza código y recursos para Play Store.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}
