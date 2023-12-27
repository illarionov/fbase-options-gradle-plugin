/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("LongMethod")

package ru.pixnews.gradle.fbase

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.pixnews.gradle.fbase.assertions.dexBytecodeMatch
import ru.pixnews.gradle.fbase.fixtures.AndroidAppFlavorsFixtures
import ru.pixnews.gradle.fbase.fixtures.RootProjectFixtures
import ru.pixnews.gradle.fbase.fixtures.TestSubmodules.androidAppFlavors
import ru.pixnews.gradle.fbase.fixtures.TestSubmodules.androidAppMulticonfig
import ru.pixnews.gradle.fbase.fixtures.TestSubmodules.androidAppSimple
import ru.pixnews.gradle.fbase.fixtures.fixtures
import ru.pixnews.gradle.fbase.junit.AndroidProjectExtension
import ru.pixnews.gradle.fbase.junit.SubmoduleId
import ru.pixnews.gradle.fbase.test.functional.testmatrix.TestMatrix.FIREBASE_TEST_VARIANTS_FQN
import ru.pixnews.gradle.fbase.test.functional.testmatrix.TestMatrix.MAIN_TEST_VARIANTS_FQN
import ru.pixnews.gradle.fbase.test.functional.testmatrix.VersionCatalog

class FbaseConfigGeneratorGradlePluginFunctionalTest {
    @JvmField
    @RegisterExtension
    var projectBuilder = AndroidProjectExtension()

    @ParameterizedTest
    @MethodSource(MAIN_TEST_VARIANTS_FQN, FIREBASE_TEST_VARIANTS_FQN)
    fun `can build simple project`(versions: VersionCatalog) {
        val rootProject = projectBuilder.setupTestProject(androidAppSimple, versions)

        val result = projectBuilder.build(versions.gradleVersion, "assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))

        val submodule = rootProject.subProject(androidAppSimple)
        submodule.apk("release").also { releaseApk ->
            val releaseDexCode = releaseApk.getDexCode(
                classFqcn = "com.example.samplefbase.config.FirebaseOptionsKt",
                methodSignature = "<clinit>()V",
            )
            assertThat(releaseApk.getStringResource("google_app_id"))
                .isEqualTo("1:1035469437089:android:112233445566778899aabb")
            assertThat(releaseDexCode).dexBytecodeMatch(
                LocalFirebaseOptions(
                    projectId = "sample-en",
                    apiKey = "AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZ8",
                    applicationId = "1:1035469437089:android:112233445566778899aabb",
                    databaseUrl = "https://sample-en.firebaseio.com",
                    gaTrackingId = "UA-65557217-3",
                    gcmSenderId = "1035469437089",
                    storageBucket = "sample-en.appspot.com",
                ),
            )
        }

        submodule.apk("benchmark").also { benchmarkApk ->
            val benchmarkDexCode = benchmarkApk.getDexCode(
                classFqcn = "com.example.samplefbase.config.FirebaseOptionsKt",
                methodSignature = "<clinit>()V",
            )
            assertThat(benchmarkApk.getStringResource("google_app_id"))
                .isEqualTo("1:1035469437089:android:2233445566778899aabbcc")
            assertThat(benchmarkDexCode).dexBytecodeMatch(
                LocalFirebaseOptions(
                    projectId = "sample-en",
                    apiKey = "AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZ8",
                    applicationId = "1:1035469437089:android:2233445566778899aabbcc",
                    databaseUrl = "https://sample-en.firebaseio.com",
                    gaTrackingId = "UA-65557217-3",
                    gcmSenderId = "1035469437089",
                    storageBucket = "sample-en.appspot.com",
                ),
            )
        }

        submodule.apk("debug").also { debugApk ->
            val debugDexCode = debugApk.getDexCode(
                classFqcn = "com.example.samplefbase.config.FirebaseOptionsKt",
                methodSignature = "<clinit>()V",
            )
            assertThat(debugApk.getStringResource("google_app_id"))
                .isEqualTo("1:1035469437089:android:73a4fb8297b2cd4f")
            assertThat(debugDexCode).dexBytecodeMatch(
                LocalFirebaseOptions(
                    projectId = "sample-en",
                    apiKey = "AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZ8",
                    applicationId = "1:1035469437089:android:73a4fb8297b2cd4f",
                    databaseUrl = "https://sample-en.firebaseio.com",
                    gaTrackingId = "UA-65557217-3",
                    gcmSenderId = "1035469437089",
                    storageBucket = "sample-en.appspot.com",
                ),
            )
        }
    }

    @ParameterizedTest
    @MethodSource(MAIN_TEST_VARIANTS_FQN, FIREBASE_TEST_VARIANTS_FQN)
    fun `can build project with multiple configurations`(versions: VersionCatalog) {
        projectBuilder.setupTestProject(androidAppMulticonfig, versions)

        val result = projectBuilder.build(versions.gradleVersion, "assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @ParameterizedTest
    @MethodSource(MAIN_TEST_VARIANTS_FQN, FIREBASE_TEST_VARIANTS_FQN)
    fun `can build project with flavors`(versions: VersionCatalog) {
        val rootProject = projectBuilder.setupTestProject(androidAppFlavors, versions)
        val submodule = rootProject.subProject(androidAppFlavors)

        val result = projectBuilder.build(versions.gradleVersion, "assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))

        AndroidAppFlavorsFixtures.testedVariants.forEach { testedVariant ->
            val apk = submodule.apk(
                buildType = testedVariant.buildType,
                flavors = testedVariant.flavors.toTypedArray(),
            )
            assertThat(apk.getStringResource("google_app_id"))
                .isEqualTo(testedVariant.expectedGoogleAppId)

            testedVariant.expectedBuilders.forEach { (className, expectedOptions) ->
                val code = apk.getDexCode("com.example.samplefbase.config.$className")
                assertThat(code).dexBytecodeMatch(expectedOptions)
            }
        }
    }

    @Test
    fun `can build project if configurations are not defined`() {
        val submoduleId = SubmoduleId(
            projectName = "android-app-noconf",
            namespace = "com.example.samplefbase",
        )

        val submodule = projectBuilder.setupTestProjectScaffold(submoduleId)
            .subProject(submoduleId)

        val buildGradleKts = submodule.fixtures.buildGradleKts(
            """
            firebaseConfig {}
        """.trimIndent(),
        )
        val application = submodule.fixtures.application.copy(
            content = """
                package ${submodule.id.namespace}
                import android.app.Application
                class Application : Application()
        """.trimIndent(),
        )

        submodule.writeFiles(
            buildGradleKts,
            application,
        )

        val result = projectBuilder.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `can build project with default value if source is not set`() {
        val submoduleId = SubmoduleId(
            projectName = "android-app-no-source-set",
            namespace = "com.example.samplefbase",
        )
        val rootProject = projectBuilder.setupTestProjectScaffold(submoduleId)
        val submodule = rootProject.subProject(submoduleId)

        val buildGradleKts = submodule.fixtures.buildGradleKts(
            """
            firebaseConfig {
               configurations {
                   create("firebaseOptions") {
                   }
               }
            }
        """.trimIndent(),
        )
        submodule.writeFiles(
            buildGradleKts,
            submodule.fixtures.application,
        )
        rootProject.writeFiles(
            RootProjectFixtures.defaultFirebaseProperties,
        )

        val result = projectBuilder.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `should fail when multiple configurations are defined and the main configuration is not set`() {
        val submoduleId = SubmoduleId(
            projectName = "android-app-multimple-config-no-primary",
            namespace = "com.example.samplefbase",
        )
        val rootProject = projectBuilder.setupTestProjectScaffold(submoduleId)
        val submodule = rootProject.subProject(submoduleId)

        val buildGradleKts = submodule.fixtures.buildGradleKts(
            """
            firebaseConfig {
               configurations {
                   create("firebaseOptions1") {
                   }
                   create("firebaseOptions2") {
                   }
               }
            }
        """.trimIndent(),
        )
        submodule.writeFiles(
            buildGradleKts,
            submodule.fixtures.application,
        )
        rootProject.writeFiles(RootProjectFixtures.defaultFirebaseProperties)

        val result = projectBuilder.buildAndFail("assemble")

        assertTrue(
            result.output.contains(
                "FbaseGeneratorExtension.primaryConfiguration must be set when using multiple configurations",
            ),
        )
    }

    @Test
    fun `should fail when multiple configurations are defined and the main configuration is set to a non-existent`() {
        val submoduleId = SubmoduleId(
            projectName = "android-app-multimple-config-wrong-primary",
            namespace = "com.example.samplefbase",
        )
        val rootProject = projectBuilder.setupTestProjectScaffold(submoduleId)
        val submodule = rootProject.subProject(submoduleId)

        val buildGradleKts = submodule.fixtures.buildGradleKts(
            """
            firebaseConfig {
               primaryConfiguration = "firebaseOptions3"
               configurations {
                   create("firebaseOptions1") {
                   }
                   create("firebaseOptions2") {
                   }
               }
            }
        """.trimIndent(),
        )
        rootProject.writeFiles(RootProjectFixtures.defaultFirebaseProperties)
        submodule.writeFiles(
            buildGradleKts,
            submodule.fixtures.application,
        )

        val result = projectBuilder.buildAndFail("assemble")

        assertTrue(
            result.output.contains(
                "Configuration named `firebaseOptions3` is not defined",
            ),
        )
    }
}
