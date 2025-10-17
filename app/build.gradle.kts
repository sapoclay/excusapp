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

    // Renombrar automáticamente los APKs generados
    applicationVariants.all {
        outputs.all {
            val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val variantName = name
            outputImpl.outputFileName = when {
                variantName.contains("debug", ignoreCase = true) -> "excusApp-debug.apk"
                variantName.contains("release", ignoreCase = true) -> "excusApp-release.apk"
                else -> "excusApp.apk"
            }
        }
    }
}

// Tareas para copiar y renombrar APKs a un directorio separado para evitar conflictos con AGP.
val debugApkDir = layout.buildDirectory.dir("outputs/apk/debug")
val releaseApkDir = layout.buildDirectory.dir("outputs/apk/release")
val renamedApkDir = layout.buildDirectory.dir("renamedApk")

abstract class CopySingleApk : DefaultTask() {
    @get:InputFile
    abstract val srcApk: RegularFileProperty

    @get:OutputFile
    abstract val destApk: RegularFileProperty

    @TaskAction
    fun copyApk() {
        val preferred = srcApk.get().asFile
        val dir = preferred.parentFile
        if (dir == null || !dir.exists()) return
        val source = if (preferred.exists()) preferred else dir.listFiles { f -> f.isFile && f.extension == "apk" }?.firstOrNull() ?: return
        destApk.get().asFile.parentFile.mkdirs()
        source.copyTo(destApk.get().asFile, overwrite = true)
    }
}

// Copia app-debug.apk -> build/renamedApk/excusApp.apk
tasks.register<CopySingleApk>("renameDebugApk") {
    group = "distribution"
    description = "Copia/renombra el APK debug a build/renamedApk/excusApp.apk"
    dependsOn("assembleDebug")
    srcApk.set(debugApkDir.map { it.file("app-debug.apk") })
    destApk.set(renamedApkDir.map { it.file("excusApp.apk") })
}

// Copia app-release.apk -> build/renamedApk/excusApp-release.apk
tasks.register<CopySingleApk>("renameReleaseApk") {
    group = "distribution"
    description = "Copia/renombra el APK release a build/renamedApk/excusApp-release.apk"
    dependsOn("assembleRelease")
    srcApk.set(releaseApkDir.map { it.file("app-release.apk") })
    destApk.set(renamedApkDir.map { it.file("excusApp-release.apk") })
}

// Tareas agregadas que ejecutan assemble + rename de forma explícita
tasks.register("assembleDebugRenamed") {
    group = "distribution"
    description = "Genera el APK debug y deja una copia renombrada en build/renamedApk/"
    dependsOn("renameDebugApk")
}

tasks.register("assembleReleaseRenamed") {
    group = "distribution"
    description = "Genera el APK release y deja una copia renombrada en build/renamedApk/"
    dependsOn("renameReleaseApk")
}

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

    // Exif support para corregir la orientación de imágenes
    implementation("androidx.exifinterface:exifinterface:1.3.6")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}