import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun secretFromLocalOrEnv(name: String): String {
    return localProperties.getProperty(name).orEmpty()
        .ifBlank { System.getenv(name).orEmpty() }
}

fun String.toBuildConfigString(): String {
    return "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""
}

android {
    namespace = "com.example.whatsnextdemo"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.whatsnextdemo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "AI_API_KEY", secretFromLocalOrEnv("AI_API_KEY").toBuildConfigString())
        buildConfigField("String", "AI_API_ENDPOINT", secretFromLocalOrEnv("AI_API_ENDPOINT").toBuildConfigString())
        buildConfigField("String", "AI_MODEL", secretFromLocalOrEnv("AI_MODEL").ifBlank { "deepseek-v4-pro" }.toBuildConfigString())
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(17)
}

val kaptSqliteTmpDir = layout.buildDirectory.dir("tmp/kaptSqlite").get().asFile
tasks.withType<org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask>().configureEach {
    doFirst {
        kaptSqliteTmpDir.mkdirs()
    }
    val tmpPath = kaptSqliteTmpDir.invariantSeparatorsPath
    kaptProcessJvmArgs.add("-Djava.io.tmpdir=$tmpPath")
    kaptProcessJvmArgs.add("-Dorg.sqlite.tmpdir=$tmpPath")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.google.material)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
