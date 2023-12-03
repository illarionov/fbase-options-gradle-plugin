package ru.pixnews.gradle.fbase.options.data

import java.io.Serializable

data class LocalFirebaseOptions(
    val projectId: String?,
    val apiKey: String?,
    val applicationId: String?,
    val databaseUrl: String?,
    val gaTrackingId: String?,
    val gcmSenderId: String?,
    val storageBucket: String?,
) : Serializable {
    companion object {
        @Suppress("CONSTANT_UPPERCASE")
        private const val serialVersionUID: Long = -1
        val empty = LocalFirebaseOptions(
            projectId = null,
            apiKey = null,
            applicationId = null,
            databaseUrl = null,
            gaTrackingId = null,
            gcmSenderId = null,
            storageBucket = null,
        )
    }
}