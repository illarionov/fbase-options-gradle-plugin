/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.junit

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher
import ru.pixnews.gradle.fbase.test.functional.fixtures.FixturesPaths.testProjectsRoot
import ru.pixnews.gradle.fbase.test.functional.testmatrix.Version
import ru.pixnews.gradle.fbase.test.functional.testmatrix.VersionCatalog
import java.io.File
import java.nio.file.Files
import java.util.Optional

@Suppress("TooManyFunctions")
public class AndroidProjectExtension(
    fbasePluginVersion: String,
) : BeforeEachCallback, TestWatcher {
    public val defaultVersionCatalog: VersionCatalog = VersionCatalog.getDefault(fbasePluginVersion)
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

    override fun testDisabled(context: ExtensionContext?, reason: Optional<String>?): Unit = Unit

    public fun setupTestProject(
        submoduleId: SubmoduleId,
        versions: VersionCatalog = defaultVersionCatalog,
    ): RootProjectDsl = RootProjectDsl.setupTestProjectScaffold(
        rootDir,
        versions,
        submoduleId,
    ).apply {
        val testProjectDir = testProjectsRoot.resolve(submoduleId.projectName)
        testProjectDir.copyRecursively(
            target = rootDir.resolve(submoduleId.projectName),
            overwrite = true,
        )
    }

    public fun setupTestProjectScaffold(
        submoduleId: SubmoduleId,
        versions: VersionCatalog = defaultVersionCatalog,
    ): RootProjectDsl = RootProjectDsl.setupTestProjectScaffold(
        rootDir,
        versions,
        submoduleId,
    )

    public fun buildWithGradleVersion(
        gradleVersion: Version = defaultVersionCatalog.gradleVersion,
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
            withGradleVersion(gradleVersion.toString())
        }
        return if (!expectFail) {
            runner.build()
        } else {
            runner.buildAndFail()
        }
    }

    public fun build(
        vararg args: String,
    ): BuildResult = buildWithGradleVersion(
        expectFail = false,
        args = args,
    )

    public fun build(
        gradleVersion: Version = defaultVersionCatalog.gradleVersion,
        vararg args: String,
    ): BuildResult = buildWithGradleVersion(
        gradleVersion = gradleVersion,
        expectFail = false,
        args = args,
    )

    public fun buildAndFail(
        vararg args: String,
    ): BuildResult = buildWithGradleVersion(
        expectFail = true,
        args = args,
    )

    public fun buildAndFail(
        gradleVersion: Version = defaultVersionCatalog.gradleVersion,
        vararg args: String,
    ): BuildResult = buildWithGradleVersion(
        gradleVersion = gradleVersion,
        expectFail = true,
        args = args,
    )

    private fun cleanup() {
        rootDir.deleteRecursively()
    }
}
