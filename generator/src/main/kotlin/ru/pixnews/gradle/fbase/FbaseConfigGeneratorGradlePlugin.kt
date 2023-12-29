/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.DslExtension
import com.android.build.api.variant.ResValue
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantExtension
import com.android.build.api.variant.VariantExtensionConfig
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.InvalidUserDataException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import ru.pixnews.gradle.fbase.internal.GoogleServicesValueSource.Companion.createGoogleServicesValueSource
import ru.pixnews.gradle.fbase.internal.LocalFirebaseOptionsValueSource
import ru.pixnews.gradle.fbase.internal.VariantDefaults
import ru.pixnews.gradle.fbase.internal.VariantDefaults.PluginDefaults.EXTENSION_NAME
import ru.pixnews.gradle.fbase.source.FbaseGeneratorSource
import ru.pixnews.gradle.fbase.source.GoogleServicesJsonFileGeneratorSource
import ru.pixnews.gradle.fbase.source.PropertiesFileGeneratorSource
import ru.pixnews.gradle.fbase.source.ProvidedGeneratorSource
import java.util.SortedMap
import java.util.TreeMap

public class FbaseConfigGeneratorGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        var configured = false
        project.plugins.withType(AndroidBasePlugin::class.java) { _ ->
            configured = true
            val componentsExtension = project.extensions.findByType(AndroidComponentsExtension::class.java)
            checkNotNull(componentsExtension) {
                "Could not find the Android Gradle Plugin (AGP) extension, the Fbase Config Generator Gradle plugin " +
                        "should be only applied to an Android projects."
            }
            @Suppress("MagicNumber")
            check(componentsExtension.pluginVersion >= AndroidPluginVersion(7, 3)) {
                "Fbase Config Generator Gradle plugin is only compatible with Android Gradle plugin (AGP) " +
                        "version 7.3.0 or higher (found ${componentsExtension.pluginVersion})."
            }
            PluginConfigurator(project, componentsExtension).configure()
        }
        project.afterEvaluate {
            check(configured) {
                "Fbase Config Generator Gradle plugin can only be applied to an Android project."
            }
        }
    }

    private class PluginConfigurator(
        private val project: Project,
        private val androidExtension: AndroidComponentsExtension<*, *, *>,
    ) {
        private val layout = project.layout
        private val objects = project.objects
        private val providers = project.providers

        fun configure() {
            val globalExtension = project.extensions.create(
                EXTENSION_NAME,
                FbaseGeneratorExtension::class.java,
            )
            androidExtension.registerExtension(
                DslExtension.Builder(EXTENSION_NAME)
                    .extendBuildTypeWith(FbaseGeneratorExtension::class.java)
                    .extendProductFlavorWith(FbaseGeneratorExtension::class.java)
                    .build(),
                ExtensionMerger(objects, globalExtension),
            )

            androidExtension.onVariants { variant ->
                val variantExtension = checkNotNull(variant.getExtension(FbaseGeneratorExtension::class.java)) {
                    "Extension not registered"
                }

                val firebaseOptionsTaskProvider = project.tasks.register(
                    "${variant.name}GenerateFirebaseOptions",
                    GenerateFirebaseOptionsTask::class.java,
                ) { task ->
                    task.description = "Generates FirebaseOptions.Builder's for the `${variant.name}` variant"
                    task.sourceOutputDir.set(layout.buildDirectory.dir("firebase-options"))
                    task.configs.set(
                        variantExtension.configurations.map { config ->
                            createTaskParams(config, variant)
                        },
                    )
                }

                variant.sources.java?.addGeneratedSourceDirectory(
                    taskProvider = firebaseOptionsTaskProvider,
                    wiredWith = GenerateFirebaseOptionsTask::sourceOutputDir,
                )

                addGoogleAppIdResource(
                    variant,
                    variantExtension.configurations,
                    variantExtension.primaryConfiguration,
                    variantExtension.addGoogleAppIdResource,
                )
            }
        }

        private fun createTaskParams(
            builderExtension: FbaseBuilderExtension,
            variant: Variant,
        ): GenerateOptionsTaskParams {
            val defaultPropertyName = builderExtension.name
            val defaults = VariantDefaults(objects, providers, variant)
            val sourceTransformer = FbaseGeneratorSourceTransformer(project, variant)
            return objects.newInstance(GenerateOptionsTaskParams::class.java).apply {
                source.set(
                    builderExtension.source
                        .orElse(defaults.defaultSource)
                        .flatMap(sourceTransformer),
                )
                targetPackage.set(
                    builderExtension.targetPackage.orElse(defaults.targetPackage),
                )
                targetFileName.set(
                    builderExtension.targetFileName.orElse(defaults.targetFileName(defaultPropertyName)),
                )
                propertyName.set(
                    builderExtension.propertyName.orElse(defaultPropertyName),
                )
                visibility.set(
                    builderExtension.visibility.orElse(VariantDefaults.DEFAULT_VISIBILITY),
                )
            }
        }

        /**
         * Adds google_app_id [ResValue] needed for Firebase Analytics
         */
        private fun addGoogleAppIdResource(
            variant: Variant,
            configurations: NamedDomainObjectContainer<FbaseBuilderExtension>,
            primaryConfiguration: Provider<String>,
            addGoogleAppIdResource: Provider<Boolean>,
        ) {
            val googleAppIdKey = variant.makeResValueKey("string", "google_app_id")
            val sourceTransformer = FbaseGeneratorSourceTransformer(project, variant)
            val configurationDefaultSource = VariantDefaults(objects, providers, variant).defaultSource
            val resourcesMapProvider = providers.provider {
                if (addGoogleAppIdResource.getOrElse(true) == false) {
                    return@provider emptyMap<ResValue.Key, ResValue>()
                }
                if (configurations.size == 0) {
                    return@provider emptyMap<ResValue.Key, ResValue>()
                }

                val configuration = if (configurations.size == 1) {
                    configurations.first()
                } else {
                    @Suppress("MaxLineLength")
                    val primaryConfigurationName: String =
                        primaryConfiguration.orNull ?: throw InvalidUserDataException(
                            "FbaseGeneratorExtension.primaryConfiguration must be set when using multiple configurations",
                        )
                    configurations.findByName(primaryConfigurationName) ?: throw InvalidUserDataException(
                        "Configuration named `$primaryConfigurationName` is not defined",
                    )
                }

                val applicationId = configuration.source
                    .orElse(configurationDefaultSource)
                    .flatMap(sourceTransformer)
                    .orNull
                    ?.applicationId
                return@provider if (applicationId != null) {
                    mapOf(googleAppIdKey to ResValue(applicationId))
                } else {
                    emptyMap()
                }
            }
            variant.resValues.putAll(resourcesMapProvider)
        }
    }

    internal class FbaseGeneratorSourceTransformer(
        project: Project,
        variant: Variant,
    ) : Transformer<Provider<LocalFirebaseOptions>, FbaseGeneratorSource> {
        private val providers = project.providers
        private val defaultConfigFile = project.rootProject.layout.projectDirectory.file(
            VariantDefaults.DEFAULT_CONFIG_FILE_PATH,
        )
        private val projectDirectory = project.layout.projectDirectory
        private val defaultApplicationIdProvider: Provider<String> = if (variant is ApplicationVariant) {
            variant.applicationId
        } else {
            providers.provider { "" }
        }
        private val buildType = variant.buildType.orEmpty()
        private val productFlavorNames = variant.productFlavors.map(Pair<String, String>::second)

        override fun transform(source: FbaseGeneratorSource): Provider<LocalFirebaseOptions> {
            return when (source) {
                is ProvidedGeneratorSource -> source.source

                is PropertiesFileGeneratorSource -> {
                    val configFilePathProvider = source.location.orElse(defaultConfigFile)
                    val applicationIdProvider = source.applicationId.orElse(defaultApplicationIdProvider)

                    providers.of(LocalFirebaseOptionsValueSource::class.java) { valueSource ->
                        valueSource.parameters {
                            it.configFilePath.set(configFilePathProvider)
                            it.applicationId.set(applicationIdProvider)
                        }
                    }
                }

                is GoogleServicesJsonFileGeneratorSource -> createGoogleServicesValueSource(
                    source = source,
                    providers = providers,
                    buildType = buildType,
                    productFlavorNames = productFlavorNames,
                    projectDirectory = projectDirectory,
                    defaultApplicationIdProvider = defaultApplicationIdProvider,
                )

                else -> error("Unexpected source")
            }
        }
    }

    internal class ExtensionMerger(
        private val objects: ObjectFactory,
        private val globalExtension: FbaseGeneratorExtension,
    ) : (VariantExtensionConfig<out Variant>) -> VariantExtension {
        override fun invoke(
            variantExtensions: VariantExtensionConfig<out Variant>,
        ): FbaseGeneratorExtension {
            val configs: SortedMap<String, FbaseBuilderExtension> = TreeMap()
            var addGoogleAppId: Provider<Boolean> = globalExtension.addGoogleAppIdResource
            var primaryConfiguration: Provider<String> = globalExtension.primaryConfiguration

            globalExtension.configurations.forEach { item -> configs[item.name] = item }

            val extensions = listOf(variantExtensions.buildTypeExtension(FbaseGeneratorExtension::class.java)) +
                    variantExtensions.productFlavorsExtensions(FbaseGeneratorExtension::class.java).reversed()

            extensions.forEach { extension ->
                addGoogleAppId = extension.addGoogleAppIdResource.orElse(addGoogleAppId)
                primaryConfiguration = extension.primaryConfiguration.orElse(primaryConfiguration)
                configs.addConfigurations(extension.configurations)
            }

            val mergedExtension = objects.newInstance(FbaseGeneratorExtension::class.java).apply {
                this.addGoogleAppIdResource.set(addGoogleAppId)
                this.primaryConfiguration.set(primaryConfiguration)
                this.configurations.addAll(configs.values)
            }

            return mergedExtension
        }

        private fun MutableMap<String, FbaseBuilderExtension>.addConfigurations(
            configs: NamedDomainObjectContainer<FbaseBuilderExtension>,
        ) {
            configs.forEach { highPrioConfig ->
                val lowPrioConfig = this[highPrioConfig.name]
                this[highPrioConfig.name] = if (lowPrioConfig != null) {
                    mergeExtensions(highPrioConfig, lowPrioConfig)
                } else {
                    highPrioConfig
                }
            }
        }

        private fun mergeExtensions(
            high: FbaseBuilderExtension,
            low: FbaseBuilderExtension,
        ): FbaseBuilderExtension = objects.newInstance(
            FbaseBuilderExtension::class.java,
            high.name,
        ).apply {
            source.set(high.source.orElse(low.source))
            targetPackage.set(high.targetPackage.orElse(low.targetPackage))
            targetFileName.set(high.targetFileName.orElse(low.targetFileName))
            propertyName.set(high.propertyName.orElse(low.propertyName))
            visibility.set(high.visibility.orElse(low.visibility))
        }
    }
}
