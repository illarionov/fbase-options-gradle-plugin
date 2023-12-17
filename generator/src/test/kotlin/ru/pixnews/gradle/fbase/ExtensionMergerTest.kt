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

        assertThat(result.addGoogleAppIdResource.isPresent).isFalse()
        assertThat(result.configurations.asMap).isEmpty()
    }

    @Test
    fun `should use globalExtension when variant extensions are empty`() {
        globalExtension.apply {
            addGoogleAppIdResource.set(true)
            configurations.create("globalFirebaseOptions1").apply {
                fromPropertiesFile {
                    it.location.set(project.file("testfile"))
                }
            }
        }
        val variantExtensionConfig = MockVariantExtensionConfig(
            variant = mockk<ApplicationVariant>(),
            buildTypeExtension = createExtension(),
            productFlavorsExtensions = emptyList(),
        )

        val result = ExtensionMerger(objects, globalExtension).invoke(variantExtensionConfig)
        assertThat(result.addGoogleAppIdResource).value().isTrue()
        assertThat(result.configurations.asMap.keys).containsOnly("globalFirebaseOptions1")
        assertThat(result.configurations.getByName("globalFirebaseOptions1")).all {
            transform { it.source.get() }.isInstanceOf<PropertiesFileGeneratorSource>().all {
                hasFileName("testfile")
                applicationId().isNotPresent()
            }
            targetPackage().isNotPresent()
            targetFileName().isNotPresent()
            propertyName().isNotPresent()
            visibility().isNotPresent()
            nameProp().isEqualTo("globalFirebaseOptions1")
        }
    }

    @Test
    @Suppress("LongMethod")
    fun `should merge globalExtension and build type extension`() {
        globalExtension.apply {
            addGoogleAppIdResource.set(true)
            configurations.create("globalFirebaseOptions1").apply {
                fromPropertiesFile {
                    it.location.set(project.file("testfile"))
                }
            }
            configurations.create("sharedFirebaseOptions1").apply {
                fromPropertiesFile {
                    it.location.set(project.file("testfile2"))
                }
                targetPackage.set("com.example.global")
                visibility.set(INTERNAL)
            }
        }
        val buildTypeExtension = createExtension().apply {
            addGoogleAppIdResource.set(false)
            configurations.create("localBuildTypeOptions1").apply {
                fromPropertiesFile {
                    it.location.set(project.file("testfileBuildType"))
                }
            }
            configurations.create("sharedFirebaseOptions1").apply {
                fromPropertiesFile {
                    it.location.set(project.file("testfileBuildType3"))
                    it.applicationId.set("test.app")
                }
                targetPackage.set("com.example.buildtype")
                targetFileName.set("targetFilename")
            }
        }

        val variantExtensionConfig = MockVariantExtensionConfig(
            variant = mockk<ApplicationVariant>(),
            buildTypeExtension = buildTypeExtension,
            productFlavorsExtensions = emptyList(),
        )

        val result = ExtensionMerger(objects, globalExtension).invoke(variantExtensionConfig)
        assertThat(result.addGoogleAppIdResource).value().isFalse()
        assertThat(result.configurations.asMap.keys).containsOnly(
            "globalFirebaseOptions1",
            "sharedFirebaseOptions1",
            "localBuildTypeOptions1",
        )
        assertThat(result.configurations.getByName("globalFirebaseOptions1")).all {
            transform { it.source.get() }.isInstanceOf<PropertiesFileGeneratorSource>().all {
                hasFileName("testfile")
                applicationId().isNotPresent()
            }
            targetPackage().isNotPresent()
            targetFileName().isNotPresent()
            propertyName().isNotPresent()
            visibility().isNotPresent()
            nameProp().isEqualTo("globalFirebaseOptions1")
        }
        assertThat(result.configurations.getByName("localBuildTypeOptions1")).all {
            transform { it.source.get() }.isInstanceOf<PropertiesFileGeneratorSource>().all {
                hasFileName("testfileBuildType")
                applicationId().isNotPresent()
            }
            targetPackage().isNotPresent()
            targetFileName().isNotPresent()
            propertyName().isNotPresent()
            visibility().isNotPresent()
            nameProp().isEqualTo("localBuildTypeOptions1")
        }
        assertThat(result.configurations.getByName("sharedFirebaseOptions1")).all {
            transform { it.source.get() }.isInstanceOf<PropertiesFileGeneratorSource>().all {
                hasFileName("testfileBuildType3")
                applicationId().value().isEqualTo("test.app")
            }
            targetPackage().value().isEqualTo("com.example.buildtype")
            targetFileName().value().isEqualTo("targetFilename")
            propertyName().isNotPresent()
            visibility().value().isEqualTo(INTERNAL)
            nameProp().isEqualTo("sharedFirebaseOptions1")
        }
    }

    private fun createExtension(): FbaseGeneratorExtension = objects.newInstance(FbaseGeneratorExtension::class.java)

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
