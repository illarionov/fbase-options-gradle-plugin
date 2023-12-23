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
import ru.pixnews.gradle.fbase.android.apk.ApkAnalyzer
import ru.pixnews.gradle.fbase.android.fixtures.AndroidAppFlavorsFixtures
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.DEFAULT_NAMESPACE
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.Root
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.SubmoduleFixtures
import ru.pixnews.gradle.fbase.android.junit.AndroidProjectExtension
import ru.pixnews.gradle.fbase.android.util.dexBytecodeMatch

class FbaseConfigGeneratorGradlePluginFunctionalTest {
    @JvmField
    @RegisterExtension
    var project = AndroidProjectExtension()
    val submoduleFixtures = SubmoduleFixtures()

    @Test
    fun `can build simple project`() {
        val projectName = "android-app-simple"
        val submoduleDir = project.submoduleOutputApkDir(projectName)

        project.setupTestProject(projectName)

        val result = project.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))

        ApkAnalyzer(submoduleDir.resolve("release/$projectName-release-unsigned.apk")).also { releaseApk ->
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

        ApkAnalyzer(submoduleDir.resolve("benchmark/$projectName-benchmark-unsigned.apk")).also { benchmarkApk ->
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

        ApkAnalyzer(submoduleDir.resolve("debug/$projectName-debug.apk")).also { debugApk ->
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

    @Test
    fun `can build project with multiple configurations`() {
        project.setupTestProject("android-app-multiconfig")

        val result = project.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `can build project with flavors`() {
        val projectName = AndroidAppFlavorsFixtures.PROJECT_NAME
        val submoduleDir = project.submoduleOutputApkDir(projectName)

        project.setupTestProject(
            name = projectName,
            namespace = AndroidAppFlavorsFixtures.NAMESPACE,
        )

        val result = project.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))

        AndroidAppFlavorsFixtures.testedVariants.forEach { testedVariant ->
            val apk = ApkAnalyzer(submoduleDir.resolve(testedVariant.apkPath))
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
        val submoduleName = "android-app-noconf"
        project.setupTestProjectScaffold(submoduleName)

        val buildGradleKts = submoduleFixtures.buildGradleKts(
            """
            firebaseConfig {}
        """.trimIndent(),
        )
        val application = submoduleFixtures.application.copy(
            content = """
                package $DEFAULT_NAMESPACE
                import android.app.Application
                class Application : Application()
        """.trimIndent(),
        )

        project.writeFilesToSubmoduleRoot(
            submoduleName = submoduleName,
            buildGradleKts,
            application,
        )

        val result = project.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `can build project with default value if source is not set`() {
        val submoduleName = "android-app-no-source-set"
        project.setupTestProjectScaffold(submoduleName)

        val buildGradleKts = submoduleFixtures.buildGradleKts(
            """
            firebaseConfig {
               configurations {
                   create("firebaseOptions") {
                   }
               }
            }
        """.trimIndent(),
        )
        project.writeFilesToSubmoduleRoot(
            submoduleName = submoduleName,
            buildGradleKts,
            submoduleFixtures.application,
        )
        project.writeFiles(
            project.rootDir,
            Root.defaultFirebaseProperties,
        )

        val result = project.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `should fail when multiple configurations are defined and the main configuration is not set`() {
        val submoduleName = "android-app-multimple-config-no-primary"
        project.setupTestProjectScaffold(submoduleName)

        val buildGradleKts = submoduleFixtures.buildGradleKts(
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
        project.writeFilesToSubmoduleRoot(
            submoduleName = submoduleName,
            buildGradleKts,
            submoduleFixtures.application,
        )
        project.writeFiles(
            project.rootDir,
            Root.defaultFirebaseProperties,
        )

        val result = project.buildAndFail("assemble")

        assertTrue(
            result.output.contains(
                "FbaseGeneratorExtension.primaryConfiguration must be set when using multiple configurations",
            ),
        )
    }

    @Test
    fun `should fail when multiple configurations are defined and the main configuration is set to a non-existent`() {
        val submoduleName = "android-app-multimple-config-wrong-primary"
        project.setupTestProjectScaffold(submoduleName)

        val buildGradleKts = submoduleFixtures.buildGradleKts(
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
        project.writeFilesToSubmoduleRoot(
            submoduleName = submoduleName,
            buildGradleKts,
            submoduleFixtures.application,
        )
        project.writeFiles(
            project.rootDir,
            Root.defaultFirebaseProperties,
        )

        val result = project.buildAndFail("assemble")

        assertTrue(
            result.output.contains(
                "Configuration named `firebaseOptions3` is not defined",
            ),
        )
    }
}
