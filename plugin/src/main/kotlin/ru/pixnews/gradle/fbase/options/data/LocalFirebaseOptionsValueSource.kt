package ru.pixnews.gradle.fbase.options.data

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import ru.pixnews.gradle.fbase.options.data.LocalFirebaseOptionsValueSource.Parameters
import ru.pixnews.gradle.fbase.options.reader.FirebaseConfigReader
import ru.pixnews.gradle.fbase.options.util.toProperties

abstract class LocalFirebaseOptionsValueSource : ValueSource<LocalFirebaseOptions, Parameters> {
    override fun obtain(): LocalFirebaseOptions? {
        val configProperties = parameters.configFilePath.get().asFile.toProperties()
        return FirebaseConfigReader(
            configProperties,
            parameters.applicationId.get().ifEmpty { null },
        ).read()
    }

    interface Parameters : ValueSourceParameters {
        val applicationId: Property<String>
        val configFilePath: RegularFileProperty
    }
}