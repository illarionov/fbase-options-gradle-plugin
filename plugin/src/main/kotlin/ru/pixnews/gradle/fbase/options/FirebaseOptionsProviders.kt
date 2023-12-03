package ru.pixnews.gradle.fbase.options

import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import ru.pixnews.gradle.fbase.options.data.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.options.data.LocalFirebaseOptionsValueSource
import javax.inject.Inject

public class FirebaseOptionsProviders @Inject private constructor(
    private val project: Project,
    private val providers: ProviderFactory,
) {
    fun propertiesFile(
        configFilePath: RegularFile = project.defaultConfigFile(),
        applicationIdProvider: Provider<String> = providers.provider { "" },
    ): Provider<LocalFirebaseOptions> = providers.of(LocalFirebaseOptionsValueSource::class.java) { valueSource ->
        valueSource.parameters {
            it.applicationId.set(applicationIdProvider)
            it.configFilePath.set(configFilePath)
        }
    }

    internal companion object {
        private fun Project.defaultConfigFile() = rootProject.layout.projectDirectory.file("config/firebase.properties")
    }
}
