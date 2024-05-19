/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.util

import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import org.gradle.api.file.RegularFileProperty
import ru.pixnews.gradle.fbase.source.PropertiesFileGeneratorSource

fun Assert<PropertiesFileGeneratorSource>.location(): Assert<RegularFileProperty> =
    prop(PropertiesFileGeneratorSource::location)

fun Assert<PropertiesFileGeneratorSource>.applicationId() =
    prop(PropertiesFileGeneratorSource::applicationId)

fun Assert<PropertiesFileGeneratorSource>.hasFileName(name: String) = location()
    .transform { it.get().asFile.name }
    .isEqualTo(name)
