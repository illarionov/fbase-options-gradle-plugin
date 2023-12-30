/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.internal

import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import ru.pixnews.gradle.fbase.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.internal.LocalFirebaseOptionsValueSource.Parameters
import ru.pixnews.gradle.fbase.internal.util.toProperties
import ru.pixnews.gradle.fbase.source.PropertiesFileGeneratorSource

internal abstract class LocalFirebaseOptionsValueSource : ValueSource<LocalFirebaseOptions, Parameters> {
    override fun obtain(): LocalFirebaseOptions? {
        val configProperties = parameters.configFilePath.get().asFile.toProperties()
        return FirebaseConfigReader(
            configProperties,
            parameters.applicationId.get().ifEmpty { null },
        ).read()
    }

    interface Parameters : ValueSourceParameters {
        val applicationId: Property<String>
        val configFilePath: RegularFileProperty
    }

    internal companion object {
        internal fun createPropertiesFileGeneratorSource(
            source: PropertiesFileGeneratorSource,
            providers: ProviderFactory,
            rootProjectDirectory: Directory,
            defaultApplicationIdProvider: Provider<String>,
        ): Provider<LocalFirebaseOptions> {
            val defaultConfigFile = rootProjectDirectory.file(VariantDefaults.DEFAULT_CONFIG_FILE_PATH)
            val configFilePathProvider = source.location.orElse(defaultConfigFile)
            val applicationIdProvider = source.applicationId.orElse(defaultApplicationIdProvider)

            return providers.of(LocalFirebaseOptionsValueSource::class.java) { valueSource ->
                valueSource.parameters {
                    it.configFilePath.set(configFilePathProvider)
                    it.applicationId.set(applicationIdProvider)
                }
            }
        }
    }
}
