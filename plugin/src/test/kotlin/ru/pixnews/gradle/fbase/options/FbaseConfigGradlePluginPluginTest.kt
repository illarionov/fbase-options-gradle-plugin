/*
 * Copyright (c) 2023, the fbase-options-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.options

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

/**
 * A simple unit test for the 'ru.pixnews.gradle.fbase.config.greeting' plugin.
 */
class FbaseConfigGradlePluginPluginTest {
    @Test
    fun `plugin registers task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("ru.pixnews.gradle.fbase.options")

        // TODO Verify the result
        // assertNotNull(project.tasks.findByName("greeting"))
    }
}
