/*
 * Copyright (c) 2023, the fbase-options-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.options

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.DslExtension
import com.android.build.api.variant.ResValue
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantExtension
import com.android.build.api.variant.VariantExtensionConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import ru.pixnews.gradle.fbase.options.data.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.options.util.VariantDefaults
import ru.pixnews.gradle.fbase.options.util.VariantDefaults.PluginDefaults.DEFAULT_PROPERTY_NAME
import ru.pixnews.gradle.fbase.options.util.VariantDefaults.PluginDefaults.DEFAULT_TARGET_OBJECT_NAME
import ru.pixnews.gradle.fbase.options.util.VariantDefaults.PluginDefaults.DEFAULT_VISIBILITY
import ru.pixnews.gradle.fbase.options.util.VariantDefaults.PluginDefaults.EXTENSION_NAME
import ru.pixnews.gradle.fbase.options.util.withAnyOfAndroidPlugins

class FbaseOptionsGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.withAnyOfAndroidPlugins { _, androidComponentsExtension ->
            with(androidComponentsExtension) {
                registerFirebaseOptionsTask(project)
            }
        }
    }

    private fun AndroidComponentsExtension<*, *, *>.registerFirebaseOptionsTask(
        project: Project,
    ) {
        val globalExtension = project.extensions.create(EXTENSION_NAME, FirebaseOptionsExtension::class.java)
        registerExtension(
            DslExtension.Builder(EXTENSION_NAME).build(),
            ExtensionMerger(project.providers, project.objects, globalExtension),
        )

        onVariants { variant ->
            val variantExtension = checkNotNull(variant.getExtension(FirebaseOptionsExtension::class.java)) {
                "Extension not registered"
            }

            @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
            val firebaseOptionsTaskProvider = project.tasks.register(
                "${variant.name}GenerateFirebaseOptions",
                GenerateFirebaseOptionsTask::class.java,
            ) {
                it.group = "Build"
                it.firebaseConfig.set(variantExtension.source)
                it.outputObjectPackage.set(variantExtension.targetPackage)
                it.outputObjectName.set(variantExtension.targetObjectName)
                it.outputPropertyName.set(variantExtension.propertyName)
                it.sourceOutputDir.set(project.layout.buildDirectory.dir("firebase-options"))
            }

            variant.sources.java?.addGeneratedSourceDirectory(
                taskProvider = firebaseOptionsTaskProvider,
                wiredWith = GenerateFirebaseOptionsTask::sourceOutputDir,
            )

            addGoogleAppIdResource(variant, variantExtension.source)
        }
    }

    /**
     * Adds google_app_id [ResValue] needed for Firebase Analytics
     */
    private fun addGoogleAppIdResource(
        variant: Variant,
        firebaseOptionsProvider: Provider<LocalFirebaseOptions>,
    ) {
        val googleAppIdKey = variant.makeResValueKey("string", "google_app_id")
        variant.resValues.putAll(
            firebaseOptionsProvider
                .map(ApplicationIdToMapOfValuesTransformer(googleAppIdKey))
                .orElse(emptyMap()),
        )
    }

    private class ApplicationIdToMapOfValuesTransformer(
        private val googleApiKey: ResValue.Key,
    ) : Transformer<Map<ResValue.Key, ResValue>, LocalFirebaseOptions> {
        override fun transform(options: LocalFirebaseOptions): Map<ResValue.Key, ResValue> {
            return options.applicationId?.let {
                mapOf(googleApiKey to ResValue(it))
            } ?: emptyMap()
        }
    }

    private class ExtensionMerger(
        val providers: ProviderFactory,
        val objects: ObjectFactory,
        val globalExtension: FirebaseOptionsExtension,
    ) : (VariantExtensionConfig<out Variant>) -> VariantExtension {
        override fun invoke(
            variantExtensionConfig: VariantExtensionConfig<out Variant>,
        ): FirebaseOptionsExtension {
            val variantDefaults = VariantDefaults(providers, variantExtensionConfig.variant)
            return objects.newInstance(FirebaseOptionsExtension::class.java, variantExtensionConfig).apply {
                source.set(
                    globalExtension.source.orElse(this.providers.propertiesFile()),
                )
                targetPackage.set(
                    globalExtension.targetPackage.orElse(variantDefaults.targetPackage),
                )
                targetObjectName.set(
                    globalExtension.targetObjectName.orElse(DEFAULT_TARGET_OBJECT_NAME),
                )
                propertyName.set(
                    globalExtension.propertyName.orElse(DEFAULT_PROPERTY_NAME),
                )
                visibility.set(
                    globalExtension.visibility.orElse(DEFAULT_VISIBILITY),
                )
            }
        }
    }
}
