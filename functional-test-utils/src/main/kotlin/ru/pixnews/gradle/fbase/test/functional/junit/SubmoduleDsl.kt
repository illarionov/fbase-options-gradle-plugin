/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.junit

import ru.pixnews.gradle.fbase.test.functional.apk.ApkAnalyzer
import ru.pixnews.gradle.fbase.test.functional.util.getApkPath
import java.io.File

public class SubmoduleDsl(
    private val projectRoot: File,
    public val id: SubmoduleId,
) {
    public val root: File
        get() = projectRoot.resolve(id.projectName)

    public val apkDir: File
        get() = root.resolve("build/outputs/apk/")

    public fun apk(
        buildType: String,
        vararg flavors: String,
    ): ApkAnalyzer {
        val apkPath = getApkPath(id.projectName, buildType, *flavors)
        return ApkAnalyzer(apkDir.resolve(apkPath))
    }

    public fun writeFiles(
        vararg files: FileContent,
    ) {
        files.forEach {
            val dst = root.resolve(it.dstPath)
            dst.parentFile.mkdirs()
            dst.writeText(it.content)
        }
    }
}
