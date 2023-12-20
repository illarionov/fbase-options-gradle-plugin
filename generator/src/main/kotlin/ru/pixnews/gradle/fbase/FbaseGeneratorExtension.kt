/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import com.android.build.api.variant.VariantExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import java.io.Serializable
import javax.inject.Inject

public abstract class FbaseGeneratorExtension @Inject internal constructor() : Serializable, VariantExtension {
    /**
     * Should the google_app_id string parameter be added to Android resources.
     * Enabled by default.
     * Android string resource "google_app_id" will be initialized with the value from the configuration specified
     * by [primaryConfiguration].
     */
    public abstract val addGoogleAppIdResource: Property<Boolean>

    /**
     * Name of the configuration from the [configurations] that will be used to fill "google_app_id" string resource.
     *
     * Required when using 2 or more configurations.
     */
    public abstract val primaryConfiguration: Property<String>
    public abstract val configurations: NamedDomainObjectContainer<FbaseBuilderExtension>

    public companion object {
        private const val serialVersionUID: Long = -1
    }
}
