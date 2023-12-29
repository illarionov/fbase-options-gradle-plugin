/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.gradle.fbase.test.functional.TestFirebaseOptions
import ru.pixnews.gradle.fbase.test.functional.assertions.dexBytecodeMatch
import ru.pixnews.gradle.fbase.test.functional.fixtures.TestSubmodules.androidAppGoogleServicesCustomLocation
import ru.pixnews.gradle.fbase.test.functional.fixtures.TestSubmodules.androidAppGoogleServicesProject1
import ru.pixnews.gradle.fbase.test.functional.fixtures.fixtures
import ru.pixnews.gradle.fbase.test.functional.junit.AndroidProjectExtension

class GsonServicesTests {
    @JvmField
    @RegisterExtension
    var project = AndroidProjectExtension(FBASE_VERSION)

    @Test
    fun `Should build project with default config from google-services`() {
        val submodule = project.setupTestProject(androidAppGoogleServicesProject1)
            .subProject(androidAppGoogleServicesProject1)
        submodule.writeFiles(submodule.fixtures.googleServicesJson)

        val result = project.build("assemble")

        assertThat(result.output).contains("BUILD SUCCESSFUL")

        val apkFiles = buildList {
            listOf("free", "paid").forEach { flavor1 ->
                listOf("one", "two").forEach { flavor2 ->
                    listOf("debug", "release").forEach { buildType ->
                        add(submodule.apk(buildType, flavor1, flavor2))
                    }
                }
            }
        }

        apkFiles.forEach { apk ->
            val optionsDexCode = apk.getDexCode(
                classFqcn = "com.example.myapplication.config.FirebaseOptionsKt",
                methodSignature = "<clinit>()V",
            )
            assertThat(apk.getStringResource("google_app_id")).isEqualTo(EXPECTED_GOOGLE_APP_ID)
            assertThat(optionsDexCode).dexBytecodeMatch(EXPECTED_FIREBASE_OPTIONS)
        }
    }

    @Test
    fun `Should build project with default config from google-services with flavors`() {
        val submodule = project.setupTestProject(androidAppGoogleServicesProject1)
            .subProject(androidAppGoogleServicesProject1)
        val googleServiceJson = submodule.fixtures.googleServicesJson

        submodule.writeFiles(
            googleServiceJson.copy(dstPath = "src/free/google-services.json"),
            googleServiceJson.copy(dstPath = "src/paid/google-services.json"),
        )

        val result = project.build("assemble")

        assertThat(result.output).contains("BUILD SUCCESSFUL")

        val apkFiles = buildList {
            listOf("free", "paid").forEach { flavor1 ->
                listOf("one", "two").forEach { flavor2 ->
                    listOf("debug", "release").forEach { buildType ->
                        add(submodule.apk(buildType, flavor1, flavor2))
                    }
                }
            }
        }

        apkFiles.forEach { apk ->
            val optionsDexCode = apk.getDexCode(
                classFqcn = "com.example.myapplication.config.FirebaseOptionsKt",
                methodSignature = "<clinit>()V",
            )
            assertThat(apk.getStringResource("google_app_id")).isEqualTo(EXPECTED_GOOGLE_APP_ID)
            assertThat(optionsDexCode).dexBytecodeMatch(EXPECTED_FIREBASE_OPTIONS)
        }
    }

    @Test
    fun `Should build project with custom google-services location`() {
        val rootProject = project.setupTestProject(androidAppGoogleServicesCustomLocation)
        val submodule = rootProject.subProject(androidAppGoogleServicesCustomLocation)

        val googleServiceJson = submodule.fixtures.googleServicesJson
        rootProject.writeFiles(
            googleServiceJson.copy(dstPath = "config/google-services.json"),
        )

        val result = project.build("assemble")

        assertThat(result.output).contains("BUILD SUCCESSFUL")

        val apk = submodule.apk("release")
        val optionsDexCode = apk.getDexCode(
            classFqcn = "com.example.myapplication.config.FirebaseOptionsKt",
            methodSignature = "<clinit>()V",
        )
        assertThat(apk.getStringResource("google_app_id")).isEqualTo(EXPECTED_GOOGLE_APP_ID)
        assertThat(optionsDexCode).dexBytecodeMatch(EXPECTED_FIREBASE_OPTIONS)
    }

    internal companion object {
        const val EXPECTED_GOOGLE_APP_ID = "1:123456789000:android:f1bf012572b04063"
        val EXPECTED_FIREBASE_OPTIONS = TestFirebaseOptions(
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
