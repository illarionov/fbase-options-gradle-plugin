/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.DEFAULT_NAMESPACE
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.Root
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.SubmoduleFixtures
import ru.pixnews.gradle.fbase.android.junit.AndroidProjectExtension

class FbaseConfigGeneratorGradlePluginFunctionalTest {
    @JvmField
    @RegisterExtension
    var project = AndroidProjectExtension()

    @Test
    fun `can build simple project`() {
        project.setupTestProject("android-app-simple")

        val result = project.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `can build project with multiple configurations`() {
        project.setupTestProject("android-app-multiconfig")

        val result = project.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `can build project if configurations are not defined`() {
        val submoduleName = "android-app-noconf"
        project.setupTestProjectScaffold(submoduleName)
        val submoduleFixtures = SubmoduleFixtures()

        val buildGradleKts = submoduleFixtures.buildGradleKts(
            """
            firebaseConfig {}
        """.trimIndent(),
        )
        val application = submoduleFixtures.application.copy(
            content = """
                package $DEFAULT_NAMESPACE
                import android.app.Application
                class Application : Application()
        """.trimIndent(),
        )

        project.writeFilesToSubmoduleRoot(
            submoduleName = submoduleName,
            buildGradleKts,
            application,
        )

        val result = project.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `can build project with default value if source is not set`() {
        val submoduleName = "android-app-no-def-src"
        project.setupTestProjectScaffold(submoduleName)
        val submoduleFixtures = SubmoduleFixtures()

        val buildGradleKts = submoduleFixtures.buildGradleKts(
            """
            firebaseConfig {
               configurations {
                   create("firebaseOptions") {
                   }
               }
            }
        """.trimIndent(),
        )
        project.writeFilesToSubmoduleRoot(
            submoduleName = submoduleName,
            buildGradleKts,
        )
        project.writeFiles(project.rootDir, Root.defaultFirebaseProperties)

        val result = project.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }
}
