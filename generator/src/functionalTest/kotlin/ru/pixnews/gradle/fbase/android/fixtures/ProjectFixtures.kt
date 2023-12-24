/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.android.fixtures

import ru.pixnews.gradle.fbase.android.util.androidHome
import java.io.File

object ProjectFixtures {
    private val userDir: String
        get() = System.getProperty("user.dir")
    public val testProjectsRoot
        get() = File(userDir, "src/testProjects")
    public val testFilesRoot
        get() = File(userDir, "src/testFiles")

    object Root {
        val libsVersionToml: FileContent by lazy { testFilesRootFileContent("gradle/libs.versions.toml") }
        val gradleProperties: FileContent by lazy { testFilesRootFileContent("gradle.properties") }
        val buildGradleKts: FileContent by lazy { testFilesRootFileContent("build.gradle.kts") }
        val localProperties: FileContent by lazy {
            FileContent(
                "local.properties",
                "sdk.dir=${androidHome()}".trimIndent(),
            )
        }
        val defaultFirebaseProperties by lazy { testFilesRootFileContent("config/firebase.properties") }

        fun settingsGradleKts(
            vararg includeSubprojects: String,
        ): FileContent {
            val settingsFile = testFilesRootFileContent("settings.gradle.kts")

            val includes = includeSubprojects.joinToString("\n") { """include("$it")""" }
            val pluginSrcPath = File(userDir, "../")
            val newContent = settingsFile.content + includes +
                    """
                    pluginManagement {
                        includeBuild("$pluginSrcPath")
                        repositories {
                            google()
                            mavenCentral()
                            gradlePluginPortal()
                        }
                    }
                    """.trimIndent()
            return FileContent(
                settingsFile.dstPath,
                newContent,
            )
        }

        private fun testFilesRootFileContent(dstPath: String) = FileContent(
            dstPath,
            File(testFilesRoot, "root").resolve(dstPath).readText(),
        )
    }

    object TestSubmodules {
        val androidAppSimple = SubmoduleId(
            projectName = "android-app-simple",
            namespace = "com.example.samplefbase",
        )
        val androidAppFlavors = SubmoduleId(
            projectName = "android-app-flavors",
            namespace = "com.example.samplefbase.flavors",
        )
        val androidAppMulticonfig = SubmoduleId(
            projectName = "android-app-multiconfig",
            namespace = "com.example.samplefbase",
        )
        val androidAppGoogleServicesProject1 = SubmoduleId(
            projectName = "android-app-google-services-project1",
            namespace = "com.example.myapplication",
        )
        val androidAppGoogleServicesCustomLocation = SubmoduleId(
            projectName = "android-app-google-services-custom-location",
            namespace = "com.example.myapplication",
        )
    }

    class SubmoduleFixtures internal constructor(
        val namespace: String,
    ) {
        val androidManifestXml: FileContent by lazy {
            testFilesSubmoduleFileContent("src/main/AndroidManifest.xml")
        }
        val mainActivity: FileContent by lazy {
            val dstPath = "src/main/kotlin/${namespace.namespaceToPackage()}/MainActivity.kt"
            val content = """
                package $namespace
                import android.app.Activity
                class MainActivity : Activity()
            """.trimIndent()
            FileContent(dstPath, content)
        }
        val application: FileContent by lazy {
            val dstPath = "src/main/kotlin/${namespace.namespaceToPackage()}/Application.kt"
            val content = """
                package $namespace

                import android.app.Application
                import $namespace.firebaseOptions
                import com.google.firebase.FirebaseApp

                class Application : Application() {
                    public var firebaseApp: FirebaseApp? = null

                    override fun onCreate() {
                        super.onCreate()
                        firebaseApp = FirebaseApp.initializeApp(this, firebaseOptions)
                    }
                }
            """.trimIndent()
            FileContent(dstPath, content)
        }
        val googleServicesJson: FileContent by lazy {
            testFilesSubmoduleFileContent("google-services.json")
        }

        fun buildGradleKts(
            additionalText: String = "",
        ): FileContent {
            val template = testFilesSubmoduleFileContent("build.gradle.kts")
            val text = template.content.replace("<<NAMESPACE>>", namespace)

            return FileContent(
                template.dstPath,
                text + additionalText,
            )
        }

        private fun String.namespaceToPackage(): String = this.replace('.', '/')

        private fun testFilesSubmoduleFileContent(dstPath: String) = FileContent(
            dstPath,
            File(testFilesRoot, "submodule").resolve(dstPath).readText(),
        )
    }
}
