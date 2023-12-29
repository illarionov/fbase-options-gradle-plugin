plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.fbase.config.generator.gradle.plugin)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += listOf("foo", "bar")
    productFlavors {
        create("free") {
            dimension = "foo"
        }
        create("paid") {
            dimension = "foo"
        }

        create("one") {
            dimension = "bar"
        }

        create("two") {
            dimension = "bar"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
    configurations {
        create("firebaseOptions") {
            fromGoogleServicesJson {}
            targetPackage.set("com.example.myapplication.config")
        }
    }
}
