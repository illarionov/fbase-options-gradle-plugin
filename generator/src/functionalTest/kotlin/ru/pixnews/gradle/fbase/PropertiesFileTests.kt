/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.gradle.fbase.android.fixtures.SubmoduleId
import ru.pixnews.gradle.fbase.android.junit.AndroidProjectExtension

class PropertiesFileTests {
    @JvmField
    @RegisterExtension
    var project = AndroidProjectExtension()

    @Test
    fun `Should fail if properties file not found`() {
        val submoduleId = SubmoduleId(
            projectName = "android-app-no-properties-file",
            namespace = "com.example.samplefbase",
        )
        val submodule = project.setupTestProjectScaffold(submoduleId).subProject(submoduleId)

        val buildGradleKts = submodule.fixtures.buildGradleKts(
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
        submodule.writeFiles(
            buildGradleKts,
            submodule.fixtures.application,
        )

        val result = project.buildAndFail("assemble")

        Assertions.assertTrue(result.output.contains("java.io.FileNotFoundException"))
    }
}
