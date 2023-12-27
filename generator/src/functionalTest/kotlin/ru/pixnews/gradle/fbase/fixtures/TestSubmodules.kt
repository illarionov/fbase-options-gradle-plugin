/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.fixtures

import ru.pixnews.gradle.fbase.junit.SubmoduleId

object TestSubmodules {
    val androidAppSimple: SubmoduleId = SubmoduleId(
        projectName = "android-app-simple",
        namespace = "com.example.samplefbase",
    )
    val androidAppFlavors: SubmoduleId = SubmoduleId(
        projectName = "android-app-flavors",
        namespace = "com.example.samplefbase.flavors",
    )
    val androidAppMulticonfig: SubmoduleId = SubmoduleId(
        projectName = "android-app-multiconfig",
        namespace = "com.example.samplefbase",
    )
    val androidAppGoogleServicesProject1: SubmoduleId = SubmoduleId(
        projectName = "android-app-google-services-project1",
        namespace = "com.example.myapplication",
    )
    val androidAppGoogleServicesCustomLocation: SubmoduleId = SubmoduleId(
        projectName = "android-app-google-services-custom-location",
        namespace = "com.example.myapplication",
    )
}
