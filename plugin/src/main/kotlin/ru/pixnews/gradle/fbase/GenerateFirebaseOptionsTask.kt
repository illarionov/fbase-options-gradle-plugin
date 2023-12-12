/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import ru.pixnews.gradle.fbase.internal.FirebaseOptionsGenerator
import ru.pixnews.gradle.fbase.internal.FirebaseOptionsGenerator.PropertyValues
import ru.pixnews.gradle.fbase.internal.util.getWarnIfNotPresent
import java.io.File

/**
 * Generates GeneratedFirebaseOptions.firebaseOptions using values from
 * configuration file
 */
public abstract class GenerateFirebaseOptionsTask : DefaultTask() {
    @get:OutputDirectory
    public abstract val sourceOutputDir: DirectoryProperty

    @get:Nested
    public abstract val configs: ListProperty<GenerateOptionsTaskParams>

    @TaskAction
    public fun doTaskAction() {
        val codegenDir = sourceOutputDir.asFile.get()
        codegenDir.listFiles()?.forEach { it.deleteRecursively() }

        configs.get().groupBy { it.targetPackage.get() to it.targetFileName.get() }.map { (target, properties) ->
            createGenerator(
                outputPackageName = target.first,
                outputFileName = target.second,
                properties = properties,
                codegenDir = codegenDir,
            )
        }.forEach {
            it.generate()
        }
    }

    private fun createGenerator(
        outputPackageName: String,
        outputFileName: String,
        properties: List<GenerateOptionsTaskParams>,
        codegenDir: File,
    ): FirebaseOptionsGenerator {
        val propertyValues = properties.map { props ->
            val firebaseOptions = props.source.getWarnIfNotPresent(
                logger = logger,
                name = "Firebase",
                ifNotPresent = LocalFirebaseOptions.Companion::empty,
            )
            PropertyValues(
                options = firebaseOptions,
                propertyName = props.propertyName.get(),
                visibility = props.visibility.get(),
            )
        }
        return FirebaseOptionsGenerator(
            codeGenDir = codegenDir,
            outputPackageName = outputPackageName,
            outputFileName = outputFileName,
            properties = propertyValues,
        )
    }
}
