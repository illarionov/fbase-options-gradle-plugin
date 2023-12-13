/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.DslExtension
import com.android.build.api.variant.ResValue
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantExtension
import com.android.build.api.variant.VariantExtensionConfig
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import ru.pixnews.gradle.fbase.internal.VariantDefaults
import ru.pixnews.gradle.fbase.internal.VariantDefaults.PluginDefaults.EXTENSION_NAME
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
                project,
                null,
            )
            androidExtension.registerExtension(
                DslExtension.Builder(EXTENSION_NAME)
                    .extendBuildTypeWith(FbaseGeneratorFlavorExtension::class.java)
                    .extendProductFlavorWith(FbaseGeneratorFlavorExtension::class.java)
                    .build(),
                ExtensionMerger(objects, globalExtension),
            )

            androidExtension.onVariants { variant ->
                val variantExtension =
                    checkNotNull(variant.getExtension(FbaseGeneratorExtension::class.java)) {
                        "Extension not registered"
                    }

                val firebaseOptionsTaskProvider = project.tasks.register(
                    "${variant.name}GenerateFirebaseOptions",
                    GenerateFirebaseOptionsTask::class.java,
                ) { task ->
                    task.group = "Build"
                    task.sourceOutputDir.set(layout.buildDirectory.dir("firebase-options"))
                    task.configs.set(
                        variantExtension.configurations.map { config -> createTaskParams(config, variant) },
                    )
                }

                variant.sources.java?.addGeneratedSourceDirectory(
                    taskProvider = firebaseOptionsTaskProvider,
                    wiredWith = GenerateFirebaseOptionsTask::sourceOutputDir,
                )

                addGoogleAppIdResource(
                    variant,
                    variantExtension.configurations,
                    variantExtension.addGoogleAppIdResource,
                )
            }
        }

        private fun createTaskParams(
            options: FbaseBuilderExtension,
            variant: Variant,
        ): GenerateOptionsTaskParams {
            val defaultPropertyName = options.name
            val defaults = VariantDefaults(providers, variant)
            return objects.newInstance(GenerateOptionsTaskParams::class.java).apply {
                source.set(options.source)
                targetPackage.set(
                    options.targetPackage.orElse(defaults.targetPackage),
                )
                targetFileName.set(
                    options.targetFileName.orElse(defaults.targetFileName(defaultPropertyName)),
                )
                propertyName.set(
                    options.propertyName.orElse(defaultPropertyName),
                )
                visibility.set(
                    options.visibility.orElse(VariantDefaults.DEFAULT_VISIBILITY),
                )
            }
        }

        /**
         * Adds google_app_id [ResValue] needed for Firebase Analytics
         */
        private fun addGoogleAppIdResource(
            variant: Variant,
            configurations: NamedDomainObjectContainer<FbaseBuilderExtension>,
            addGoogleAppIdResource: Provider<Boolean>,
        ) {
            val googleAppIdKey = variant.makeResValueKey("string", "google_app_id")
            providers.provider {
                if (addGoogleAppIdResource.get() == false) {
                    emptyMap<ResValue.Key, ResValue>()
                }
                val configuration = configurations.firstOrNull()
                if (configuration == null) {
                    emptyMap<ResValue.Key, ResValue>()
                }
                val applicationId = configuration?.source?.get()?.applicationId
                if (applicationId != null) {
                    mapOf(googleAppIdKey to ResValue(applicationId))
                } else {
                    emptyMap()
                }
            }
        }
    }

    private class ExtensionMerger(
        private val objects: ObjectFactory,
        private val globalExtension: FbaseGeneratorExtension,
    ) : (VariantExtensionConfig<out Variant>) -> VariantExtension {
        override fun invoke(
            variantExtensionConfig: VariantExtensionConfig<out Variant>,
        ): FbaseGeneratorExtension {
            val mergedConfigs: SortedMap<String, FbaseBuilderExtension> = TreeMap()
            var mergedAddGoogleAppId: Provider<Boolean> = globalExtension.addGoogleAppIdResource

            globalExtension.configurations.forEach { item -> mergedConfigs[item.name] = item }

            val buildTypeExtension = variantExtensionConfig.buildTypeExtension(
                FbaseGeneratorFlavorExtension::class.java,
            )
            mergedConfigs.addConfigurations(buildTypeExtension.configurations)
            mergedAddGoogleAppId = buildTypeExtension.addGoogleAppIdResource
                .orElse(mergedAddGoogleAppId)

            val flavorExtensions = variantExtensionConfig.productFlavorsExtensions(
                FbaseGeneratorFlavorExtension::class.java,
            )
            flavorExtensions.forEach { extension ->
                mergedConfigs.addConfigurations(extension.configurations)
                mergedAddGoogleAppId = extension.addGoogleAppIdResource.orElse(mergedAddGoogleAppId)
            }

            val mergedExtension = objects.newInstance(
                FbaseGeneratorExtension::class.java,
                variantExtensionConfig,
            ).apply {
                this.addGoogleAppIdResource.set(mergedAddGoogleAppId)
                this.configurations.addAll(mergedConfigs.values)
            }

            return mergedExtension
        }

        private fun MutableMap<String, FbaseBuilderExtension>.addConfigurations(
            configs: NamedDomainObjectContainer<FbaseBuilderExtension>,
        ) {
            configs.forEach { item ->
                val lowPrio = this[item.name]
                this[item.name] = if (lowPrio != null) {
                    mergeExtensions(item, lowPrio)
                } else {
                    item
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
