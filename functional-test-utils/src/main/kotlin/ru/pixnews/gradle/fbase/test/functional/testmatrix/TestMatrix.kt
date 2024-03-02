/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.testmatrix

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.AGP_8_1_4
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.AGP_8_2_0
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.AGP_8_3_0
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.AGP_8_4_0_ALPHA_12
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.agpIsCompatibleWithGradle
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.getCompatibleAndroidApiLevel
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.isAgpCompatibleWithRuntime
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.FirebaseCompatibility.FIREBASE_BOM_32_3_1
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.FirebaseCompatibility.FIREBASE_BOM_32_4_1
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.FirebaseCompatibility.FIREBASE_BOM_32_5_0
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.FirebaseCompatibility.FIREBASE_BOM_32_6_0
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.FirebaseCompatibility.FIREBASE_BOM_32_7_3
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_7_5_1
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_7_6_3
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_8_0_2
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_8_1_1
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_8_2_1
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_8_3
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_8_4
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_8_5
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_8_6
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_8_7_RC2
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.isGradleCompatibleWithRuntime

public class TestMatrix(
    fbasePluginVersion: String,
) {
    private val logger: Logger = LoggerFactory.getLogger(TestMatrix::class.java)
    private val defaultVersionCatalog: VersionCatalog = VersionCatalog.getDefault(fbasePluginVersion)
    private val gradleVersions = listOf(
        GRADLE_8_7_RC2,
        GRADLE_8_6,
        GRADLE_8_5,
        GRADLE_8_4,
        GRADLE_8_3,
        GRADLE_8_2_1,
        GRADLE_8_1_1,
        GRADLE_8_0_2,
        GRADLE_7_6_3,
        GRADLE_7_5_1,
    )

    // See https://developer.android.com/studio/releases/gradle-plugin
    private val agpVersions = listOf(
        AGP_8_4_0_ALPHA_12,
        AGP_8_3_0,
        AGP_8_2_0,
        AGP_8_1_4,
    )
    private val firebaseVersions = listOf(
        FIREBASE_BOM_32_7_3,
        FIREBASE_BOM_32_6_0,
        FIREBASE_BOM_32_5_0,
        FIREBASE_BOM_32_4_1,
        FIREBASE_BOM_32_3_1,
    )

    public fun getMainTestVariants(): List<VersionCatalog> = getCompatibleGradleAgpVariants()
        .map { (gradleVersion, agpVersion) ->
            val compileTargetSdk = getCompatibleAndroidApiLevel(agpVersion)
            defaultVersionCatalog.copy(
                gradleVersion = gradleVersion,
                agpVersion = agpVersion,
                compileSdk = compileTargetSdk,
                targetSdk = compileTargetSdk,
            )
        }
        .toList()
        .also { catalogs ->
            require(catalogs.isNotEmpty()) {
                "Found no compatible AGP and Gradle version combination, check your supplied arguments."
            }
        }

    public fun getFirebaseTestVariants(): List<VersionCatalog> = firebaseVersions().map {
        defaultVersionCatalog.copy(firebaseVersion = it)
    }

    private fun getCompatibleGradleAgpVariants(): Sequence<Pair<Version, Version>> {
        val (gradleCompatibleVersions, gradleIncompatibleVersions) = gradleVersions().partition {
            isGradleCompatibleWithRuntime(it.baseVersion())
        }

        if (gradleIncompatibleVersions.isNotEmpty()) {
            logger.warn(
                "Gradle versions {} cannot be run on the current JVM `{}`",
                gradleIncompatibleVersions.joinToString(),
                Runtime.version(),
            )
        }

        val (agpCompatibleVersions, agpIncompatibleVersions) = agpVersions().partition {
            isAgpCompatibleWithRuntime(it)
        }

        if (agpIncompatibleVersions.isNotEmpty()) {
            logger.warn(
                "Android Gradle Plugin versions {} cannot be run on the current JVM `{}`",
                agpIncompatibleVersions.joinToString(),
                Runtime.version(),
            )
        }

        return sequence {
            gradleCompatibleVersions.forEach { gradleVersion ->
                agpCompatibleVersions.forEach { agpVersion ->
                    yield(gradleVersion to agpVersion)
                }
            }
        }.filter { (gradleVersion, agpVersion) ->
            agpIsCompatibleWithGradle(agpVersion, gradleVersion)
        }
    }

    // Allow setting a single, fixed Gradle version via environment variables
    private fun gradleVersions(): List<Version> {
        val gradleVersion = System.getenv("GRADLE_VERSION")
        return if (gradleVersion == null) {
            gradleVersions
        } else {
            listOf(Version.parse(gradleVersion))
        }
    }

    // Allow setting a single, fixed AGP version via environment variables
    private fun agpVersions(): List<Version> {
        val agpVersion = System.getenv("AGP_VERSION")
        return if (agpVersion == null) {
            agpVersions
        } else {
            listOf(Version.parse(agpVersion))
        }
    }

    private fun firebaseVersions(): List<Version> {
        val firebaseVersion = System.getenv("FIREBASE_VERSION")
        return if (firebaseVersion == null) {
            firebaseVersions
        } else {
            listOf(Version.parse(firebaseVersion))
        }
    }
}
