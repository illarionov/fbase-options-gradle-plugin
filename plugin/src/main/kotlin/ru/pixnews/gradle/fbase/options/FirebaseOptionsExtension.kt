package ru.pixnews.gradle.fbase.options

import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property
import ru.pixnews.gradle.fbase.options.data.LocalFirebaseOptions

abstract class FirebaseOptionsExtension {
    /**
     * Firebase configuration parameters used to build [FirebaseOptions].
     */
    abstract val source: Property<LocalFirebaseOptions>

    /**
     * Target package of the generated [FirebaseOptions] instance
     */
    abstract val targetPackage: Property<String>

    /**
     * Generated оbject class with property
     */
    abstract val targetObjectName: Property<String>

    /***
     * Name of the generated property
     */
    abstract val propertyName: Property<String>

    /**
     * Visibility of the generated [targetProperty]
     */
    abstract val visibility: Property<TargetVisibility>

    abstract val providers: FirebaseOptionsProviders

    internal companion object {
        internal fun ExtensionContainer.createFirebaseOptionsExtension(
            name: String = "firebaseOptions",
        ): FirebaseOptionsExtension = create(name, FirebaseOptionsExtension::class.java).apply {
        }
    }
}


