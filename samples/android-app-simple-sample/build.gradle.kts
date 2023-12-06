plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.fbase.options.gradle.plugin)
}

android {
    namespace = "com.example.samplefbase"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.samplefbase"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

firebaseOptions {
    source = providers.propertiesFile(layout.projectDirectory.file("firebase.properties"))
    targetPackage = "com.example.samplefbase.config"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
