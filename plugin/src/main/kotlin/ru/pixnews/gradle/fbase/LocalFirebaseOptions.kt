/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import java.io.Serializable

@Suppress("LongParameterList")
public class LocalFirebaseOptions(
    public val projectId: String?,
    public val apiKey: String?,
    public val applicationId: String?,
    public val databaseUrl: String?,
    public val gaTrackingId: String?,
    public val gcmSenderId: String?,
    public val storageBucket: String?,
) : Serializable {
    @Suppress("NO_BRACES_IN_CONDITIONALS_AND_LOOPS")
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
    public companion object {
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
}
