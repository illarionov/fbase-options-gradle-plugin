/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.gradle.fbase.android.apk.ApkAnalyzer
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures
import ru.pixnews.gradle.fbase.android.junit.AndroidProjectExtension
import ru.pixnews.gradle.fbase.android.util.dexBytecodeMatch

class GsonServicesTests {
    @JvmField
    @RegisterExtension
    var project = AndroidProjectExtension()

    @Test
    fun `Should build project with default config from google-services`() {
        val submoduleName = "android-app-google-services-project1"
        val namespace = "com.example.myapplication"

        project.setupTestProject(
            name = submoduleName,
            namespace = namespace,
        )

        project.writeFilesToSubmoduleRoot(
            submoduleName = submoduleName,
            ProjectFixtures.SubmoduleFixtures(namespace).googleServicesJson,
        )

        val result = project.build("assemble")

        Assertions.assertTrue(result.output.contains("BUILD SUCCESSFUL"))

        val apkFiles = buildList {
            listOf("free", "paid").forEach { flavor1 ->
                listOf("one", "two").forEach { flavor2 ->
                    listOf("debug", "release").forEach { buildType ->
                        add(project.apkFilePath(submoduleName, buildType, flavor1, flavor2))
                    }
                }
            }
        }

        apkFiles.forEach { apkPath ->
            val apk = ApkAnalyzer(apkPath)
            val optionsDexCode = apk.getDexCode(
                classFqcn = "com.example.myapplication.config.FirebaseOptionsKt",
                methodSignature = "<clinit>()V",
            )
            assertThat(apk.getStringResource("google_app_id")).isEqualTo(EXPECTED_GOOGLE_APP_ID)
            assertThat(optionsDexCode).dexBytecodeMatch(EXPECTED_LOCAL_FIREBASE_OPTIONS)
        }
    }

    @Test
    fun `Should build project with default config from google-services with flavors`() {
        val submoduleName = "android-app-google-services-project1"
        val namespace = "com.example.myapplication"

        project.setupTestProject(
            name = submoduleName,
            namespace = namespace,
        )

        val googleServiceJson = ProjectFixtures.SubmoduleFixtures(namespace).googleServicesJson
        project.writeFilesToSubmoduleRoot(
            submoduleName = submoduleName,
            googleServiceJson.copy(dstPath = "src/free/google-services.json"),
            googleServiceJson.copy(dstPath = "src/paid/google-services.json"),
        )

        val result = project.build("assemble")

        Assertions.assertTrue(result.output.contains("BUILD SUCCESSFUL"))

        val apkFiles = buildList {
            listOf("free", "paid").forEach { flavor1 ->
                listOf("one", "two").forEach { flavor2 ->
                    listOf("debug", "release").forEach { buildType ->
                        add(project.apkFilePath(submoduleName, buildType, flavor1, flavor2))
                    }
                }
            }
        }

        apkFiles.forEach { apkPath ->
            val apk = ApkAnalyzer(apkPath)
            val optionsDexCode = apk.getDexCode(
                classFqcn = "com.example.myapplication.config.FirebaseOptionsKt",
                methodSignature = "<clinit>()V",
            )
            assertThat(apk.getStringResource("google_app_id")).isEqualTo(EXPECTED_GOOGLE_APP_ID)
            assertThat(optionsDexCode).dexBytecodeMatch(EXPECTED_LOCAL_FIREBASE_OPTIONS)
        }
    }

    @Test
    fun `Should build project with custom google-services location`() {
        val submoduleName = "android-app-google-services-custom-location"
        val namespace = "com.example.myapplication"

        project.setupTestProject(
            name = submoduleName,
            namespace = namespace,
        )

        val googleServiceJson = ProjectFixtures.SubmoduleFixtures(namespace).googleServicesJson
        project.writeFilesToRoot(
            googleServiceJson.copy(dstPath = "config/google-services.json"),
        )

        val result = project.build("assemble")

        Assertions.assertTrue(result.output.contains("BUILD SUCCESSFUL"))

        val releaseApk = project.apkFilePath(submoduleName, "release")
        val apk = ApkAnalyzer(releaseApk)
        val optionsDexCode = apk.getDexCode(
            classFqcn = "com.example.myapplication.config.FirebaseOptionsKt",
            methodSignature = "<clinit>()V",
        )
        assertThat(apk.getStringResource("google_app_id")).isEqualTo(EXPECTED_GOOGLE_APP_ID)
        assertThat(optionsDexCode).dexBytecodeMatch(EXPECTED_LOCAL_FIREBASE_OPTIONS)
    }

    internal companion object {
        const val EXPECTED_GOOGLE_APP_ID = "1:123456789000:android:f1bf012572b04063"
        val EXPECTED_LOCAL_FIREBASE_OPTIONS = LocalFirebaseOptions(
            projectId = "mockproject-1234",
            apiKey = "AIzbSzCn1N6LWIe6wthYyrgUUSAlUsdqMb-wvTo",
            applicationId = "1:123456789000:android:f1bf012572b04063",
            databaseUrl = "https://mockproject-1234.firebaseio.com",
            gaTrackingId = null,
            gcmSenderId = "123456789000",
            storageBucket = null,
        )
    }
}
