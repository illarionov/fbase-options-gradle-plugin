/*
 * Copyright (c) 2023-2024, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.buildlogic.settings

/*
 * Base settings convention plugin for the use in library modules
 */
plugins {
    id("ru.pixnews.gradle.fbase.buildlogic.settings.repositories")
    id("ru.pixnews.gradle.fbase.buildlogic.settings.gradle-enterprise")
}
