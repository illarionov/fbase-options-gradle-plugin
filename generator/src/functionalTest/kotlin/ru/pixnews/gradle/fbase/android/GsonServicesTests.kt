/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.android

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.SubmoduleFixtures
import ru.pixnews.gradle.fbase.android.junit.AndroidProjectExtension

class GsonServicesTests {
    @JvmField
    @RegisterExtension
    var project = AndroidProjectExtension()

    @Test
    fun `Should build project with default config from google-services`() {
        val submoduleName = "android-app-google-services"
        val namespace = "com.example.myapplication"
        val submoduleFixtures = SubmoduleFixtures(namespace)

        project.setupTestProject(
            submoduleName,
            namespace = "com.example.myapplication"
        )

        project.writeFilesToSubmoduleRoot(
            submoduleName = submoduleName,
            submoduleFixtures.googleServicesJson,
        )

        val result = project.build("assemble")

        Assertions.assertTrue(result.output.contains("BUILD SUCCESSFUL"))

        val submoduleApkDir = project.submoduleOutputApkDir(submoduleName)

    }
}
