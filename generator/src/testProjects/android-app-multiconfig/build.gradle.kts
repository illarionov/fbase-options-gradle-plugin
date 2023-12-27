plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.fbase.config.generator.gradle.plugin)
}

android {
    namespace = "com.example.samplefbase"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.samplefbase"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            applicationIdSuffix = ".release"
        }
        create("benchmark") {
            applicationIdSuffix = ".benchmark"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    lint {
        checkOnly += listOf("AnnotationProcessorOnCompilePath")
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.core.ktx)
}

firebaseConfig {
    primaryConfiguration = "firebaseOptions"
    configurations {
        create("firebaseOptions") {
            fromPropertiesFile {
                location = layout.projectDirectory.file("firebase.properties")
            }
            targetPackage = "com.example.samplefbase.config"
        }
        create("firebaseOptionsOrg2") {
            fromPropertiesFile {
                location = layout.projectDirectory.file("firebase2.properties")
            }
            targetPackage = "com.example.samplefbase.config"
            targetFileName = "FirebaseOptionsOrg23"
        }
        create("firebaseOptionsOrg3") {
            fromPropertiesFile {
                location = layout.projectDirectory.file("firebase3.properties")
            }
            targetPackage = "com.example.samplefbase.config"
            targetFileName = "FirebaseOptionsOrg23"
        }
    }
}
