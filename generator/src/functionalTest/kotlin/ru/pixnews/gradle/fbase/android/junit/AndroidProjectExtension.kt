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
import ru.pixnews.gradle.fbase.android.fixtures.FileContent
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.DEFAULT_NAMESPACE
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.Root
import java.io.File
import java.nio.file.Files
import java.util.Optional

@Suppress("TooManyFunctions")
class AndroidProjectExtension : BeforeEachCallback, TestWatcher {
    internal val testProjectsRoot
        get() = File(System.getProperty("user.dir"), "src/testProjects")
    internal val testFilesRoot
        get() = File(System.getProperty("user.dir"), "src/testFiles")
    lateinit var rootDir: File

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

    public fun submodule(
        submoduleName: String,
        namespace: String = DEFAULT_NAMESPACE,
    ): Submodule = Submodule(rootDir, submoduleName, namespace)

    fun setupTestProject(
        name: String,
        namespace: String = DEFAULT_NAMESPACE,
    ) = setupTestProject(testProjectsRoot.resolve(name), namespace)

    fun setupTestProject(
        projectDir: File,
        namespace: String = DEFAULT_NAMESPACE,
    ) {
        val submoduleName = projectDir.name
        setupTestProjectScaffold(submoduleName, namespace)

        projectDir.copyRecursively(rootDir.resolve(submoduleName), overwrite = true)
    }

    fun setupTestProjectScaffold(
        submoduleName: String,
        namespace: String = DEFAULT_NAMESPACE,
    ) {
        setupRoot(submoduleName)
        val submodule = submodule(submoduleName, namespace)
        submodule.writeFilesToSubmoduleRoot(
            submodule.fixtures.androidManifestXml,
            submodule.fixtures.mainActivity,
            submodule.fixtures.buildGradleKts(),
        )
    }

    fun buildWithGradleVersion(
        gradleVersion: String?,
        expectFail: Boolean,
        vararg args: String,
    ): BuildResult {
        val runner = GradleRunner.create().apply {
            forwardOutput()
            withArguments(
                "-Dorg.gradle.daemon.performance.enable-monitoring=false",
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

    fun writeFilesToRoot(
        vararg files: FileContent,
    ) = writeFiles(rootDir, *files)

    fun writeFiles(
        root: File,
        vararg files: FileContent,
    ) {
        files.forEach {
            val dst = root.resolve(it.dstPath)
            dst.parentFile.mkdirs()
            dst.writeText(it.content)
        }
    }

    private fun cleanup() {
        rootDir.deleteRecursively()
    }

    private fun setupRoot(
        vararg includeSubprojects: String,
    ) {
        writeFiles(
            root = rootDir,
            files = arrayOf(
                Root.localProperties,
                Root.libsVersionToml,
                Root.gradleProperties,
                Root.buildGradleKts,
                Root.settingsGradleKts(*includeSubprojects),
            ),
        )
    }
}
