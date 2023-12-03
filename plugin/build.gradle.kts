import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.jvm)
    id("com.gradle.plugin-publish")
    id("maven-publish")
}

repositories {
    google()
    mavenCentral()
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
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
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.params)
                runtimeOnly(libs.junit.jupiter.engine)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlin.compile.testing)
            }
        }

        val functionalTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                implementation(project())
            }

            targets {
                all {
                    testTask.configure {
                        configureTestTaskDefaults()
                        shouldRunAfter(test)
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
            .map { JavaLanguageVersion.of(it.toInt())  }
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
    website.set("https://github.com/illarionov/fbase-options-gradle-plugin")
    vcsUrl.set("https://github.com/illarionov/fbase-options-gradle-plugin")
    val fbaseConfig by plugins.creating {
        id = "ru.pixnews.gradle.fbase.options"
        implementationClass = "ru.pixnews.gradle.fbase.options.FbaseOptionsGradlePlugin"
        displayName = "Fbase Options Gradle Plugin"
        description = "Gradle plugin that generates FirebaseOptions with values from configuration file."
        tags = listOf("android", "firebase")
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
        apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_6
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_6
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
}

tasks.named<Task>("check") {
    dependsOn(testing.suites.named("functionalTest"))
}
