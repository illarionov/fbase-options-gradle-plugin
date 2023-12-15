/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import ru.pixnews.gradle.fbase.source.FbaseGeneratorSource
import ru.pixnews.gradle.fbase.source.PropertiesFileGeneratorSource
import ru.pixnews.gradle.fbase.source.ProvidedGeneratorSource
import java.io.Serializable
import javax.inject.Inject

public abstract class FbaseBuilderExtension @Inject constructor(
    @Transient
    private val objects: ObjectFactory,
    private val name: String,
) : Named, Serializable {
    internal val source: Property<FbaseGeneratorSource> = objects.property(FbaseGeneratorSource::class.java)

    /**
     * Target package of the generated [FirebaseOptions] instance.
     *
     * Default value: namespace of the Android Variant.
     */
    public abstract val targetPackage: Property<String>

    /**
     * Generated Kotlin file with property.
     *
     * Default value: [propertyName] value in capital case.
     */
    public abstract val targetFileName: Property<String>

    /** *
     * Name of the generated property.
     *
     * Default value: name specified when creating the object in `firebaseOptions`
     * [NamedDomainObjectContainer] container
     */
    public abstract val propertyName: Property<String>

    /**
     * Visibility of the generated [targetProperty]
     *
     * Default value: internal
     */
    public abstract val visibility: Property<TargetVisibility>

    override fun getName(): String = name

    public fun fromPropertiesFile(action: Action<PropertiesFileGeneratorSource>) {
        val propertiesSource = objects.newInstance(PropertiesFileGeneratorSource::class.java)
        action.execute(propertiesSource)
        source.set(propertiesSource)
        source.disallowChanges()
    }

    public fun fromValues(action: Action<ProvidedGeneratorSource>) {
        val valuesSource = objects.newInstance(ProvidedGeneratorSource::class.java)
        action.execute(valuesSource)
        source.set(valuesSource)
        source.disallowChanges()
    }

    public companion object {
        private const val serialVersionUID: Long = -1
    }
}
