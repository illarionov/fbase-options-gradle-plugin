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
import ru.pixnews.gradle.fbase.android.fixtures.AndroidAppFlavorsFixtures
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.Root
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.TestSubmodules.androidAppFlavors
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.TestSubmodules.androidAppMulticonfig
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.TestSubmodules.androidAppSimple
import ru.pixnews.gradle.fbase.android.fixtures.SubmoduleId
import ru.pixnews.gradle.fbase.android.junit.AndroidProjectExtension
import ru.pixnews.gradle.fbase.android.util.dexBytecodeMatch

class FbaseConfigGeneratorGradlePluginFunctionalTest {
    @JvmField
    @RegisterExtension
    var projectBuilder = AndroidProjectExtension()

    @Test
    fun `can build simple project`() {
        val rootProject = projectBuilder.setupTestProject(androidAppSimple)

        val result = projectBuilder.build("assemble")

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

    @Test
    fun `can build project with multiple configurations`() {
        projectBuilder.setupTestProject(androidAppMulticonfig)

        val result = projectBuilder.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `can build project with flavors`() {
        val rootProject = projectBuilder.setupTestProject(androidAppFlavors)
        val submodule = rootProject.subProject(androidAppFlavors)

        val result = projectBuilder.build("assemble")

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
            Root.defaultFirebaseProperties,
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
        rootProject.writeFiles(Root.defaultFirebaseProperties)

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
        rootProject.writeFiles(Root.defaultFirebaseProperties)
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
