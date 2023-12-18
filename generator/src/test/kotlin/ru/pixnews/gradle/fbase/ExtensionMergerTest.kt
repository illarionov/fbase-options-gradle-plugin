/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import assertk.all
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantExtensionConfig
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import ru.pixnews.gradle.fbase.FbaseConfigGeneratorGradlePlugin.ExtensionMerger
import ru.pixnews.gradle.fbase.TargetVisibility.INTERNAL
import ru.pixnews.gradle.fbase.TargetVisibility.PUBLIC
import ru.pixnews.gradle.fbase.source.PropertiesFileGeneratorSource
import ru.pixnews.gradle.fbase.util.applicationId
import ru.pixnews.gradle.fbase.util.hasFileName
import ru.pixnews.gradle.fbase.util.isNotPresent
import ru.pixnews.gradle.fbase.util.nameProp
import ru.pixnews.gradle.fbase.util.propertyName
import ru.pixnews.gradle.fbase.util.targetFileName
import ru.pixnews.gradle.fbase.util.targetPackage
import ru.pixnews.gradle.fbase.util.value
import ru.pixnews.gradle.fbase.util.visibility

class ExtensionMergerTest {
    val project: Project = ProjectBuilder.builder().build()
    val objects: ObjectFactory = project.objects
    val globalExtension: FbaseGeneratorExtension = createExtension()

    @Test
    fun `should be empty when all extensions are empty`() {
        val globalExtension = createExtension()
        val buildTypeExtension = createExtension()
        val productFlavorExtensions: List<FbaseGeneratorExtension> = emptyList()
        val variant: ApplicationVariant = mockk()
        val variantExtensionConfig = MockVariantExtensionConfig(
            variant = variant,
            buildTypeExtension = buildTypeExtension,
            productFlavorsExtensions = productFlavorExtensions,
        )

        val result = ExtensionMerger(objects, globalExtension).invoke(variantExtensionConfig)

        assertThat(result.addGoogleAppIdResource).isNotPresent()
        assertThat(result.configurations.asMap).isEmpty()
    }

    @Test
    fun `should use globalExtension when variant extensions are empty`() {
        globalExtension.apply {
            addGoogleAppIdResource.set(true)
            createConfiguration(
                name = "global",
                propertiesFileName = "testfile",
            )
        }
        val variantExtensionConfig = MockVariantExtensionConfig(
            variant = mockk<ApplicationVariant>(),
            buildTypeExtension = createExtension(),
            productFlavorsExtensions = emptyList(),
        )

        val result = ExtensionMerger(objects, globalExtension).invoke(variantExtensionConfig)

        assertThat(result.addGoogleAppIdResource).value().isTrue()
        assertThat(result.configurations.asMap.keys).containsOnly("global")
        assertThat(result.configurations.getByName("global")).all {
            transform { it.source.get() }.isInstanceOf<PropertiesFileGeneratorSource>().all {
                hasFileName("testfile")
                applicationId().isNotPresent()
            }
            targetPackage().isNotPresent()
            targetFileName().isNotPresent()
            propertyName().isNotPresent()
            visibility().isNotPresent()
            nameProp().isEqualTo("global")
        }
    }

    @Suppress("LongMethod")
    @Test
    fun `should merge globalExtension and build type extension`() {
        globalExtension.apply {
            addGoogleAppIdResource.set(true)
            createConfiguration(
                name = "global",
                propertiesFileName = "testfile",
            )
            createConfiguration(
                name = "shared",
                propertiesFileName = "global_testfile",
                applicationId = "com.example.global",
                targetPackage = "com.example.global",
                visibility = INTERNAL,
            )
        }
        val buildTypeExtension = createExtension().apply {
            addGoogleAppIdResource.set(false)
            createConfiguration(
                name = "local",
                propertiesFileName = "local_test_file_build_type",
            )
            createConfiguration(
                name = "shared",
                propertiesFileName = "shared_test_file_build_type",
                applicationId = "test.app",
                targetPackage = "com.example.buildtype",
                targetFileName = "targetFilename",
            )
        }

        val variantExtensionConfig = MockVariantExtensionConfig(
            variant = mockk<ApplicationVariant>(),
            buildTypeExtension = buildTypeExtension,
            productFlavorsExtensions = emptyList(),
        )

        val result = ExtensionMerger(objects, globalExtension).invoke(variantExtensionConfig)

        assertThat(result.addGoogleAppIdResource).value().isFalse()
        assertThat(result.configurations.names).containsOnly("local", "global", "shared")
        assertThat(result.configurations.getByName("global")).all {
            transform { it.source.get() }.isInstanceOf<PropertiesFileGeneratorSource>().all {
                hasFileName("testfile")
                applicationId().isNotPresent()
            }
            targetPackage().isNotPresent()
            targetFileName().isNotPresent()
            propertyName().isNotPresent()
            visibility().isNotPresent()
            nameProp().isEqualTo("global")
        }
        assertThat(result.configurations.getByName("local")).all {
            transform { it.source.get() }.isInstanceOf<PropertiesFileGeneratorSource>().all {
                hasFileName("local_test_file_build_type")
                applicationId().isNotPresent()
            }
            targetPackage().isNotPresent()
            targetFileName().isNotPresent()
            propertyName().isNotPresent()
            visibility().isNotPresent()
            nameProp().isEqualTo("local")
        }
        assertThat(result.configurations.getByName("shared")).all {
            transform { it.source.get() }.isInstanceOf<PropertiesFileGeneratorSource>().all {
                hasFileName("shared_test_file_build_type")
                applicationId().value().isEqualTo("test.app")
            }
            targetPackage().value().isEqualTo("com.example.buildtype")
            targetFileName().value().isEqualTo("targetFilename")
            propertyName().isNotPresent()
            visibility().value().isEqualTo(INTERNAL)
            nameProp().isEqualTo("shared")
        }
    }

    @Suppress("LongMethod")
    @Test
    fun `should merge flavor extensions`() {
        val buildTypeExtension = createExtension().apply {
            addGoogleAppIdResource.set(false)
            createConfiguration(
                name = "localBuildType",
                propertiesFileName = "testfileBuildType",
            )
            createConfiguration(
                name = "shared",
                propertiesFileName = "testfileBuildType3.propertis",
                applicationId = "test.app",
                targetPackage = "com.example.buildtype",
                targetFileName = "targetFilename",
            )
        }
        val demoModeFlavorExtension = createExtension().apply {
            addGoogleAppIdResource.set(true)
            createConfiguration(
                name = "demoModeFlavor",
                propertiesFileName = "demo.properties",
                targetPackage = "com.example.demo",
            )
            createConfiguration(
                name = "shared",
                propertiesFileName = "demo.properties",
                propertyName = "demo",
                targetFileName = "demo",
                visibility = PUBLIC,
            )
        }
        val minApi21FlavorExtension = createExtension().apply {
            addGoogleAppIdResource.set(false)
            createConfiguration(
                name = "minApi21Flavor",
                propertiesFileName = "minapi21.properties",
                targetPackage = "com.example.minapi21",
            )
            createConfiguration(
                name = "shared",
                propertiesFileName = "minapi21.properties",
                targetFileName = "targetFilenameMinapi21",
            )
        }

        val variantExtensionConfig = MockVariantExtensionConfig(
            variant = mockk<ApplicationVariant>(),
            buildTypeExtension = buildTypeExtension,
            productFlavorsExtensions = listOf(minApi21FlavorExtension, demoModeFlavorExtension),
        )

        val result = ExtensionMerger(objects, globalExtension).invoke(variantExtensionConfig)

        assertThat(result.addGoogleAppIdResource).value().isFalse()
        assertThat(result.configurations.asMap.keys).containsOnly(
            "localBuildType",
            "demoModeFlavor",
            "minApi21Flavor",
            "shared",
        )
        assertThat(result.configurations.getByName("localBuildType")).all {
            transform { it.source.get() }.isInstanceOf<PropertiesFileGeneratorSource>().all {
                hasFileName("testfileBuildType")
                applicationId().isNotPresent()
            }
            targetPackage().isNotPresent()
            targetFileName().isNotPresent()
            propertyName().isNotPresent()
            visibility().isNotPresent()
            nameProp().isEqualTo("localBuildType")
        }
        assertThat(result.configurations.getByName("demoModeFlavor")).all {
            transform { it.source.get() }.isInstanceOf<PropertiesFileGeneratorSource>().all {
                hasFileName("demo.properties")
                applicationId().isNotPresent()
            }
            targetPackage().value().isEqualTo("com.example.demo")
            targetFileName().isNotPresent()
            propertyName().isNotPresent()
            visibility().isNotPresent()
            nameProp().isEqualTo("demoModeFlavor")
        }
        assertThat(result.configurations.getByName("minApi21Flavor")).all {
            transform { it.source.get() }.isInstanceOf<PropertiesFileGeneratorSource>().all {
                hasFileName("minapi21.properties")
                applicationId().isNotPresent()
            }
            targetPackage().value().isEqualTo("com.example.minapi21")
            targetFileName().isNotPresent()
            propertyName().isNotPresent()
            visibility().isNotPresent()
            nameProp().isEqualTo("minApi21Flavor")
        }
        assertThat(result.configurations.getByName("shared")).all {
            transform { it.source.get() }.isInstanceOf<PropertiesFileGeneratorSource>().all {
                hasFileName("minapi21.properties")
                applicationId().isNotPresent()
            }
            targetPackage().value().isEqualTo("com.example.buildtype")
            targetFileName().value().isEqualTo("targetFilenameMinapi21")
            propertyName().value().isEqualTo("demo")
            visibility().value().isEqualTo(PUBLIC)
            nameProp().isEqualTo("shared")
        }
    }

    private fun createExtension(): FbaseGeneratorExtension = objects.newInstance(FbaseGeneratorExtension::class.java)

    private fun FbaseGeneratorExtension.createConfiguration(
        name: String,
        propertiesFileName: String,
        applicationId: String? = null,
        targetPackage: String? = null,
        targetFileName: String? = null,
        propertyName: String? = null,
        visibility: TargetVisibility? = null,
    ): FbaseBuilderExtension = this.configurations.create(name).apply {
        fromPropertiesFile {
            it.location.set(project.file(propertiesFileName))
            if (applicationId != null) {
                it.applicationId.set(applicationId)
            }
        }
        if (targetPackage != null) {
            this.targetPackage.set(targetPackage)
        }
        if (targetFileName != null) {
            this.targetFileName.set(targetFileName)
        }
        if (propertyName != null) {
            this.propertyName.set(propertyName)
        }
        if (visibility != null) {
            this.visibility.set(visibility)
        }
    }

    @Suppress("UNCHECKED_CAST", "GENERIC_NAME")
    private class MockVariantExtensionConfig<VariantT : Variant>(
        override val variant: VariantT,
        val buildTypeExtension: FbaseGeneratorExtension? = null,
        val productFlavorsExtensions: List<FbaseGeneratorExtension> = emptyList(),
        val projectExtension: FbaseGeneratorExtension? = null,
    ) : VariantExtensionConfig<VariantT> {
        override fun <T> buildTypeExtension(extensionType: Class<T>): T {
            return if (extensionType != FbaseGeneratorExtension::class.java) {
                null as T
            } else {
                buildTypeExtension as T
            }
        }

        override fun <T> productFlavorsExtensions(extensionType: Class<T>): List<T> {
            return if (extensionType != FbaseGeneratorExtension::class.java) {
                emptyList()
            } else {
                productFlavorsExtensions as List<T>
            }
        }

        override fun <T> projectExtension(extensionType: Class<T>): T {
            return if (extensionType != FbaseGeneratorExtension::class.java) {
                null as T
            } else {
                projectExtension as T
            }
        }
    }
}
