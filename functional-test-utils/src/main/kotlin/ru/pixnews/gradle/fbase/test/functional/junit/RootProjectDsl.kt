/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.junit

import ru.pixnews.gradle.fbase.test.functional.fixtures.RootProjectFixtures
import ru.pixnews.gradle.fbase.test.functional.fixtures.fixtures
import ru.pixnews.gradle.fbase.test.functional.fixtures.toLibsVersionsToml
import ru.pixnews.gradle.fbase.test.functional.testmatrix.VersionCatalog
import java.io.File

public class RootProjectDsl private constructor(
    val rootDir: File,
) {
    public fun subProject(
        submoduleId: SubmoduleId,
    ): SubmoduleDsl = SubmoduleDsl(rootDir, submoduleId)

    public fun writeFiles(
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
            versions: VersionCatalog,
            vararg submodules: SubmoduleId,
        ): RootProjectDsl = RootProjectDsl(rootDir).apply {
            setupRoot(
                rootProject = this,
                versions = versions,
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
            versions: VersionCatalog,
            vararg submodules: SubmoduleId,
        ) {
            rootProject.writeFiles(
                files = arrayOf(
                    RootProjectFixtures.localProperties,
                    versions.toLibsVersionsToml(),
                    RootProjectFixtures.gradleProperties,
                    RootProjectFixtures.buildGradleKts,
                    RootProjectFixtures.settingsGradleKts(
                        includeSubprojects = submodules
                            .map(SubmoduleId::projectName)
                            .toTypedArray(),
                    ),
                ),
            )
        }
    }
}
