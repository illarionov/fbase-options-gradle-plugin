/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.fixtures

import ru.pixnews.gradle.fbase.FBASE_VERSION
import ru.pixnews.gradle.fbase.junit.FileContent
import ru.pixnews.gradle.fbase.test.functional.testmatrix.VersionCatalog

fun VersionCatalog.toLibsVersionsToml(): FileContent {
    val versionCatalogText = """
        [versions]
        agp = "$agpVersion"
        kotlin = "$kotlinVersion"
        minSdk = "$minSdk"
        targetSdk = "$targetSdk"
        compileSdk = "$compileSdk"

        androidx-core-ktx = "$androidxCore"
        fbase-config-generator-gradle-plugin = "$FBASE_VERSION"
        firebase = "$firebaseVersion"

        [libraries]
        agp-plugin = { module = "com.android.tools.build:gradle", version.ref = "agp" }
        androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "androidx-core-ktx" }
        firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebase"}
        firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics"}
        firebase-crashlitycs = { group = "com.google.firebase", name = "firebase-crashlytics"}
        firebase-config = { group = "com.google.firebase", name = "firebase-config"}

        [plugins]
        android-application = { id = "com.android.application", version.ref = "agp" }
        fbase-config-generator-gradle-plugin = { id = "ru.pixnews.gradle.fbase", version.ref = "fbase-config-generator-gradle-plugin" }
        jetbrains-kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
        jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
    """.trimIndent()

    return FileContent(
        dstPath = "gradle/libs.versions.toml",
        content = versionCatalogText,
    )
}
