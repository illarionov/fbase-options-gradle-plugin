import ru.pixnews.gradle.fbase.FbaseGeneratorExtension

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
            extensions.configure<FbaseGeneratorExtension> {
                primaryConfiguration = "benchmarkFirebaseOptions"
                configurations.create("benchmarkFirebaseOptions") {
                    fromPropertiesFile {
                        location = layout.projectDirectory.file("firebase_benchmark.properties")
                    }
                    targetPackage = "com.example.samplefbase.config"
                }
            }
        }
    }
    flavorDimensions += listOf("api", "mode")
    productFlavors {
        create("demo") {
            dimension = "mode"
            applicationIdSuffix = ".demo"
            extensions.configure<FbaseGeneratorExtension> {
                primaryConfiguration = "demoFirebaseOptions"
                configurations.create("demoFirebaseOptions") {
                    fromPropertiesFile {
                        location = layout.projectDirectory.file("firebase_demo.properties")
                    }
                    targetPackage = "com.example.samplefbase.config"
                }
            }
        }
        create("full") {
            dimension = "mode"
            applicationIdSuffix = ".full"
            extensions.configure<FbaseGeneratorExtension> {
                primaryConfiguration = "fullFirebaseOptions"
                configurations.create("fullFirebaseOptions") {
                    fromPropertiesFile {
                        location = layout.projectDirectory.file("firebase_full.properties")
                    }
                    targetPackage = "com.example.samplefbase.config"
                }
            }
        }
        create("minApi24") {
            dimension = "api"
            minSdk = 24
            versionCode = 30000 + (android.defaultConfig.versionCode ?: 0)
            versionNameSuffix = "-minApi24"
            extensions.configure<FbaseGeneratorExtension> {
                primaryConfiguration = "minApi24FirebaseOptions"
                configurations.create("minApi24FirebaseOptions") {
                    fromPropertiesFile {
                        location = layout.projectDirectory.file("firebase_minapi24.properties")
                    }
                    targetPackage = "com.example.samplefbase.config"
                }
            }
        }
        create("minApi21") {
            dimension = "api"
            minSdk = 21
            versionCode = 10000 + (android.defaultConfig.versionCode ?: 0)
            versionNameSuffix = "-minApi21"
            extensions.configure<FbaseGeneratorExtension> {
                primaryConfiguration = "minApi21FirebaseOptions"
                configurations.create("minApi21FirebaseOptions") {
                    fromPropertiesFile {
                        location = layout.projectDirectory.file("firebase_minapi21.properties")
                    }
                    targetPackage = "com.example.samplefbase.config"
                }
            }
        }
    }

    androidComponents {
        val releaseSelector = androidComponents.selector().withBuildType("release")
        onVariants(releaseSelector) { variant ->
            variant.getExtension(FbaseGeneratorExtension::class.java)?.let { generator ->
                generator.configurations.create("releaseFirebaseOptions") {
                    fromPropertiesFile {
                        location = layout.projectDirectory.file("firebase_release.properties")
                    }
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
            fromPropertiesFile {
                location = layout.projectDirectory.file("firebase.properties")
            }
            targetPackage = "com.example.samplefbase.config"
        }
    }
}
