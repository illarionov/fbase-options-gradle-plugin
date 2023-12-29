/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.fixtures

import ru.pixnews.gradle.fbase.test.functional.junit.FileContent
import java.io.File

public object FixturesPaths {
    internal val userDir: String
        get() = System.getProperty("user.dir")
    public val testProjectsRoot: File
        get() = File(userDir, "src/testProjects")
    public val testFilesRoot: File
        get() = File(userDir, "src/testFiles")

    internal fun testFilesRootFileContent(dstPath: String) = FileContent(
        dstPath,
        File(testFilesRoot, "root").resolve(dstPath).readText(),
    )
}
