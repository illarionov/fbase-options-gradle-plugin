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
import ru.pixnews.gradle.fbase.FbaseOptions
import ru.pixnews.gradle.fbase.internal.GoogleServicesValueSource.Parameters
import ru.pixnews.gradle.fbase.internal.util.capitalized
import ru.pixnews.gradle.fbase.source.GoogleServicesJsonFileGeneratorSource

internal abstract class GoogleServicesValueSource : ValueSource<FbaseOptions, Parameters> {
    override fun obtain(): FbaseOptions? {
        val files = parameters.configurationFiles.filter { it.isFile }
        val applicationId = parameters.applicationId.get()

        if (files.isEmpty) {
            throw GradleException(
                "File $JSON_FILE_NAME is missing. " +
                        "Searched locations: ${parameters.configurationFilenames()}",
            )
        }

        val gsonServiceFiles = files.mapNotNull { it to parseGoogleServicesFile(it) }
        val (projectInfo, clientInfo) = if (applicationId.isNotEmpty()) {
            val filePathServices = gsonServiceFiles.firstOrNull { (_, json) ->
                json.clients.any { it.packageName == applicationId }
            }
            if (filePathServices == null) {
                throw GradleException(
                    "Can not find configuration for Android application with id `$applicationId`." +
                            " Searched locations: ${parameters.configurationFilenames()}",
                )
            }
            val json = filePathServices.second
            json.projectInfo to json.clients.first { it.packageName == applicationId }
        } else {
            val (filePath, json) = gsonServiceFiles.first()
            if (json.clients.size > 1) {
                throw GradleException(
                    "Found configurations for ${json.clients.size} clients in file " +
                            "`${filePath.absolutePath}`, unable to determine which one to use. " +
                            "Required application ID can be specified using the `applicationId` parameter of the " +
                            "`fromGoogleServicesJson(){} block`",
                )
            }
            json.projectInfo to json.clients.first()
        }
        return FbaseOptions(
            projectId = projectInfo.projectId,
            apiKey = clientInfo.googleApiKey,
            applicationId = clientInfo.mobileSdkAppId,
            databaseUrl = projectInfo.firebaseUrl,
            gaTrackingId = clientInfo.trackingId,
            gcmSenderId = projectInfo.projectNumber,
            storageBucket = projectInfo.storageBucket,
        )
    }

    private fun Parameters.configurationFilenames(): String = configurationFiles.joinToString { it.absolutePath }

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
        ): Provider<FbaseOptions> {
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
        ).map(projectDirectory::file)

        internal fun getJsonLocations(buildType: String, flavorNames: List<String>): List<String> {
            val fileLocations: List<String> = if (flavorNames.isEmpty()) {
                listOf(
                    "src/$buildType",
                    "src",
                    "",
                )
            } else {
                val fullFlavorName: String = flavorNames.reduce { fullName, flavorName ->
                    fullName + flavorName.capitalized()
                }
                val fullFlavorNamePaths = listOf(
                    "",
                    "src/$fullFlavorName/$buildType",
                    "src/$buildType/$fullFlavorName",
                    "src/$fullFlavorName",
                    "src/$buildType",
                    "src/$fullFlavorName${buildType.capitalized()}",
                )

                val flavorSubpaths: List<String> = flavorNames.runningReduce { path, flavorName ->
                    "$path/$flavorName"
                }
                val flavorPaths = flavorSubpaths.flatMap { subpath ->
                    listOf(
                        "src/$subpath",
                        "src/$subpath/$buildType",
                        "src/$subpath${buildType.capitalized()}",
                    )
                }
                fullFlavorNamePaths + flavorPaths
            }

            return fileLocations
                .distinct()
                .map { location: String ->
                    if (location.isEmpty()) location + JSON_FILE_NAME else "$location/$JSON_FILE_NAME"
                }
                .sortedByDescending { path -> path.count { it == '/' } }
        }
    }
}
