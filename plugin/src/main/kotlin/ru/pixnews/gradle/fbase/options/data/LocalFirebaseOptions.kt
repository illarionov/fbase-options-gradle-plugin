package ru.pixnews.gradle.fbase.options.data

import java.io.Serializable

class LocalFirebaseOptions(
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
        internal val empty = LocalFirebaseOptions(
            projectId = null,
            apiKey = null,
            applicationId = null,
            databaseUrl = null,
            gaTrackingId = null,
            gcmSenderId = null,
            storageBucket = null,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalFirebaseOptions

        if (projectId != other.projectId) return false
        if (apiKey != other.apiKey) return false
        if (applicationId != other.applicationId) return false
        if (databaseUrl != other.databaseUrl) return false
        if (gaTrackingId != other.gaTrackingId) return false
        if (gcmSenderId != other.gcmSenderId) return false
        if (storageBucket != other.storageBucket) return false

        return true
    }

    override fun hashCode(): Int {
        var result = projectId?.hashCode() ?: 0
        result = 31 * result + (apiKey?.hashCode() ?: 0)
        result = 31 * result + (applicationId?.hashCode() ?: 0)
        result = 31 * result + (databaseUrl?.hashCode() ?: 0)
        result = 31 * result + (gaTrackingId?.hashCode() ?: 0)
        result = 31 * result + (gcmSenderId?.hashCode() ?: 0)
        result = 31 * result + (storageBucket?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "LocalFirebaseOptions(" +
                "projectId=$projectId, " +
                "apiKey=$apiKey, " +
                "applicationId=$applicationId, " +
                "databaseUrl=$databaseUrl, " +
                "gaTrackingId=$gaTrackingId, " +
                "gcmSenderId=$gcmSenderId, " +
                "storageBucket=$storageBucket" +
                ")"
    }


}
