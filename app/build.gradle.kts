import java.net.URI
import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)  // com.android.application
    alias(libs.plugins.kotlin.android)       // org.jetbrains.kotlin.android
    alias(libs.plugins.kotlin.compose)       // org.jetbrains.kotlin.plugin.compose
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

fun String.asBuildConfigString(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

val configuredApiBaseUrl = providers.gradleProperty("API_BASE_URL")
    .orElse("https://api.example.invalid/")
    .get()
val debugApiBaseUrl = providers.gradleProperty("API_BASE_URL")
    .orElse("http://10.0.2.2:8080/")
    .get()
val supportEmail = providers.gradleProperty("SUPPORT_EMAIL")
    .orElse("support@example.test")
    .get()
val enableLogging = providers.gradleProperty("ENABLE_LOGGING")
    .orElse("false")
    .get()

fun validateReleaseApiBaseUrlValue(value: String) {
    val uri = try {
        URI(value)
    } catch (_: Exception) {
        throw GradleException("Release API_BASE_URL must be an absolute HTTPS Retrofit base URL with a host, no user-info, and a trailing slash.")
    }
    val validPort = uri.port == -1 || uri.port in 1..65535
    val valid = uri.isAbsolute &&
        uri.scheme.equals("https", ignoreCase = true) &&
        !uri.host.isNullOrBlank() &&
        uri.rawUserInfo == null &&
        uri.rawPath.endsWith("/") &&
        validPort
    if (!valid) {
        throw GradleException("Release API_BASE_URL must be an absolute HTTPS Retrofit base URL with a host, no user-info, and a trailing slash.")
    }
}

val validateReleaseApiBaseUrlTask = tasks.register("validateReleaseApiBaseUrl") {
    group = "verification"
    doLast {
        validateReleaseApiBaseUrlValue(configuredApiBaseUrl)
    }
}

android {
    namespace = "com.bankingsystem.mobile"
    compileSdk = 36
    compileSdkExtension = 20
    buildToolsVersion = "36.1.0"

    defaultConfig {
        applicationId = "com.bankingsystem.mobile"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Release builds must receive a real endpoint through -PAPI_BASE_URL;
        // the placeholder prevents an emulator-only host from shipping by default.
        buildConfigField("String", "API_BASE_URL", configuredApiBaseUrl.asBuildConfigString())
        buildConfigField("String", "SUPPORT_EMAIL", supportEmail.asBuildConfigString())
        buildConfigField("boolean", "ENABLE_LOGGING", enableLogging)
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", debugApiBaseUrl.asBuildConfigString())
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    kotlinOptions {
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

tasks.configureEach {
    if (name == "preReleaseBuild") {
        dependsOn(validateReleaseApiBaseUrlTask)
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
dependencies {
    // Android core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.core.splashscreen)

    // Compose UI dependencies
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.browser)
    implementation(libs.play.services.mlkit.barcode.scanning)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Material design dependencies
    implementation(libs.material)
    implementation(libs.material.icons.extended)

    // Network and data storage dependencies
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation dependencies
    implementation(libs.androidx.navigation.compose)

    // Image loading dependencies
    implementation(libs.coil.compose)

    // Face detection dependencies
    implementation(libs.face.detection)

    // --- Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.converter.moshi)
    implementation(libs.moshi.kotlin)

}

kapt { correctErrorTypes = true }
