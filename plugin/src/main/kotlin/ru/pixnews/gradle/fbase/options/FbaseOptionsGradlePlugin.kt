/*
 * Copyright (c) 2023, the fbase-options-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.options

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.DslExtension
import com.android.build.api.variant.ResValue
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantExtension
import com.android.build.api.variant.VariantExtensionConfig
import com.android.build.gradle.api.AndroidBasePlugin
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

class FbaseOptionsGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        var configured = false
        project.plugins.withType(AndroidBasePlugin::class.java) { plugin ->
            configured = true
            val componentsExtension = project.extensions.findByType(AndroidComponentsExtension::class.java)
            checkNotNull(componentsExtension) {
                "Could not find the Android Gradle Plugin (AGP) extension, the Fbase Options Gradle plugin " +
                        "should be only applied to an Android projects."
            }
            @Suppress("MagicNumber")
            check(componentsExtension.pluginVersion >= AndroidPluginVersion(7, 3)) {
                "Fbase Options Gradle plugin is only compatible with Android Gradle plugin (AGP) " +
                        "version 7.3.0 or higher (found ${componentsExtension.pluginVersion})."
            }
            componentsExtension.configurePlugin(project)
        }
        project.afterEvaluate {
            check(configured) {
                "Fbase Options Gradle plugin can only be applied to an Android project."
            }
        }
    }

    private fun AndroidComponentsExtension<*, *, *>.configurePlugin(
        project: Project,
    ) {
        val globalExtension = project.extensions.create(
            EXTENSION_NAME,
            FirebaseOptionsExtension::class.java,
            project,
            null,
        )
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
                it.targetVisibility.set(variantExtension.visibility)
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
                    globalExtension.source.orElse(this.providers.propertiesFileProvider()),
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
