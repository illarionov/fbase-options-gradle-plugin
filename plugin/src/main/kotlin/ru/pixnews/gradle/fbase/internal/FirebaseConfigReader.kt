/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.internal

import ru.pixnews.gradle.fbase.LocalFirebaseOptions
import java.util.Properties

/**
 * Read [LocalFirebaseOptions] values from [Properties].
 *
 * @param properties Properties from which [LocalFirebaseOptions] values will be read
 * @param applicationId If specified, keys with the Application Id suffix
 * will also be taken into account.
 * For example, when [applicationId] is "com.example",
 * then the value for the *Google api key* will be read first from the *"firebase_com_example_project_id"*
 * property and then, if not specified, from the *"firebase_google_api_key"* property in [properties].
 */
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
