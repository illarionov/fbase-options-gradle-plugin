/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import assertk.assertFailure
import assertk.assertions.cause
import assertk.assertions.isNotNull
import assertk.assertions.messageContains
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class FbaseConfigGradlePluginPluginTest {
    @Test
    fun `plugin should fail if Android plugin is not applied`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("ru.pixnews.gradle.fbase")
        assertFailure {
            (project as? ProjectInternal)?.evaluate()
        }.cause()
            .isNotNull()
            .messageContains("can only be applied to an Android project")
    }
}
