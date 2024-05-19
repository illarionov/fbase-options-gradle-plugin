/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.internal

import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.Variant
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import ru.pixnews.gradle.fbase.TargetVisibility
import ru.pixnews.gradle.fbase.source.FbaseGeneratorSource
import ru.pixnews.gradle.fbase.source.GoogleServicesJsonFileGeneratorSource

internal class VariantDefaults(
    val objects: ObjectFactory,
    val providers: ProviderFactory,
    val variant: Variant,
) {
    val applicationId: Provider<String>
        get() = if (variant is ApplicationVariant) {
            variant.applicationId
        } else {
            providers.provider { "" }
        }

    val targetPackage: Provider<String>
        get() = variant.namespace

    val defaultSource: FbaseGeneratorSource
        get() = objects.newInstance(GoogleServicesJsonFileGeneratorSource::class.java)

    fun targetFileName(propertyName: String): String = propertyName.replaceFirstChar(Char::titlecase)

    internal companion object PluginDefaults {
        internal const val EXTENSION_NAME = "firebaseConfig"
        internal const val DEFAULT_CONFIG_FILE_PATH = "config/firebase.properties"
        internal val DEFAULT_VISIBILITY = TargetVisibility.INTERNAL
    }
}
