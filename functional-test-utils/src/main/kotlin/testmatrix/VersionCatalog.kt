/*
 * Copyright (c) 2023-2024, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.testmatrix

import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.AGP_8_5_0
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.getCompatibleAndroidApiLevel
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.FirebaseCompatibility.FIREBASE_BOM_33_1_2
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_8_9
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.KotlinVersionCompatibility.KOTLIN_2_0_0

public data class VersionCatalog(
    val gradleVersion: Version,
    val kotlinVersion: Version,
    val agpVersion: Version,
    val androidxCore: Version,
    val firebaseVersion: Version,
    val compileSdk: Int,
    val targetSdk: Int,
    val minSdk: Int = 21,
    val fbasePluginVersion: String,
) {
    public companion object {
        public val ANDROIDX_CORE_VERSION: Version = Version(1, 13, 1)
        public fun getDefault(fbasePluginVersion: String): VersionCatalog {
            val agpVersion = AGP_8_5_0
            val compileTargetSdk = getCompatibleAndroidApiLevel(agpVersion)
            return VersionCatalog(
                gradleVersion = GRADLE_8_9,
                kotlinVersion = KOTLIN_2_0_0,
                agpVersion = agpVersion,
                androidxCore = ANDROIDX_CORE_VERSION,
                firebaseVersion = FIREBASE_BOM_33_1_2,
                compileSdk = compileTargetSdk,
                targetSdk = compileTargetSdk,
                fbasePluginVersion = fbasePluginVersion,
            )
        }
    }
}
