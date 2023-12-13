import ru.pixnews.gradle.fbase.FbaseGeneratorExtension
import ru.pixnews.gradle.fbase.FbaseGeneratorFlavorExtension
import ru.pixnews.gradle.fbase.LocalFirebaseOptions

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.fbase.config.generator.gradle.plugin)
}

android {
    namespace = "com.example.samplefbase.flavors"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.samplefbase.flavors"
        minSdk = 24
        targetSdk = 34
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
            extensions.configure<FbaseGeneratorFlavorExtension> {
                configurations.create("benchmarkFirebaseOptions") {
                    source = provider {
                        LocalFirebaseOptions(
                            projectId = null,
                            apiKey = null,
                            applicationId = null,
                            databaseUrl = null,
                            gaTrackingId = null,
                            gcmSenderId = null,
                            storageBucket = null,
                        )
                    }
                    targetPackage = "com.example.samplefbase.config"
                }
            }
        }
    }
    flavorDimensions += "version"
    productFlavors {
        create("demo") {
            dimension = "version"
            applicationIdSuffix = ".demo"
            extensions.configure<FbaseGeneratorFlavorExtension> {
                configurations.create("demoFirebaseOptions") {
                    source = provider {
                        LocalFirebaseOptions(
                            projectId = null,
                            apiKey = null,
                            applicationId = null,
                            databaseUrl = null,
                            gaTrackingId = null,
                            gcmSenderId = null,
                            storageBucket = null,
                        )
                    }
                    targetPackage = "com.example.samplefbase.config"
                }
            }
        }
        create("full") {
            dimension = "version"
            applicationIdSuffix = ".full"
        }
    }

    androidComponents {
        val releaseSelector = androidComponents.selector().withBuildType("release")
        onVariants(releaseSelector) { variant ->
            variant.getExtension(FbaseGeneratorExtension::class.java)?.let { generator ->
                generator.configurations.create("releaseFirebaseOptions") {
                    source = generator.providers.propertiesFile(
                        layout.projectDirectory.file("firebase_release.properties"),
                    )
                    targetPackage = "com.example.samplefbase.config"
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

firebaseConfig {
    configurations {
        create("firebaseOptions") {
            source = providers.propertiesFile(layout.projectDirectory.file("firebase.properties"))
            targetPackage = "com.example.samplefbase.config"
        }
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.core.ktx)
}
