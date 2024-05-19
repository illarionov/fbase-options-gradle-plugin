/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.internal

import assertk.Assert
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.hasSameSizeAs
import assertk.assertions.isEqualToWithGivenProperties
import assertk.assertions.prop
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.writeText

class GoogleServicesJsonParserTest {
    @TempDir
    lateinit var tempDir: Path

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
            "[]",
            "{}",
            """{ "project_info": { }}""",
            """{ "project_info": { "project_number": "1", "project_id": "id" } }""",
            """{ "project_info": { "project_number": "1", "project_id": "id" } }""",
            """{ "project_info": { "project_number": "1", "project_id": "id" }, "client": [] }""",
            """{ "project_info": { "project_number": "1", "project_id": "id" }, "client": [{ "client_info": {}}] }""",
        ],
    )
    fun `parseGoogleServicesFile should throw exception on malformed input`(input: String) {
        val testFile = tempDir.resolve("test.json").createFile().apply {
            writeText(input)
        }

        assertFailure {
            parseGoogleServicesFile(testFile.toFile())
        }.hasClass(JsonParseException::class.java)
    }

    @Test
    fun `parseGoogleServicesFile should parse minimal google-services-json`() {
        val validGoogleServicesJson = """{
                 "project_info": {
                   "project_number": "1",
                   "project_id": "id"
                  },
                  "client": [
                    {
                      "client_info": {
                         "mobilesdk_app_id": "1"
                      },
                      "api_key": [ { "current_key": "key" } ]
                    }
                  ]
                }"""

        val testFile = tempDir.resolve("test.json").createFile().apply {
            writeText(validGoogleServicesJson)
        }

        val services = parseGoogleServicesFile(testFile.toFile())

        assertThat(services).isEqualToByProperties(
            GoogleServicesJson(
                projectInfo = ProjectInfo(
                    projectNumber = "1",
                    firebaseUrl = null,
                    projectId = "id",
                    storageBucket = null,
                ),
                clients = listOf(
                    Client(
                        mobileSdkAppId = "1",
                        packageName = null,
                        trackingId = null,
                        googleApiKey = "key",
                        defaultWebClientId = null,
                    ),
                ),
            ),
        )
    }

    @Test
    fun `parseGoogleServicesFile should parse full google-services-json`() {
        val testFile = File(
            javaClass.classLoader.getResource("google-services.json")?.file
                ?: error("No google-services.json"),
        )
        val services = parseGoogleServicesFile(testFile)

        assertThat(services.projectInfo).isEqualToByProperties(
            ProjectInfo(
                projectNumber = "123456789000",
                firebaseUrl = "https://mockproject-1234.firebaseio.com",
                projectId = "mockproject-1234",
                storageBucket = "mockproject-1234-em.appspot.com",
            ),
        )

        assertThat(services.clients.first()).isEqualToByProperties(GOOGLE_SERVICES_FIRST_CLIENT)
    }

    private fun Assert<GoogleServicesJson>.isEqualToByProperties(other: GoogleServicesJson) {
        this.prop(GoogleServicesJson::projectInfo).isEqualToByProperties(other.projectInfo)
        this.prop(GoogleServicesJson::clients).given { list ->
            assertThat(list).hasSameSizeAs(other.clients)
            list.indices.forEach {
                assertThat(list[it]).isEqualToByProperties(other.clients[it])
            }
        }
    }

    private fun Assert<ProjectInfo>.isEqualToByProperties(other: ProjectInfo) = isEqualToWithGivenProperties(
        other,
        ProjectInfo::projectId,
        ProjectInfo::projectNumber,
        ProjectInfo::firebaseUrl,
        ProjectInfo::storageBucket,
    )

    private fun Assert<Client>.isEqualToByProperties(other: Client) = isEqualToWithGivenProperties(
        other,
        Client::mobileSdkAppId,
        Client::packageName,
        Client::trackingId,
        Client::googleApiKey,
        Client::defaultWebClientId,
    )

    private companion object {
        val GOOGLE_SERVICES_FIRST_CLIENT = Client(
            mobileSdkAppId = "1:123456789000:android:f1bf012572b04063",
            packageName = "com.google.samples.quickstart.admobexample",
            trackingId = null,
            googleApiKey = "AIzbSzCn1N6LWIe6wthYyrgUUSAlUsdqMb-wvTo",
            defaultWebClientId = "123456789000-e4uksm38sne0bqrj6uvkbo4oiu4hvigl.apps.googleusercontent.com",
        )
    }
}
