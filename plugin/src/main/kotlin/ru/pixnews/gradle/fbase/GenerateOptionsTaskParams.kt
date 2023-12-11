/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import ru.pixnews.gradle.fbase.data.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.data.TargetVisibility

public interface GenerateOptionsTaskParams {
    /**
     * Firebase configuration parameters used to build [FirebaseOptions].
     */
    @get:Input
    val source: Property<LocalFirebaseOptions>

    /**
     * Target package of the generated [FirebaseOptions] instance
     */
    @get:Input
    val targetPackage: Property<String>

    /**
     * Generated оbject class with property
     */
    @get:Input
    val targetObjectName: Property<String>

    /** *
     * Name of the generated property
     */
    @get:Input
    val propertyName: Property<String>

    /**
     * Visibility of the generated [targetProperty]
     */
    @get:Input
    val visibility: Property<TargetVisibility>
}
