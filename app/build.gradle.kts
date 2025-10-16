import org.gradle.api.tasks.Copy

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.excusas"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.excusas"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

// Eliminar el bloque androidComponents anterior (causaba referencias no resueltas)
// y usar tareas Gradle para renombrar automáticamente los APKs tras el assemble.

val debugApkDir = layout.buildDirectory.dir("outputs/apk/debug")
val releaseApkDir = layout.buildDirectory.dir("outputs/apk/release")

// Renombra app-debug.apk -> excusApp-debug.apk
tasks.register<Copy>("renameDebugApk") {
    // Ejecutar solo si existe el APK fuente
    onlyIf {
        val src = debugApkDir.get().file("app-debug.apk").asFile
        src.exists()
    }
    from(debugApkDir.map { it.file("app-debug.apk") })
    into(debugApkDir)
    rename("app-debug.apk", "excusApp-debug.apk")
    mustRunAfter("assembleDebug")
}

// Renombra app-release.apk -> excusApp-release.apk
tasks.register<Copy>("renameReleaseApk") {
    // Ejecutar solo si existe el APK fuente
    onlyIf {
        val src = releaseApkDir.get().file("app-release.apk").asFile
        src.exists()
    }
    from(releaseApkDir.map { it.file("app-release.apk") })
    into(releaseApkDir)
    rename("app-release.apk", "excusApp-release.apk")
    mustRunAfter("assembleRelease")
}

// Encadenar las tareas de rename al final de los assemble correspondientes (configuración perezosa)
tasks.matching { it.name == "assembleDebug" }.configureEach { finalizedBy("renameDebugApk") }
tasks.matching { it.name == "assembleRelease" }.configureEach { finalizedBy("renameReleaseApk") }

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Retrofit para llamadas HTTP
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Coroutines para operaciones asíncronas
    implementation(libs.kotlinx.coroutines.android)

    // Lifecycle components
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}