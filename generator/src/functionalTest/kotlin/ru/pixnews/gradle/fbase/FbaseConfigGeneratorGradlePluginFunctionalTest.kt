/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("LongMethod")

package ru.pixnews.gradle.fbase

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.gradle.fbase.test.functional.assertions.outcomeOfTask
import ru.pixnews.gradle.fbase.test.functional.fixtures.RootProjectFixtures
import ru.pixnews.gradle.fbase.test.functional.fixtures.TestSubmodules
import ru.pixnews.gradle.fbase.test.functional.fixtures.fixtures
import ru.pixnews.gradle.fbase.test.functional.junit.AndroidProjectExtension
import ru.pixnews.gradle.fbase.test.functional.junit.SubmoduleId

class FbaseConfigGeneratorGradlePluginFunctionalTest {
    @JvmField
    @RegisterExtension
    var projectBuilder = AndroidProjectExtension(FBASE_VERSION)

    @Test
    fun `can build project if configurations are not defined`() {
        val submoduleId = SubmoduleId(
            projectName = "android-app-noconf",
            namespace = "com.example.samplefbase",
        )

        val submodule = projectBuilder.setupTestProjectScaffold(submoduleId)
            .subProject(submoduleId)

        val buildGradleKts = submodule.fixtures.buildGradleKts(
            """
            firebaseConfig {}
        """.trimIndent(),
        )
        val application = submodule.fixtures.application.copy(
            content = """
                package ${submodule.id.namespace}
                import android.app.Application
                class Application : Application()
        """.trimIndent(),
        )

        submodule.writeFiles(
            buildGradleKts,
            application,
        )

        val result = projectBuilder.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `task should be up-to-date on second build and configuration cache should be reused`() {
        projectBuilder.setupTestProject(TestSubmodules.androidAppSimple)

        projectBuilder.build("clean")

        val generateOptionsTask = ":android-app-simple:releaseGenerateFirebaseOptions"
        projectBuilder.build(
            "--configuration-cache",
            generateOptionsTask,
        ).let { result ->
            assertThat(result).outcomeOfTask(generateOptionsTask).isEqualTo(SUCCESS)
        }
        projectBuilder.build(
            "--configuration-cache",
            generateOptionsTask,
        ).let { result ->
            assertThat(result).outcomeOfTask(generateOptionsTask).isEqualTo(UP_TO_DATE)
            assertThat(result.output).contains("Reusing configuration cache.")
        }
    }

    @Test
    fun `can build project with default value if source is not set`() {
        val submoduleId = SubmoduleId(
            projectName = "android-app-no-source-set",
            namespace = "com.example.samplefbase",
        )
        val rootProject = projectBuilder.setupTestProjectScaffold(submoduleId)
        val submodule = rootProject.subProject(submoduleId)

        val buildGradleKts = submodule.fixtures.buildGradleKts(
            """
            firebaseConfig {
               configurations {
                   create("firebaseOptions") {
                   }
               }
            }
        """.trimIndent(),
        )
        submodule.writeFiles(
            buildGradleKts,
            submodule.fixtures.application,
        )
        rootProject.writeFiles(
            RootProjectFixtures.defaultFirebaseProperties,
        )

        val result = projectBuilder.build("assemble")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `should fail when multiple configurations are defined and the main configuration is not set`() {
        val submoduleId = SubmoduleId(
            projectName = "android-app-multimple-config-no-primary",
            namespace = "com.example.samplefbase",
        )
        val rootProject = projectBuilder.setupTestProjectScaffold(submoduleId)
        val submodule = rootProject.subProject(submoduleId)

        val buildGradleKts = submodule.fixtures.buildGradleKts(
            """
            firebaseConfig {
               configurations {
                   create("firebaseOptions1") {
                   }
                   create("firebaseOptions2") {
                   }
               }
            }
        """.trimIndent(),
        )
        submodule.writeFiles(
            buildGradleKts,
            submodule.fixtures.application,
        )
        rootProject.writeFiles(RootProjectFixtures.defaultFirebaseProperties)

        val result = projectBuilder.buildAndFail("assemble")

        assertTrue(
            result.output.contains(
                "FbaseGeneratorExtension.primaryConfiguration must be set when using multiple configurations",
            ),
        )
    }

    @Test
    fun `should fail when multiple configurations are defined and the main configuration is set to a non-existent`() {
        val submoduleId = SubmoduleId(
            projectName = "android-app-multimple-config-wrong-primary",
            namespace = "com.example.samplefbase",
        )
        val rootProject = projectBuilder.setupTestProjectScaffold(submoduleId)
        val submodule = rootProject.subProject(submoduleId)

        val buildGradleKts = submodule.fixtures.buildGradleKts(
            """
            firebaseConfig {
               primaryConfiguration = "firebaseOptions3"
               configurations {
                   create("firebaseOptions1") {
                   }
                   create("firebaseOptions2") {
                   }
               }
            }
        """.trimIndent(),
        )
        rootProject.writeFiles(RootProjectFixtures.defaultFirebaseProperties)
        submodule.writeFiles(
            buildGradleKts,
            submodule.fixtures.application,
        )

        val result = projectBuilder.buildAndFail("assemble")

        assertTrue(
            result.output.contains(
                "Configuration named `firebaseOptions3` is not defined",
            ),
        )
    }
}
