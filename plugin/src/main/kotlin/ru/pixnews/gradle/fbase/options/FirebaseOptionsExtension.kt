/*
 * Copyright (c) 2023, the fbase-options-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.options

import com.android.build.api.variant.VariantExtension
import com.android.build.api.variant.VariantExtensionConfig
import org.gradle.api.Project
import org.gradle.api.provider.Property
import ru.pixnews.gradle.fbase.options.data.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.options.util.VariantDefaults
import java.io.Serializable
import javax.inject.Inject

abstract class FirebaseOptionsExtension @Inject constructor(
    project: Project,
    extensionConfig: VariantExtensionConfig<*>?,
) : VariantExtension, Serializable {
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

    @Transient
    val providers: FirebaseOptionsProviders

    init {
        val variantDefaults = extensionConfig?.variant?.let { VariantDefaults(project.providers, it) }
        val defaultApplicationIdProvider = if (variantDefaults != null) {
            variantDefaults.applicationId
        } else {
            project.providers.provider { "" }
        }

        providers = project.objects.newInstance(
            FirebaseOptionsProviders::class.java,
            project,
            defaultApplicationIdProvider,
        )
    }

    companion object {
        private const val serialVersionUID: Long = -1
    }
}
