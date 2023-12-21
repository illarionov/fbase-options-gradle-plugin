/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.android.fixtures

import ru.pixnews.gradle.fbase.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.android.util.capitalized

object AndroidAppFlavorsFixtures {
    const val PROJECT_NAME = "android-app-flavors"
    const val NAMESPACE = "com.example.samplefbase.flavors"
    val firebaseProperties = LocalFirebaseOptions(
        projectId = "sample-en",
        apiKey = "AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZ8",
        applicationId = "1:1035469437089:android:73a4fb8297b2cd4f",
        databaseUrl = "https://sample-en.firebaseio.com",
        gaTrackingId = "UA-65557217-3",
        gcmSenderId = "1035469437089",
        storageBucket = "sample-en.appspot.com",
    )
    val firebaseBenchmarkProperties = LocalFirebaseOptions(
        projectId = "sample-benchmark-en",
        apiKey = "AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZ9",
        applicationId = "1:1035469437089:android:73a4fb8297b2cd50",
        databaseUrl = "https://sample-benchmark-en.firebaseio.com",
        gaTrackingId = "UA-65557217-5",
        gcmSenderId = "1035469437090",
        storageBucket = "sample-benchmark-en.appspot.com",
    )
    val firebaseDemoProperties = LocalFirebaseOptions(
        projectId = "sample-demo-en",
        apiKey = "AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZA",
        applicationId = "1:1035469437089:android:73a4fb8297b2cd51",
        databaseUrl = "https://sample-demo-en.firebaseio.com",
        gaTrackingId = "UA-65557217-6",
        gcmSenderId = "1035469437091",
        storageBucket = "sample-demo-en.appspot.com",
    )
    val firebaseFullProperties = LocalFirebaseOptions(
        projectId = "sample-full-en",
        apiKey = "AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZB",
        applicationId = "1:1035469437089:android:73a4fb8297b2cd52",
        databaseUrl = "https://sample-full-en.firebaseio.com",
        gaTrackingId = "UA-65557217-7",
        gcmSenderId = "1035469437092",
        storageBucket = "sample-full-en.appspot.com",
    )
    val firebaseMinApi21Properties = LocalFirebaseOptions(
        projectId = "sample-minapi21-en",
        apiKey = "AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZC",
        applicationId = "1:1035469437089:android:73a4fb8297b2cd53",
        databaseUrl = "https://sample-minapi21-en.firebaseio.com",
        gaTrackingId = "UA-65557217-8",
        gcmSenderId = "1035469437093",
        storageBucket = "sample-minapi21-en.appspot.com",
    )
    val firebaseMinApi24Properties = LocalFirebaseOptions(
        projectId = "sample-minapi24-en",
        apiKey = "AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZD",
        applicationId = "1:1035469437089:android:73a4fb8297b2cd54",
        databaseUrl = "https://sample-minapi24-en.firebaseio.com",
        gaTrackingId = "UA-65557217-9",
        gcmSenderId = "1035469437094",
        storageBucket = "sample-minapi24-en.appspot.com",
    )
    val firebaseReleaseProperties = LocalFirebaseOptions(
        projectId = "sample-release-en",
        apiKey = "AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZE",
        applicationId = "1:1035469437089:android:73a4fb8297b2cd55",
        databaseUrl = "https://sample-release-en.firebaseio.com",
        gaTrackingId = "UA-65557218-1",
        gcmSenderId = "1035469437095",
        storageBucket = "sample-release-en.appspot.com",
    )
    val testedVariants: List<AppFlavorsVariant> = buildList {
        listOf("minApi21", "minApi24").forEach { api ->
            listOf("demo", "full").forEach { mode ->
                listOf("debug", "benchmark", "release").forEach { buildType ->
                    val expectedGoogleAppId = when (api) {
                        "minApi21" -> firebaseMinApi21Properties.applicationId!!
                        "minApi24" -> firebaseMinApi24Properties.applicationId!!
                        else -> error("Unknown api")
                    }

                    val variant = AppFlavorsVariant(
                        api = api,
                        mode = mode,
                        buildType = buildType,
                        expectedGoogleAppId = expectedGoogleAppId,
                        expectedBuilders = getExpectedBuilders(api, mode, buildType),
                    )
                    add(variant)
                }
            }
        }
    }

    private fun getExpectedBuilders(
        api: String,
        mode: String,
        buildType: String,
    ): List<Pair<String, LocalFirebaseOptions>> = buildList {
        when (api) {
            "minApi21" -> add("MinApi21FirebaseOptionsKt" to firebaseMinApi21Properties)
            "minApi24" -> add("MinApi24FirebaseOptionsKt" to firebaseMinApi24Properties)
        }
        when (mode) {
            "demo" -> add("DemoFirebaseOptionsKt" to firebaseDemoProperties)
            "full" -> add("FullFirebaseOptionsKt" to firebaseFullProperties)
        }
        when (buildType) {
            "release" -> add("ReleaseFirebaseOptionsKt" to firebaseReleaseProperties)
            "benchmark" -> add("BenchmarkFirebaseOptionsKt" to firebaseBenchmarkProperties)
        }
        add("FirebaseOptionsKt" to firebaseProperties)
    }

    data class AppFlavorsVariant(
        val api: String,
        val mode: String,
        val buildType: String,
        val expectedGoogleAppId: String,
        val expectedBuilders: List<Pair<String, LocalFirebaseOptions>>,
    ) {
        val apkPath: String = run {
            val unsignedSuffix = if (buildType != "debug") {
                "-unsigned"
            } else {
                ""
            }
            "$api${mode.capitalized()}/$buildType/$PROJECT_NAME-$api-$mode-$buildType$unsignedSuffix.apk"
        }
    }
}
