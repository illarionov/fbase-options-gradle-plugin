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
                primaryConfiguration.set("benchmarkFirebaseOptions")
                configurations.create("benchmarkFirebaseOptions") {
                    fromPropertiesFile {
                        location.set(layout.projectDirectory.file("firebase_benchmark.properties"))
                    }
                    targetPackage.set("com.example.samplefbase.config")
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
                primaryConfiguration.set("demoFirebaseOptions")
                configurations.create("demoFirebaseOptions") {
                    fromPropertiesFile {
                        location.set(layout.projectDirectory.file("firebase_demo.properties"))
                    }
                    targetPackage.set("com.example.samplefbase.config")
                }
            }
        }
        create("full") {
            dimension = "mode"
            applicationIdSuffix = ".full"
            extensions.configure<FbaseGeneratorExtension> {
                primaryConfiguration.set("fullFirebaseOptions")
                configurations.create("fullFirebaseOptions") {
                    fromPropertiesFile {
                        location.set(layout.projectDirectory.file("firebase_full.properties"))
                    }
                    targetPackage.set("com.example.samplefbase.config")
                }
            }
        }
        create("minApi24") {
            dimension = "api"
            minSdk = 24
            versionCode = 30000 + (android.defaultConfig.versionCode ?: 0)
            versionNameSuffix = "-minApi24"
            extensions.configure<FbaseGeneratorExtension> {
                primaryConfiguration.set("minApi24FirebaseOptions")
                configurations.create("minApi24FirebaseOptions") {
                    fromPropertiesFile {
                        location.set(layout.projectDirectory.file("firebase_minapi24.properties"))
                    }
                    targetPackage.set("com.example.samplefbase.config")
                }
            }
        }
        create("minApi21") {
            dimension = "api"
            minSdk = 21
            versionCode = 10000 + (android.defaultConfig.versionCode ?: 0)
            versionNameSuffix = "-minApi21"
            extensions.configure<FbaseGeneratorExtension> {
                primaryConfiguration.set("minApi21FirebaseOptions")
                configurations.create("minApi21FirebaseOptions") {
                    fromPropertiesFile {
                        location.set(layout.projectDirectory.file("firebase_minapi21.properties"))
                    }
                    targetPackage.set("com.example.samplefbase.config")
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
                        location.set(layout.projectDirectory.file("firebase_release.properties"))
                    }
                    targetPackage.set("com.example.samplefbase.config")
                }
            }
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
            fromPropertiesFile {
                location.set(layout.projectDirectory.file("firebase.properties"))
            }
            targetPackage.set("com.example.samplefbase.config")
        }
    }
}
