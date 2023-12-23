/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.SubmoduleFixtures
import ru.pixnews.gradle.fbase.android.junit.AndroidProjectExtension

class PropertiesFileTests {
    @JvmField
    @RegisterExtension
    var project = AndroidProjectExtension()
    val submoduleFixtures = SubmoduleFixtures()

    @Test
    fun `Should fail if properties file not found`() {
        val submoduleName = "android-app-no-properties-file"
        project.setupTestProjectScaffold(submoduleName)

        val buildGradleKts = submoduleFixtures.buildGradleKts(
            """
        firebaseConfig {
           configurations {
               create("firebaseOptions") {
                   fromPropertiesFile {
                       location = layout.projectDirectory.file("nonexistent.properties")
                   }
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

        val result = project.buildAndFail("assemble")

        Assertions.assertTrue(result.output.contains("java.io.FileNotFoundException"))
    }
}
