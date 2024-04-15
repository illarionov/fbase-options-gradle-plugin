/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.testmatrix

import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.AGP_8_3_2
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.getCompatibleAndroidApiLevel
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.FirebaseCompatibility.FIREBASE_BOM_32_8_1
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_8_7
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.KotlinVersionCompatibility.KOTLIN_2_0_0_RC1

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
        public val ANDROIDX_CORE_VERSION: Version = Version(1, 12, 0)
        public fun getDefault(fbasePluginVersion: String): VersionCatalog {
            val agpVersion = AGP_8_3_2
            val compileTargetSdk = getCompatibleAndroidApiLevel(agpVersion)
            return VersionCatalog(
                gradleVersion = GRADLE_8_7,
                kotlinVersion = KOTLIN_2_0_0_RC1,
                agpVersion = agpVersion,
                androidxCore = ANDROIDX_CORE_VERSION,
                firebaseVersion = FIREBASE_BOM_32_8_1,
                compileSdk = compileTargetSdk,
                targetSdk = compileTargetSdk,
                fbasePluginVersion = fbasePluginVersion,
            )
        }
    }
}
