/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.source

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class GoogleServicesJsonFileGeneratorSource @Inject constructor() : FbaseGeneratorSource {
    /**
     * File path for configuration settings to be read from
     *
     * Default: $project/google-services.json
     */
    public abstract val location: RegularFileProperty

    /**
     * Allows you to redefine the application id, which is used to determine the configuration to use
     *
     * Default: not set (the application id of the current Android Variant is used)
     */
    public abstract val applicationId: Property<String>
}
