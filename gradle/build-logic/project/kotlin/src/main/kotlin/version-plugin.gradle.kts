/*
 * Copyright (c) 2024, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")

package ru.pixnews.gradle.fbase.buildlogic.project.kotlin.version

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

private val versionProvider: Provider<String> = providers.provider { project.version.toString() }
private val writeVersionNumberTaskTest = registerWriteVersionNumberTask("test")
private val writeVersionNumberTaskFunctionalTest = registerWriteVersionNumberTask("functionalTest")
private val writeVersionNumberTaskMatrixTest = registerWriteVersionNumberTask("functionalMatrixTest")

plugins.withType<KotlinBasePlugin> {
    extensions.configure<KotlinJvmProjectExtension>("kotlin") {
        sourceSets {
            named("test") {
                kotlin.srcDirs(writeVersionNumberTaskTest)
            }
            matching { return@matching it.name == "functionalTest" }.configureEach {
                kotlin.srcDirs(writeVersionNumberTaskFunctionalTest)
            }
            matching { return@matching it.name == "functionalMatrixTest" }.configureEach {
                kotlin.srcDirs(writeVersionNumberTaskMatrixTest)
            }
        }
    }
}

private fun registerWriteVersionNumberTask(
    variant: String,
) = tasks.register<WriteVersionNumberTask>("${variant}WriteVersionNumber") {
    fbaseVersion.set(versionProvider)
    outputDir.set(project.layout.buildDirectory.dir("${variant}FbaseVersion"))
}
