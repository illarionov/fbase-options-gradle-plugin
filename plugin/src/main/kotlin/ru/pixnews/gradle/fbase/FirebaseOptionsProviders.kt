/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import ru.pixnews.gradle.fbase.internal.LocalFirebaseOptionsValueSource
import ru.pixnews.gradle.fbase.internal.VariantDefaults.PluginDefaults.DEFAULT_CONFIG_FILE_PATH

public class FirebaseOptionsProviders internal constructor(
    project: Project,
    private val defaultApplicationIdProvider: Provider<String>,
) {
    private val providers: ProviderFactory = project.providers
    private val defaultConfigFile = project.rootProject.layout.projectDirectory.file(DEFAULT_CONFIG_FILE_PATH)

    public fun propertiesFile(
        configFilePath: RegularFile = defaultConfigFile,
        applicationIdProvider: Provider<String> = defaultApplicationIdProvider,
    ): Provider<LocalFirebaseOptions> = propertiesFileProvider(
        configFilePathProvider = providers.provider { configFilePath },
        applicationIdProvider = applicationIdProvider,
    )

    public fun propertiesFileProvider(
        configFilePathProvider: Provider<RegularFile> = providers.provider { defaultConfigFile },
        applicationIdProvider: Provider<String> = defaultApplicationIdProvider,
    ): Provider<LocalFirebaseOptions> = providers.of(LocalFirebaseOptionsValueSource::class.java) { valueSource ->
        valueSource.parameters {
            it.applicationId.set(applicationIdProvider)
            it.configFilePath.set(configFilePathProvider)
        }
    }
}
