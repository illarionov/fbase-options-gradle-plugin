/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.android

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.gradle.fbase.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.android.apk.ApkAnalyzer
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.SubmoduleFixtures
import ru.pixnews.gradle.fbase.android.junit.AndroidProjectExtension
import ru.pixnews.gradle.fbase.android.util.dexBytecodeMatch
import ru.pixnews.gradle.fbase.android.util.getApkPath

class GsonServicesTests {
    @JvmField
    @RegisterExtension
    var project = AndroidProjectExtension()

    @Test
    fun `Should build project with default config from google-services`() {
        val submoduleName = "android-app-google-services"
        val namespace = "com.example.myapplication"

        project.setupTestProject(
            name = submoduleName,
            namespace = namespace,
        )

        project.writeFilesToSubmoduleRoot(
            submoduleName = submoduleName,
            SubmoduleFixtures(namespace).googleServicesJson,
        )

        val result = project.build("assemble")

        Assertions.assertTrue(result.output.contains("BUILD SUCCESSFUL"))

        val apkPatches = buildList {
            listOf("free", "paid").forEach { flavor1 ->
                listOf("one", "two").forEach { flavor2 ->
                    listOf("debug", "release").forEach { buildType ->
                        add(getApkPath(submoduleName, buildType, flavor1, flavor2))
                    }
                }
            }
        }

        val submoduleApkDir = project.submoduleOutputApkDir(submoduleName)
        apkPatches.forEach { apkPath ->
            val apk = ApkAnalyzer(submoduleApkDir.resolve(apkPath))
            val optionsDexCode = apk.getDexCode(
                classFqcn = "com.example.myapplication.config.FirebaseOptionsKt",
                methodSignature = "<clinit>()V",
            )
            assertThat(apk.getStringResource("google_app_id"))
                .isEqualTo("1:123456789000:android:f1bf012572b04063")
            assertThat(optionsDexCode).dexBytecodeMatch(
                LocalFirebaseOptions(
                    projectId = "mockproject-1234",
                    apiKey = "AIzbSzCn1N6LWIe6wthYyrgUUSAlUsdqMb-wvTo",
                    applicationId = "1:123456789000:android:f1bf012572b04063",
                    databaseUrl = "https://mockproject-1234.firebaseio.com",
                    gaTrackingId = null,
                    gcmSenderId = "123456789000",
                    storageBucket = null,
                ),
            )
        }
    }
}
