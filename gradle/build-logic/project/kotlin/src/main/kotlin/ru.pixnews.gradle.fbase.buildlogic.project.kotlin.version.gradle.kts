/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")

import ru.pixnews.gradle.fbase.buildlogic.project.kotlin.version.WriteVersionNumberTask

plugins {
    id("org.jetbrains.kotlin.jvm") apply false
}

val versionProvider: Provider<String> = providers.provider { project.version.toString() }
val writeVersionNumberTaskTest = registerWriteVersionNumberTask("test")
val writeVersionNumberTaskFunctionalTest = registerWriteVersionNumberTask("functionalTest")

kotlin {
    sourceSets {
        test {
            kotlin.srcDirs(writeVersionNumberTaskTest)
        }

        matching { return@matching it.name == "functionalTest" }.configureEach {
            kotlin.srcDirs(writeVersionNumberTaskFunctionalTest)
        }
    }
}

private fun registerWriteVersionNumberTask(
    variant: String,
) = tasks.register<WriteVersionNumberTask>("${variant}WriteVersionNumber") {
    fbaseVersion.set(versionProvider)
    outputDir.set(project.layout.buildDirectory.dir("${variant}FbaseVersion"))
}
