package ru.pixnews.gradle.fbase.options.reader

import ru.pixnews.gradle.fbase.options.data.LocalFirebaseOptions
import java.util.*

internal class FirebaseConfigReader(
    private val properties: Properties,
    applicationId: String?,
) {
    @Suppress("NULLABLE_PROPERTY_TYPE")
    private val applicationIdPrefix: String? = applicationId?.replace('.', '_')

    fun read(): LocalFirebaseOptions {
        return LocalFirebaseOptions(
            projectId = readApplicationOrDefaultProperty("project_id"),
            apiKey = readApplicationOrDefaultProperty("google_api_key"),
            applicationId = readApplicationOrDefaultProperty("google_app_id"),
            databaseUrl = readApplicationOrDefaultProperty("database_url"),
            gaTrackingId = readApplicationOrDefaultProperty("ga_tracking_id"),
            gcmSenderId = readApplicationOrDefaultProperty("gcm_default_sender_id"),
            storageBucket = readApplicationOrDefaultProperty("storage_bucket"),
        )
    }

    private fun readApplicationOrDefaultProperty(key: String): String? {
        return applicationIdPrefix?.let {
            properties.getProperty("firebase_${applicationIdPrefix}_$key", null)
        } ?: properties.getProperty("firebase_$key", null)
    }
}