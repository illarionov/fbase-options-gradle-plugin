/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.fixtures

import ru.pixnews.gradle.fbase.junit.FileContent
import java.io.File

object FixturesPaths {
    internal val userDir: String
        get() = System.getProperty("user.dir")
    val testProjectsRoot
        get() = File(userDir, "src/testProjects")
    val testFilesRoot
        get() = File(userDir, "src/testFiles")

    internal fun testFilesRootFileContent(dstPath: String) = FileContent(
        dstPath,
        File(testFilesRoot, "root").resolve(dstPath).readText(),
    )
}
