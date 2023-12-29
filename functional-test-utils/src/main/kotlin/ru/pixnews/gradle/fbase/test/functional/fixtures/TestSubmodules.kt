/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.fixtures

import ru.pixnews.gradle.fbase.test.functional.junit.SubmoduleId

public object TestSubmodules {
    public val androidAppSimple: SubmoduleId = SubmoduleId(
        projectName = "android-app-simple",
        namespace = "com.example.samplefbase",
    )
    public val androidAppFlavors: SubmoduleId = SubmoduleId(
        projectName = "android-app-flavors",
        namespace = "com.example.samplefbase.flavors",
    )
    public val androidAppMulticonfig: SubmoduleId = SubmoduleId(
        projectName = "android-app-multiconfig",
        namespace = "com.example.samplefbase",
    )
    public val androidAppGoogleServicesProject1: SubmoduleId = SubmoduleId(
        projectName = "android-app-google-services-project1",
        namespace = "com.example.myapplication",
    )
    public val androidAppGoogleServicesCustomLocation: SubmoduleId = SubmoduleId(
        projectName = "android-app-google-services-custom-location",
        namespace = "com.example.myapplication",
    )
}
