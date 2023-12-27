/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.fixtures

import ru.pixnews.gradle.fbase.junit.FileContent
import ru.pixnews.gradle.fbase.junit.SubmoduleDsl
import java.io.File

val SubmoduleDsl.fixtures: SubmoduleFixtures
    get() = SubmoduleFixtures(id.namespace)

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
        File(FixturesPaths.testFilesRoot, "submodule").resolve(dstPath).readText(),
    )
}
