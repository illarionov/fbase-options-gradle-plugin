/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import org.gradle.api.Named
import org.gradle.api.provider.Property
import java.io.Serializable
import javax.inject.Inject

public abstract class FirebaseConfigInstanceExtension @Inject constructor(
    private val name: String,
) : Named, Serializable {
    /**
     * Firebase configuration parameters used to build [FirebaseOptions].
     */
    public abstract val source: Property<LocalFirebaseOptions>

    /**
     * Target package of the generated [FirebaseOptions] instance
     */
    public abstract val targetPackage: Property<String>

    /**
     * Generated Kotlin file with property
     */
    public abstract val targetFileName: Property<String>

    /** *
     * Name of the generated property
     */
    public abstract val propertyName: Property<String>

    /**
     * Visibility of the generated [targetProperty]
     */
    public abstract val visibility: Property<TargetVisibility>

    override fun getName(): String = name

    public companion object {
        private const val serialVersionUID: Long = -1
    }
}
