/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.android.junit

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import ru.pixnews.gradle.fbase.android.util.androidHome
import java.io.File
import java.nio.file.Files

@Suppress("TooManyFunctions")
class AndroidProjectExtension : BeforeEachCallback, AfterEachCallback {
    val rootBuildFile by lazy { rootDir.resolve("build.gradle.kts") }
    val rootSettingsFile by lazy { rootDir.resolve("settings.gradle.kts") }
    internal val testProjectsRoot
        get() = File(System.getProperty("user.dir"), "src/testProjects")
    internal val testFilesRoot
        get() = File(System.getProperty("user.dir"), "src/testFiles")
    lateinit var rootDir: File

    override fun beforeEach(context: ExtensionContext?) {
        rootDir = Files.createTempDirectory("fbase-test").toFile()
        setupLocalProperties()
        setupVersionCatalog()
    }

    override fun afterEach(context: ExtensionContext?) {
        rootDir.deleteRecursively()
    }

    fun setupTestProject(name: String) = setupTestProject(testProjectsRoot.resolve(name))

    fun setupTestProject(
        projectDir: File,
    ) {
        val submoduleName = projectDir.name
        projectDir.copyRecursively(this.rootDir.resolve(submoduleName), overwrite = true)
        setupGradleProperties()
        setupRootBuildGradle()
        setupRootModuleSettings(submoduleName)
    }

    fun buildWithGradleVersion(
        gradleVersion: String?,
        expectFail: Boolean,
        vararg args: String,
    ): BuildResult {
        val runner = GradleRunner.create().apply {
            forwardOutput()
            // withPluginClasspath()
            withArguments("--stacktrace", *args)
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

    private fun setupLocalProperties() {
        val localProperties = rootDir.resolve("local.properties")
        localProperties.writeText("sdk.dir=${androidHome()}".trimIndent())
    }

    private fun setupVersionCatalog() {
        val srcVersionCatalog = testFilesRoot.resolve("libs.versions.toml")
        val dst = rootDir.resolve("gradle/libs.versions.toml")
        srcVersionCatalog.copyTo(dst, overwrite = false)
    }

    private fun setupRootModuleSettings(
        vararg includeSubprojects: String,
    ) {
        val srcSettings = testFilesRoot.resolve("settings.gradle.kts")
        val dst = rootDir.resolve("settings.gradle.kts")
        srcSettings.copyTo(dst, overwrite = false)
        dst.appendText(
            includeSubprojects.joinToString("\n") { """include("$it")""" },
        )
        val pluginSrcPath = File(System.getProperty("user.dir"), "../") // TODO: sanitise?
        dst.appendText(
            """
            pluginManagement {
                includeBuild("$pluginSrcPath")
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
        """.trimIndent(),
        )
    }

    private fun setupRootBuildGradle() {
        val srcSettings = testFilesRoot.resolve("build.gradle.kts")
        val dst = rootDir.resolve("build.gradle.kts")
        srcSettings.copyTo(dst, overwrite = false)
    }

    private fun setupGradleProperties() {
        val srcVersionCatalog = testFilesRoot.resolve("gradle.properties")
        val dst = rootDir.resolve("gradle.properties")
        srcVersionCatalog.copyTo(dst, overwrite = false)
    }
}
