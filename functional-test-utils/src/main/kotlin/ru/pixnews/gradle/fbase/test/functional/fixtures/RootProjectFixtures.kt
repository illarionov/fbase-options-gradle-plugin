/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.fixtures

import ru.pixnews.gradle.fbase.test.functional.TestFirebaseOptions
import ru.pixnews.gradle.fbase.test.functional.junit.FileContent
import ru.pixnews.gradle.fbase.test.functional.util.androidHome
import java.io.File

public object RootProjectFixtures {
    public val gradleProperties: FileContent by lazy { FixturesPaths.testFilesRootFileContent("gradle.properties") }
    public val buildGradleKts: FileContent by lazy { FixturesPaths.testFilesRootFileContent("build.gradle.kts") }
    public val localProperties: FileContent by lazy {
        FileContent(
            "local.properties",
            "sdk.dir=${androidHome()}".trimIndent(),
        )
    }
    public val defaultFirebaseProperties: FileContent by lazy {
        FixturesPaths.testFilesRootFileContent("config/firebase.properties")
    }
    public val defaultFirebasePropertiesReleaseConfig: TestFirebaseOptions = TestFirebaseOptions(
        projectId = "sample-en",
        apiKey = "AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZ8",
        applicationId = "1:1035469437089:android:112233445566778899aabb",
        databaseUrl = "https://sample-en.firebaseio.com",
        gaTrackingId = "UA-65557217-3",
        gcmSenderId = "1035469437089",
        storageBucket = "sample-en.appspot.com",
    )

    public fun settingsGradleKts(
        vararg includeSubprojects: String,
    ): FileContent {
        val settingsFile = FixturesPaths.testFilesRootFileContent("settings.gradle.kts")

        val includes = includeSubprojects.joinToString("\n") { """include("$it")""" }
        val functionalTestsMaven = File(FixturesPaths.userDir, "../build/functional-tests-plugin-repository")
        val newContent = settingsFile.content + includes + "\n" +
                """
                pluginManagement {
                    repositories {
                        exclusiveContent {
                            forRepository {
                                maven { url = uri("file://$functionalTestsMaven") }
                            }
                            filter {
                                includeGroup("ru.pixnews.gradle.fbase")
                            }
                        }
                        google()
                        mavenCentral()
                        gradlePluginPortal()
                    }
                }
                """.trimIndent()
        return FileContent(
            settingsFile.dstPath,
            newContent,
        )
    }
}
