/*
 * Copyright (c) 2023, the fbase-options-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.options

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.gradle.fbase.options.testutil.android.junit.AndroidProjectExtension

/**
 * A simple functional test for the 'ru.pixnews.gradle.fbase.config.greeting' plugin.
 */
class FbaseConfigGradlePluginPluginFunctionalTest {
    @JvmField
    @RegisterExtension
    var project = AndroidProjectExtension()

    @Test
    fun `can build project`() {
        project.setupTestProject("android-app-simple")

        val result = project.build("build")

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }
}
