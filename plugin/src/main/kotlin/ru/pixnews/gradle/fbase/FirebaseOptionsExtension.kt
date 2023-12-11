/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import org.gradle.api.Named
import org.gradle.api.provider.Property
import ru.pixnews.gradle.fbase.data.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.data.TargetVisibility
import java.io.Serializable
import javax.inject.Inject

abstract class FirebaseOptionsExtension @Inject constructor(
    private val name: String,
) : Named, Serializable {
    /**
     * Firebase configuration parameters used to build [FirebaseOptions].
     */
    abstract val source: Property<LocalFirebaseOptions>

    /**
     * Target package of the generated [FirebaseOptions] instance
     */
    abstract val targetPackage: Property<String>

    /**
     * Generated оbject class with property
     */
    abstract val targetObjectName: Property<String>

    /** *
     * Name of the generated property
     */
    abstract val propertyName: Property<String>

    /**
     * Visibility of the generated [targetProperty]
     */
    abstract val visibility: Property<TargetVisibility>

    override fun getName(): String = name

    companion object {
        private const val serialVersionUID: Long = -1
    }
}
