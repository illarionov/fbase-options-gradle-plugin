/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class FbaseGeneratorFlavorExtension @Inject internal constructor() {
    /**
     * Should the google_app_id string parameter be added to Android resources.
     * Enabled by default.
     * Android string resource "google_app_id" will be initialized with the value from the first configuration defined.
     */
    public abstract val addGoogleAppIdResource: Property<Boolean>
    public abstract val configurations: NamedDomainObjectContainer<FbaseBuilderExtension>
}
