/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.android.junit

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher
import ru.pixnews.gradle.fbase.android.fixtures.SubmoduleId
import java.io.File
import java.nio.file.Files
import java.util.Optional

@Suppress("TooManyFunctions")
class AndroidProjectExtension : BeforeEachCallback, TestWatcher {
    internal val testProjectsRoot
        get() = File(System.getProperty("user.dir"), "src/testProjects")
    private lateinit var rootDir: File

    override fun beforeEach(context: ExtensionContext?) {
        rootDir = Files.createTempDirectory("fbase-test").toFile()
    }

    override fun testSuccessful(context: ExtensionContext?) {
        cleanup()
    }

    override fun testAborted(context: ExtensionContext?, cause: Throwable?) {
        cleanup()
    }

    override fun testFailed(context: ExtensionContext?, cause: Throwable?) {
        // do not clean up, leave a temporary rootDir directory for future inspection
    }

    override fun testDisabled(context: ExtensionContext?, reason: Optional<String>?) = Unit

    fun setupTestProject(
        submoduleId: SubmoduleId,
    ): RootProjectDsl = RootProjectDsl.setupTestProjectScaffold(
        rootDir,
        submoduleId,
    ).apply {
        val testProjectDir = testProjectsRoot.resolve(submoduleId.projectName)
        testProjectDir.copyRecursively(
            target = rootDir.resolve(submoduleId.projectName),
            overwrite = true,
        )
    }

    fun setupTestProjectScaffold(
        submoduleId: SubmoduleId,
    ): RootProjectDsl = RootProjectDsl.setupTestProjectScaffold(
        rootDir,
        submoduleId,
    )

    fun buildWithGradleVersion(
        gradleVersion: String?,
        expectFail: Boolean,
        vararg args: String,
    ): BuildResult {
        val runner = GradleRunner.create().apply {
            forwardOutput()
            withArguments(
                "--stacktrace",
                *args,
            )
            withProjectDir(rootDir)
            if (gradleVersion != null) {
                withGradleVersion(gradleVersion)
            }
        }
        return if (!expectFail) {
            runner.build()
        } else {
            runner.buildAndFail()
        }
    }

    fun build(vararg args: String) = buildWithGradleVersion(null, false, *args)

    fun buildAndFail(vararg args: String) = buildWithGradleVersion(null, true, *args)

    private fun cleanup() {
        rootDir.deleteRecursively()
    }
}
