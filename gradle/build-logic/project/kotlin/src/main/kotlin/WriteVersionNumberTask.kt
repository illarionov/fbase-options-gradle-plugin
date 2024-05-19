/*
 * Copyright (c) 2024, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.buildlogic.project.kotlin.version

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class WriteVersionNumberTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val fbaseVersion: Property<String>

    @TaskAction
    fun generateVersion() {
        val outputDir = outputDir.asFile.get()
        outputDir.listFiles()?.forEach { it.deleteRecursively() }

        val outputFile = outputDir.resolve("FbaseVersion.kt")
        outputFile.writeText(
            """
            package ru.pixnews.gradle.fbase
            public const val FBASE_VERSION: String = "${fbaseVersion.get()}"
        """.trimIndent(),
        )
    }
}
