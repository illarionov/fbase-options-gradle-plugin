/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.testmatrix

import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.AGP_8_3_0_BETA01
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.AgpVersionCompatibility.getCompatibleAndroidApiLevel
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.FirebaseCompatibility.FIREBASE_BOM_32_7_0
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.GradleVersionCompatibility.GRADLE_8_5
import ru.pixnews.gradle.fbase.test.functional.testmatrix.compatibility.KotlinVersionCompatibility.KOTLIN_1_9_22

public data class VersionCatalog(
    val gradleVersion: Version,
    val kotlinVersion: Version,
    val agpVersion: Version,
    val androidxCore: Version,
    val firebaseVersion: Version,
    val compileSdk: Int,
    val targetSdk: Int,
    val minSdk: Int = 21,
) {
    public companion object {
        public val ANDROIDX_CORE_VERSION: Version = Version(1, 12, 0)
        public val DEFAULT: VersionCatalog = run {
            val agpVersion = AGP_8_3_0_BETA01
            val compileTargetSdk = getCompatibleAndroidApiLevel(agpVersion)
            VersionCatalog(
                gradleVersion = GRADLE_8_5,
                kotlinVersion = KOTLIN_1_9_22,
                agpVersion = AGP_8_3_0_BETA01,
                androidxCore = ANDROIDX_CORE_VERSION,
                firebaseVersion = FIREBASE_BOM_32_7_0,
                compileSdk = compileTargetSdk,
                targetSdk = compileTargetSdk,
            )
        }
    }
}
