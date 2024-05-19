/*
 * Copyright (c) 2024, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `java-gradle-plugin`
    signing
    alias(libs.plugins.jvm)
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
    id("ru.pixnews.gradle.fbase.buildlogic.project.kotlin.version.version-plugin")
}

repositories {
    google()
    mavenCentral()
}

group = "ru.pixnews.gradle.fbase"
version = "0.2-SNAPSHOT"

val functionalTestRepository = rootProject.layout.buildDirectory.dir("functional-tests-plugin-repository")

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

                implementation(libs.agp.plugin)
                implementation(libs.assertk)
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.params)
                implementation(libs.kotlin.compile.testing)
                implementation(libs.mockk)
                runtimeOnly(libs.junit.jupiter.engine)
            }
        }

        withType(JvmTestSuite::class).matching {
            it.name in setOf("functionalTest", "functionalMatrixTest")
        }.configureEach {
            useJUnitJupiter(libs.versions.junit5)

            dependencies {
                implementation(project())
                implementation(libs.assertk)
                implementation(project(":functional-test-utils"))
            }

            targets {
                all {
                    testTask.configure {
                        configureTestTaskDefaults()
                        dependsOn(tasks.named("publishAllPublicationsToFunctionalTestsRepository"))
                        inputs.dir(functionalTestRepository)
                        shouldRunAfter(test)
                        listOf(
                            "testGradleVersion" to "GRADLE_VERSION",
                            "testAgpVersion" to "AGP_VERSION",
                            "testFirebaseVersion" to "FIREBASE_VERSION",
                        ).forEach { (inputProperty, envVariable) ->
                            inputs
                                .property(inputProperty) { System.getenv(envVariable) }
                                .optional(true)
                        }
                    }
                }
            }
        }

        register<JvmTestSuite>("functionalTest") {
            testType = "functional-test"
        }
        register<JvmTestSuite>("functionalMatrixTest") {
            testType = "functional-matrix-test"
        }
    }
}

private fun Test.configureTestTaskDefaults() {
    maxHeapSize = "1512M"
    jvmArgs = listOf("-XX:MaxMetaspaceSize=768M")
    testLogging {
        if (providers.gradleProperty("verboseTest").map(String::toBoolean).getOrElse(false)) {
            events = setOf(
                TestLogEvent.FAILED,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STANDARD_OUT,
            )
        } else {
            events = setOf(TestLogEvent.FAILED)
        }
    }
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = providers.environmentVariable("TEST_JDK_VERSION")
            .map { JavaLanguageVersion.of(it.toInt()) }
            .orElse(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(libs.agp.plugin.api)
    implementation(libs.gson)
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
        displayName = "Fbase Config Generator Gradle Plugin"
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
            name = "functionalTests"
            url = uri(functionalTestRepository)
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
        val signingRequired = providers.gradleProperty("enableSigning").map(String::toBoolean).orElse(false)
        signingRequired.get()
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
        apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_6
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_6
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all",
        )
        if (!this@configureEach.name.endsWith("TestKotlin")) {
            freeCompilerArgs.addAll("-Xexplicit-api=warning")
        }
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}

tasks.named<Task>("check") {
    dependsOn(testing.suites.named("functionalTest"))
}
