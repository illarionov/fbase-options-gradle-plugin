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
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.pixnews.gradle.fbase.test.functional.TestFirebaseOptions
import ru.pixnews.gradle.fbase.test.functional.assertions.dexBytecodeMatch
import ru.pixnews.gradle.fbase.test.functional.fixtures.AndroidAppFlavorsFixtures
import ru.pixnews.gradle.fbase.test.functional.fixtures.TestSubmodules.androidAppFlavors
import ru.pixnews.gradle.fbase.test.functional.fixtures.TestSubmodules.androidAppMulticonfig
import ru.pixnews.gradle.fbase.test.functional.fixtures.TestSubmodules.androidAppSimple
import ru.pixnews.gradle.fbase.test.functional.junit.AndroidProjectExtension
import ru.pixnews.gradle.fbase.test.functional.testmatrix.TestMatrix
import ru.pixnews.gradle.fbase.test.functional.testmatrix.VersionCatalog

class FbaseConfigGeneratorGradlePluginFunctionalTest {
    @JvmField
    @RegisterExtension
    var projectBuilder = AndroidProjectExtension(FBASE_VERSION)

    @ParameterizedTest
    @MethodSource("mainTestVariants", "firebaseTestVariants")
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
                TestFirebaseOptions(
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
                TestFirebaseOptions(
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
                TestFirebaseOptions(
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
    @MethodSource("mainTestVariants", "firebaseTestVariants")
    fun `can build project with multiple configurations`(versions: VersionCatalog) {
        projectBuilder.setupTestProject(androidAppMulticonfig, versions)

        val result = projectBuilder.build(versions.gradleVersion, "assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @ParameterizedTest
    @MethodSource("mainTestVariants", "firebaseTestVariants")
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

    public companion object {
        @JvmStatic
        fun mainTestVariants(): List<VersionCatalog> = TestMatrix(FBASE_VERSION).getMainTestVariants()

        @JvmStatic
        fun firebaseTestVariants(): List<VersionCatalog> = TestMatrix(FBASE_VERSION).getFirebaseTestVariants()
    }
}
