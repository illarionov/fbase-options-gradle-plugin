/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.internal.util

import java.io.File
import java.util.Locale
import java.util.Properties

internal fun String.capitalized() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
}

internal fun File.toProperties(): Properties = Properties().apply {
    this@toProperties.bufferedReader().use { load(it) }
}
