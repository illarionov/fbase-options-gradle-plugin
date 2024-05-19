/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.internal

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.assertions.messageContains
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.pixnews.gradle.fbase.FbaseOptions
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.writeText

class GoogleServicesValueSourceTest {
    private val project: Project = ProjectBuilder.builder().build()
    private val providers: ProviderFactory = project.providers
    private val projectDirectory = project.layout.projectDirectory

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `Should throw exception if file list is empty`() {
        val source = createGoogleServicesValueSource(
            configurationFiles = emptyList(),
        )
        assertFailure {
            source.get()
        }.all {
            hasClass(GradleException::class.java)
            messageContains("File google-services.json is missing")
        }
    }

    @Test
    fun `Should throw exception if file not existed`() {
        val source = createGoogleServicesValueSource(
            configurationFiles = listOf(projectDirectory.file("nonexistent.json").asFile.toPath()),
        )
        assertFailure {
            source.get()
        }.all {
            hasClass(GradleException::class.java)
            messageContains("File google-services.json is missing")
        }
    }

    @Test
    fun `Should use configuration specified by applicationId`() {
        val selectedClientInfo = AndroidClientInfo(
            packageName = "com.example.sample.fbase1",
            mobileSdkAppId = "1:123456789000:android:f1bf012572b04061",
        )
        val jsonFile = createTempGoogleServiceJson(
            clients = listOf(
                AndroidClientInfo(
                    packageName = "com.example.sample.fbase3",
                    mobileSdkAppId = "1:123456789000:android:f1bf012572b04063",
                ),
                AndroidClientInfo(
                    packageName = "com.example.sample.fbase2",
                    mobileSdkAppId = "1:123456789000:android:f1bf012572b04062",
                ),
                selectedClientInfo,
                AndroidClientInfo(
                    packageName = "com.example.sample.fbase",
                    mobileSdkAppId = "1:123456789000:android:f1bf012572b04060",
                ),
            ),
        )
        val source = createGoogleServicesValueSource(
            configurationFiles = listOf(jsonFile),
            applicationId = selectedClientInfo.packageName,
        )

        val config = source.get()

        assertThat(config.applicationId).isEqualTo(selectedClientInfo.mobileSdkAppId)
    }

    @Test
    fun `Should throw if configuration specified by applicationId not found`() {
        val jsonFilePath = createTempGoogleServiceJson(
            clients = listOf(
                AndroidClientInfo(
                    packageName = "com.example.sample.fbase3",
                    mobileSdkAppId = "1:123456789000:android:f1bf012572b04063",
                ),
                AndroidClientInfo(
                    packageName = "com.example.sample.fbase2",
                    mobileSdkAppId = "1:123456789000:android:f1bf012572b04062",
                ),
            ),
        )
        val source = createGoogleServicesValueSource(
            configurationFiles = listOf(jsonFilePath),
            applicationId = "com.example.sample.fbase",
        )

        assertFailure {
            source.get()
        }.all {
            hasClass(GradleException::class.java)
            messageContains("Can not find configuration for Android application with id")
        }
    }

    @Test
    fun `Should use single configuration if applicationId not specified`() {
        val jsonFile = createTempGoogleServiceJson(
            clients = listOf(
                AndroidClientInfo(
                    packageName = "com.example.sample.fbase3",
                    mobileSdkAppId = "1:123456789000:android:f1bf012572b04063",
                ),
            ),
        )
        val source = createGoogleServicesValueSource(
            configurationFiles = listOf(jsonFile),
            applicationId = "",
        )

        val config = source.get()

        assertThat(config.applicationId).isEqualTo("1:123456789000:android:f1bf012572b04063")
    }

    @Test
    fun `Should throw if applicationId not specified and has multiple configurations`() {
        val jsonFile = createTempGoogleServiceJson(
            clients = listOf(
                AndroidClientInfo(
                    packageName = "com.example.sample.fbase3",
                    mobileSdkAppId = "1:123456789000:android:f1bf012572b04063",
                ),
                AndroidClientInfo(
                    packageName = "com.example.sample.fbase4",
                    mobileSdkAppId = "1:123456789000:android:f1bf012572b04064",
                ),
            ),
        )
        val source = createGoogleServicesValueSource(
            configurationFiles = listOf(jsonFile),
            applicationId = "",
        )

        assertFailure {
            source.get()
        }.all {
            hasClass(GradleException::class.java)
            messageContains("Found configurations for")
        }
    }

    private fun createGoogleServicesValueSource(
        configurationFiles: List<Path> = emptyList(),
        applicationId: String = "",
    ): Provider<FbaseOptions> = providers.of(GoogleServicesValueSource::class.java) { valueSource ->
        valueSource.parameters {
            it.configurationFiles.from(configurationFiles)
            it.applicationId.set(applicationId)
        }
    }

    private fun createTempGoogleServiceJson(
        filename: String = "google-services.json",
        clients: List<AndroidClientInfo>,
    ): Path {
        val googleServicesText = createGoogleServicesJsonText(clients)
        return tempDir.resolve(filename).createFile().apply {
            writeText(googleServicesText)
        }
    }

    private fun createGoogleServicesJsonText(
        clients: List<AndroidClientInfo>,
        projectNumber: String = "123456789000",
        projectId: String = "mockproject-1234",
    ): String {
        val clientInfos = clients.joinToString(", \n") { clientInfo ->
            clientInfo.toJson()
        }

        // language=JSON
        return """
            {
              "project_info": {
                "project_number": "$projectNumber",
                "project_id": "$projectId"
              },
              "client": [
                $clientInfos
              ]
            }
        """.trimIndent()
    }

    private fun AndroidClientInfo.toJson(): String {
        // language=JSON
        return """
            {
              "client_info": {
                "mobilesdk_app_id": "$mobileSdkAppId",
                "android_client_info": {
                  "package_name": "$packageName",
                  "certificate_hash": []
                }
              },
              "api_key": [ { "current_key": "$apiKey" } ]
            }
        """.trimIndent()
    }

    private data class AndroidClientInfo(
        val mobileSdkAppId: String = "1:123456789000:android:f1bf012572b04063",
        val packageName: String = "com.example.sample.fbase",
        val apiKey: String = "AIzbSzCn1N6LWIe6wthYyrgUUSAlUsdqMb-wvTo",
    )
}
