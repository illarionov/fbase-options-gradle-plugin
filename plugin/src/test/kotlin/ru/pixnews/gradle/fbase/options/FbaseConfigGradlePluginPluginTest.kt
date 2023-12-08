/*
 * Copyright (c) 2023, the fbase-options-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.options

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.string.shouldContain
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class FbaseConfigGradlePluginPluginTest {
    @Test
    fun `plugin should fail if Android plugin is not applied`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("ru.pixnews.gradle.fbase.options")
        val exception = shouldThrowAny {
            (project as? ProjectInternal)?.evaluate()
        }.cause!!

        exception.message shouldContain "can only be applied to an Android project."
    }
}
