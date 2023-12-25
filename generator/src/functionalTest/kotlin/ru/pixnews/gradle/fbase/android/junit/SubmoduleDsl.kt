/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.android.junit

import ru.pixnews.gradle.fbase.android.apk.ApkAnalyzer
import ru.pixnews.gradle.fbase.android.fixtures.FileContent
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.SubmoduleFixtures
import ru.pixnews.gradle.fbase.android.fixtures.SubmoduleId
import ru.pixnews.gradle.fbase.android.util.getApkPath
import java.io.File

class SubmoduleDsl(
    private val projectRoot: File,
    val id: SubmoduleId,
) {
    val root: File
        get() = projectRoot.resolve(id.projectName)

    val fixtures: SubmoduleFixtures = SubmoduleFixtures(id.namespace)

    val apkDir: File
        get() = root.resolve("build/outputs/apk/")

    fun apk(
        buildType: String,
        vararg flavors: String,
    ): ApkAnalyzer {
        val apkPath = getApkPath(id.projectName, buildType, *flavors)
        return ApkAnalyzer(apkDir.resolve(apkPath))
    }

    fun writeFiles(
        vararg files: FileContent,
    ) {
        files.forEach {
            val dst = root.resolve(it.dstPath)
            dst.parentFile.mkdirs()
            dst.writeText(it.content)
        }
    }
}
