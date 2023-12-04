package ru.pixnews.gradle.fbase.options.util

import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.Variant
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import ru.pixnews.gradle.fbase.options.TargetVisibility

internal class VariantDefaults(
    val providers: ProviderFactory,
    val variant: Variant,
) {
    val applicationId: Provider<String>
        get() = if (variant is ApplicationVariant) {
            variant.applicationId
        } else {
            providers.provider { "" }
        }

    val targetPackage: Provider<String>
        get() = variant.namespace

    internal companion object PluginDefaults {
        internal const val EXTENSION_NAME = "firebaseOptions"
        internal const val DEFAULT_TARGET_OBJECT_NAME = "GeneratedFirebaseOptions"
        internal const val DEFAULT_PROPERTY_NAME = "firebaseOptions"
        internal val DEFAULT_VISIBILITY = TargetVisibility.INTERNAL
        internal const val DEFAULT_CONFIG_FILE_PATH = "config/firebase.properties"
    }
}
