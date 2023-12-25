/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.android.junit

import ru.pixnews.gradle.fbase.android.fixtures.FileContent
import ru.pixnews.gradle.fbase.android.fixtures.ProjectFixtures.Root
import ru.pixnews.gradle.fbase.android.fixtures.SubmoduleId
import java.io.File

class RootProjectDsl private constructor(
    val rootDir: File,
) {
    fun subProject(
        submoduleId: SubmoduleId,
    ): SubmoduleDsl = SubmoduleDsl(rootDir, submoduleId)

    fun writeFiles(
        vararg files: FileContent,
    ) {
        files.forEach {
            val dst = rootDir.resolve(it.dstPath)
            dst.parentFile.mkdirs()
            dst.writeText(it.content)
        }
    }

    internal companion object {
        internal fun setupTestProjectScaffold(
            rootDir: File,
            vararg submodules: SubmoduleId,
        ): RootProjectDsl = RootProjectDsl(rootDir).apply {
            setupRoot(
                rootProject = this,
                submodules = submodules,
            )
            submodules.forEach { submoduleId ->
                val submodule = subProject(submoduleId)
                submodule.writeFiles(
                    submodule.fixtures.androidManifestXml,
                    submodule.fixtures.mainActivity,
                    submodule.fixtures.buildGradleKts(),
                )
            }
        }

        private fun setupRoot(
            rootProject: RootProjectDsl,
            vararg submodules: SubmoduleId,
        ) {
            rootProject.writeFiles(
                files = arrayOf(
                    Root.localProperties,
                    Root.libsVersionToml,
                    Root.gradleProperties,
                    Root.buildGradleKts,
                    Root.settingsGradleKts(
                        includeSubprojects = submodules
                            .map(SubmoduleId::projectName)
                            .toTypedArray(),
                    ),
                ),
            )
        }
    }
}
