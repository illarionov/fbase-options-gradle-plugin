/*
 * Copyright (c) 2023, the fbase-options-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.options

import com.squareup.kotlinpoet.ClassName
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import ru.pixnews.gradle.fbase.options.data.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.options.util.getWarnIfNotPresent

/**
 * Generates GeneratedFirebaseOptions.firebaseOptions using values from
 * configuration file
 */
abstract class GenerateFirebaseOptionsTask : DefaultTask() {
    @get:Input
    @get:Optional
    abstract val firebaseConfig: Property<LocalFirebaseOptions>

    @get:Input
    abstract val outputObjectPackage: Property<String>

    @get:Input
    abstract val outputObjectName: Property<String>

    @get:Input
    abstract val outputPropertyName: Property<String>

    @get:OutputDirectory
    abstract val sourceOutputDir: DirectoryProperty

    @TaskAction
    fun doTaskAction() {
        val codegenDir = sourceOutputDir.asFile.get()
        codegenDir.listFiles()?.forEach { it.deleteRecursively() }

        val config = firebaseConfig.getWarnIfNotPresent(
            logger = logger,
            name = "Firebase",
            ifNotPresent = LocalFirebaseOptions.Companion::empty,
        )

        FirebaseOptionsGenerator(
            options = config,
            codeGenDir = codegenDir,
            outputObjectClassName = ClassName(
                outputObjectPackage.get(),
                outputObjectName.get(),
            ),
            propertyName = outputPropertyName.get(),
        ).generate()
    }
}
