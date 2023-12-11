import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `java-gradle-plugin`
    signing
    alias(libs.plugins.jvm)
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
}

repositories {
    google()
    mavenCentral()
}

group = "ru.pixnews.gradle.fbase"
version = "0.1-SNAPSHOT"

@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit5)
            targets {
                all {
                    testTask.configure {
                        configureTestTaskDefaults()
                    }
                }
            }
            dependencies {
                implementation(platform(libs.junit.bom))
                implementation(platform(libs.kotest.bom))

                implementation(libs.agp.plugin)
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.params)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlin.compile.testing)
                runtimeOnly(libs.junit.jupiter.engine)
            }
        }

        register<JvmTestSuite>("functionalTest") {
            useJUnitJupiter(libs.versions.junit5)

            dependencies {
                implementation(project())
            }

            targets {
                all {
                    testTask.configure {
                        configureTestTaskDefaults()
                        shouldRunAfter(test)
                        testLogging {
                            if (providers.gradleProperty("verboseTest").map(String::toBoolean).getOrElse(false)) {
                                events = setOf(
                                    org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                                    org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR,
                                    org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Test.configureTestTaskDefaults() {
    maxHeapSize = "1G"
    testLogging {
        events = setOf(org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED)
    }
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = providers.environmentVariable("TEST_JDK_VERSION")
            .map { JavaLanguageVersion.of(it.toInt()) }
            .orElse(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(libs.agp.plugin.api)
    implementation(libs.kotlinpoet) {
        exclude(module = "kotlin-reflect")
    }
}

gradlePlugin {
    website.set("https://github.com/illarionov/fbase-config-generator-gradle-plugin")
    vcsUrl.set("https://github.com/illarionov/fbase-config-generator-gradle-plugin")
    plugins.create("fbaseConfig") {
        id = "ru.pixnews.gradle.fbase"
        implementationClass = "ru.pixnews.gradle.fbase.FbaseConfigGeneratorGradlePlugin"
        displayName = "Firebase Config Generator Gradle Plugin"
        description = "Gradle plugin that generates FirebaseOptions using values from configuration file."
        tags = listOf("android", "firebase")
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

publishing {
    repositories {
        // Local repository for testing publications
        maven {
            name = "test"
            url = uri("../build/local-plugin-repository")
        }
        maven {
            name = "PixnewsS3"
            setUrl("s3://maven.pixnews.ru/")
            credentials(AwsCredentials::class) {
                accessKey = providers.environmentVariable("YANDEX_S3_ACCESS_KEY_ID").getOrElse("")
                secretKey = providers.environmentVariable("YANDEX_S3_SECRET_ACCESS_KEY").getOrElse("")
            }
        }
    }
}

signing {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)

    setRequired {
        val signingDisabled = providers.gradleProperty("disableSigning").map(String::toBoolean).orElse(false)
        !signingDisabled.get()
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
        apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_6
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_6
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all",
        )
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
}

tasks.named<Task>("check") {
    dependsOn(testing.suites.named("functionalTest"))
}
