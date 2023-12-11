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
import org.gradle.api.provider.ProviderFactory
import ru.pixnews.gradle.fbase.util.VariantDefaults
import ru.pixnews.gradle.fbase.util.VariantDefaults.PluginDefaults.EXTENSION_NAME
import java.util.SortedMap
import java.util.TreeMap

class FbaseConfigGeneratorGradlePlugin : Plugin<Project> {
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
                FirebaseConfigGeneratorExtension::class.java,
                project,
                null,
            )
            androidExtension.registerExtension(
                DslExtension.Builder(EXTENSION_NAME).build(),
                ExtensionMerger(providers, objects, globalExtension),
            )

            androidExtension.onVariants { variant ->
                val variantExtension =
                    checkNotNull(variant.getExtension(FirebaseConfigGeneratorExtension::class.java)) {
                        "Extension not registered"
                    }

                val firebaseOptionsTaskProvider = project.tasks.register(
                    "${variant.name}GenerateFirebaseOptions",
                    GenerateFirebaseOptionsTask::class.java,
                ) { task ->
                    task.group = "Build"
                    task.sourceOutputDir.set(layout.buildDirectory.dir("firebase-options"))
                    task.configs.set(
                        variantExtension.configurations.map { config -> createTaskParams(config) },
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
            options: FirebaseOptionsExtension,
        ): GenerateOptionsTaskParams = objects.newInstance(GenerateOptionsTaskParams::class.java).apply {
            source.set(options.source)
            targetPackage.set(options.targetPackage)
            targetObjectName.set(options.targetObjectName)
            propertyName.set(options.propertyName)
            visibility.set(options.visibility)
        }

        /**
         * Adds google_app_id [ResValue] needed for Firebase Analytics
         */
        private fun addGoogleAppIdResource(
            variant: Variant,
            configurations: NamedDomainObjectContainer<FirebaseOptionsExtension>,
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
        private val providers: ProviderFactory,
        private val objects: ObjectFactory,
        private val globalExtension: FirebaseConfigGeneratorExtension,
    ) : (VariantExtensionConfig<out Variant>) -> VariantExtension {
        override fun invoke(
            variantExtensionConfig: VariantExtensionConfig<out Variant>,
        ): FirebaseConfigGeneratorExtension {
            val mergedConfigs: SortedMap<String, FirebaseOptionsExtension> = TreeMap()

            globalExtension.configurations.forEach { item ->
                val defaults = firebaseOptionsExtensionDefaults(item.name, variantExtensionConfig)
                mergedConfigs[item.name] = mergeExtensions(item, defaults)
            }

            val mergedExtension = objects.newInstance(
                FirebaseConfigGeneratorExtension::class.java,
                variantExtensionConfig,
            ).apply {
                configurations.addAll(mergedConfigs.values)
            }

            return mergedExtension
        }

        private fun mergeExtensions(
            high: FirebaseOptionsExtension,
            low: FirebaseOptionsExtension,
        ): FirebaseOptionsExtension = objects.newInstance(
            FirebaseOptionsExtension::class.java,
            high.name,
        ).apply {
            source.set(high.source.orElse(low.source))
            targetPackage.set(high.targetPackage.orElse(low.targetPackage))
            targetObjectName.set(high.targetObjectName.orElse(low.targetObjectName))
            propertyName.set(high.propertyName.orElse(low.propertyName))
            visibility.set(high.visibility.orElse(low.visibility))
        }

        private fun firebaseOptionsExtensionDefaults(
            defaultPropertyName: String,
            variantExtensionConfig: VariantExtensionConfig<out Variant>,
        ): FirebaseOptionsExtension {
            val variantDefaults = VariantDefaults(providers, variantExtensionConfig.variant)
            return objects.newInstance(FirebaseOptionsExtension::class.java, defaultPropertyName).apply {
                targetPackage.set(variantDefaults.targetPackage)
                targetObjectName.set(VariantDefaults.DEFAULT_TARGET_OBJECT_NAME)
                propertyName.set(defaultPropertyName)
                visibility.set(VariantDefaults.DEFAULT_VISIBILITY)
            }
        }
    }
}
