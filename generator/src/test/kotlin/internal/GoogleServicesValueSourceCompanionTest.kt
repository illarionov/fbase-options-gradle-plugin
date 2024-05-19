/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.internal

import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.pixnews.gradle.fbase.internal.GoogleServicesValueSource.Companion.getJsonLocations

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GoogleServicesValueSourceCompanionTest {
    @ParameterizedTest
    @MethodSource("getJsonLocationsTestSource")
    fun `getJsonLocations should return paths in correct order`(test: GetJsonLocationsTest) {
        val locations = getJsonLocations(
            buildType = test.buildType,
            flavorNames = test.flavorNames,
        )
        assertThat(locations).containsExactly(
            elements = test.expectedLocations.toTypedArray(),
        )
    }

    fun getJsonLocationsTestSource(): List<GetJsonLocationsTest> = listOf(
        GetJsonLocationsTest(
            buildType = "release",
            flavorNames = emptyList(),
            expectedLocations = listOf(
                "src/release/google-services.json",
                "src/google-services.json",
                "google-services.json",
            ),
        ),
        GetJsonLocationsTest(
            buildType = "release",
            flavorNames = listOf("flavor"),
            expectedLocations = listOf(
                "src/flavor/release/google-services.json",
                "src/release/flavor/google-services.json",
                "src/flavor/google-services.json",
                "src/release/google-services.json",
                "src/flavorRelease/google-services.json",
                "google-services.json",
            ),
        ),
        GetJsonLocationsTest(
            buildType = "release",
            flavorNames = listOf("flavor", "test"),
            expectedLocations = listOf(
                "src/flavor/test/release/google-services.json",
                "src/flavorTest/release/google-services.json",
                "src/release/flavorTest/google-services.json",
                "src/flavor/release/google-services.json",
                "src/flavor/test/google-services.json",
                "src/flavor/testRelease/google-services.json",
                "src/flavorTest/google-services.json",
                "src/release/google-services.json",
                "src/flavorTestRelease/google-services.json",
                "src/flavor/google-services.json",
                "src/flavorRelease/google-services.json",
                "google-services.json",
            ),
        ),
        GetJsonLocationsTest(
            buildType = "release",
            flavorNames = listOf("flavor", "testTest"),
            expectedLocations = listOf(
                "src/flavor/testTest/release/google-services.json",
                "src/flavorTestTest/release/google-services.json",
                "src/release/flavorTestTest/google-services.json",
                "src/flavor/release/google-services.json",
                "src/flavor/testTest/google-services.json",
                "src/flavor/testTestRelease/google-services.json",
                "src/flavorTestTest/google-services.json",
                "src/release/google-services.json",
                "src/flavorTestTestRelease/google-services.json",
                "src/flavor/google-services.json",
                "src/flavorRelease/google-services.json",
                "google-services.json",
            ),
        ),
    )

    data class GetJsonLocationsTest(
        val buildType: String,
        val flavorNames: List<String>,
        val expectedLocations: List<String>,
    )
}
