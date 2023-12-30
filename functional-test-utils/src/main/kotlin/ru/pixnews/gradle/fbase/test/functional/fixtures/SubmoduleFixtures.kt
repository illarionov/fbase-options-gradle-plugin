/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.fixtures

import ru.pixnews.gradle.fbase.test.functional.junit.FileContent
import ru.pixnews.gradle.fbase.test.functional.junit.SubmoduleDsl
import java.io.File

public val SubmoduleDsl.fixtures: SubmoduleFixtures
    get() = SubmoduleFixtures(id.namespace)

public class SubmoduleFixtures internal constructor(
    public val namespace: String,
) {
    public val androidManifestXml: FileContent by lazy {
        testFilesSubmoduleFileContent("src/main/AndroidManifest.xml")
    }
    public val mainActivity: FileContent by lazy {
        val dstPath = "src/main/kotlin/${namespace.namespaceToPackage()}/MainActivity.kt"
        val content = """
            package $namespace
            import android.app.Activity
            class MainActivity : Activity()
        """.trimIndent()
        FileContent(dstPath, content)
    }
    public val application: FileContent by lazy {
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
    public val googleServicesJson: FileContent by lazy {
        testFilesSubmoduleFileContent("google-services.json")
    }
    public val googleServicesJsonHuge: FileContent by lazy {
        testFilesSubmoduleFileContent("google-services-huge.json").copy(
            dstPath = "google-services.json",
        )
    }

    public fun buildGradleKts(
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
