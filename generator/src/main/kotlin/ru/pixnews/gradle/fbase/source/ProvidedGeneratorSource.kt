/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.source

import org.gradle.api.provider.Property
import ru.pixnews.gradle.fbase.LocalFirebaseOptions
import javax.inject.Inject

public abstract class ProvidedGeneratorSource @Inject constructor() : FbaseGeneratorSource {
    public abstract val source: Property<LocalFirebaseOptions>
}
