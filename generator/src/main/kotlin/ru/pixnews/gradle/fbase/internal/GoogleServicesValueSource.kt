/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.internal

import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import ru.pixnews.gradle.fbase.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.internal.GoogleServicesValueSource.Parameters
import ru.pixnews.gradle.fbase.internal.util.capitalized
import ru.pixnews.gradle.fbase.source.GoogleServicesJsonFileGeneratorSource

internal abstract class GoogleServicesValueSource : ValueSource<LocalFirebaseOptions, Parameters> {
    @Suppress("UnusedPrivateProperty")
    override fun obtain(): LocalFirebaseOptions? {
        val files = parameters.configurationFiles.filter { it.isFile }
        val applicationid = parameters.applicationId.get()

        if (files.isEmpty) {
            val searchLocations = parameters.configurationFiles.joinToString { it.absolutePath }
            throw GradleException("File $JSON_FILE_NAME is missing. Searched locations: $searchLocations")
        }

        val options = files.mapNotNull { parseGoogleServicesFile(it, applicationid) }

        return options.first()
    }

    interface Parameters : ValueSourceParameters {
        val configurationFiles: ConfigurableFileCollection
        val applicationId: Property<String>
    }

    internal companion object {
        private const val JSON_FILE_NAME = "google-services.json"

        internal fun createGoogleServicesValueSource(
            source: GoogleServicesJsonFileGeneratorSource,
            providers: ProviderFactory,
            buildType: String,
            productFlavorNames: List<String>,
            projectDirectory: Directory,
            defaultApplicationIdProvider: Provider<String>,
        ): Provider<LocalFirebaseOptions> {
            val defaultFileList = providers.provider {
                getDefaultGoogleServicesLocations(projectDirectory, buildType, productFlavorNames)
            }
            val configFilePathProvider = source.location.map<List<RegularFile>>(::listOf)
                .orElse(defaultFileList)
            val applicationIdProvider = source.applicationId.orElse(defaultApplicationIdProvider)
            return providers.of(GoogleServicesValueSource::class.java) { valueSource ->
                valueSource.parameters {
                    it.configurationFiles.from(configFilePathProvider)
                    it.applicationId.set(applicationIdProvider)
                }
            }
        }

        private fun getDefaultGoogleServicesLocations(
            projectDirectory: Directory,
            buildType: String,
            productFlavorNames: List<String>,
        ): List<RegularFile> = getJsonLocations(
            buildType,
            productFlavorNames,
        ).map<String, RegularFile>(projectDirectory::file)

        private fun getJsonLocations(buildType: String, flavorNames: List<String>): List<String> {
            val flavorsName = flavorNames.reduce { fullName, flavorSuffix -> fullName + flavorSuffix.capitalized() }

            val fileLocations = listOf(
                "",
                "src/$flavorsName/$buildType",
                "src/$buildType/$flavorsName",
                "src/$flavorsName",
                "src/$buildType",
                "src/$flavorsName${buildType.capitalized()}",
            ) + flavorNames.flatMap { flavor ->
                listOf(
                    "src/$flavor",
                    "src/$flavor/$buildType",
                    "src/$flavor${buildType.capitalized()}",
                )
            }

            return fileLocations
                .distinct()
                .sortedByDescending { path -> path.count { it == '/' } }
                .map { location: String ->
                    if (location.isEmpty()) JSON_FILE_NAME else "$location/$JSON_FILE_NAME"
                }
        }
    }
}
