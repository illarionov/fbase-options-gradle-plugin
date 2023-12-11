/*
 * Copyright (c) 2023, the fbase-options-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.options

import com.squareup.kotlinpoet.ClassName
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import ru.pixnews.gradle.fbase.options.data.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.options.data.TargetVisibility
import ru.pixnews.gradle.fbase.options.util.getWarnIfNotPresent
import java.io.File

/**
 * Generates GeneratedFirebaseOptions.firebaseOptions using values from
 * configuration file
 */
abstract class GenerateFirebaseOptionsTask : DefaultTask() {
    @get:OutputDirectory
    abstract val sourceOutputDir: DirectoryProperty

    @get:Nested
    abstract val configs: ListProperty<GenerateOptionsTaskParams>

    @TaskAction
    fun doTaskAction() {
        val codegenDir = sourceOutputDir.asFile.get()
        codegenDir.listFiles()?.forEach { it.deleteRecursively() }

        configs.get().map {
            createGenerator(it, codegenDir)
        }.forEach {
            it.generate()
        }
    }

    private fun createGenerator(
        params: GenerateOptionsTaskParams,
        codegenDir: File,
    ): FirebaseOptionsGenerator {
        val config = params.source.getWarnIfNotPresent(
            logger = logger,
            name = "Firebase",
            ifNotPresent = LocalFirebaseOptions.Companion::empty,
        )

        return FirebaseOptionsGenerator(
            options = config,
            codeGenDir = codegenDir,
            outputObjectClassName = ClassName(
                params.targetPackage.get(),
                params.targetObjectName.get(),
            ),
            propertyName = params.propertyName.get(),
            visibility = params.visibility.getOrElse(TargetVisibility.INTERNAL),
        )
    }
}
