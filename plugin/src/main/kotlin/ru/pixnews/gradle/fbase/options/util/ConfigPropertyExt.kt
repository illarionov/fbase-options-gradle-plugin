/*
 * Copyright (c) 2023, the fbase-options-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.options.util

import org.gradle.api.provider.Property
import org.slf4j.Logger

internal fun <T> Property<T>.getWarnIfNotPresent(
    logger: Logger,
    name: String,
    ifNotPresent: () -> T,
): T {
    return if (isPresent) {
        get()
    } else {
        logger.warn("Configuration file is not installed. Will use the default $name values.")
        ifNotPresent()
    }
}
