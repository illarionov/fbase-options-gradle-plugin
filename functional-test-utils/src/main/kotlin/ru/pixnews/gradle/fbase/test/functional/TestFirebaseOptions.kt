/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional

public data class TestFirebaseOptions(
    public val projectId: String? = null,
    public val apiKey: String? = null,
    public val applicationId: String? = null,
    public val databaseUrl: String? = null,
    public val gaTrackingId: String? = null,
    public val gcmSenderId: String? = null,
    public val storageBucket: String? = null,
)
