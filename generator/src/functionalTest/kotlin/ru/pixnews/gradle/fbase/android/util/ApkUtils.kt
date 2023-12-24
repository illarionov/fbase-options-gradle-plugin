/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.android.util

internal fun getApkPath(
    appName: String,
    buildType: String,
    vararg flavors: String,
): String {
    val flavorsFullNameCamelCase = flavors.reduceOrNull { name, subFlavor -> name + subFlavor.capitalized() }

    val apkName = buildList {
        add(appName)
        addAll(flavors)
        add(buildType)
        if (buildType != "debug") {
            add("unsigned")
        }
    }.joinToString("-")

    return if (flavorsFullNameCamelCase != null) {
        "$flavorsFullNameCamelCase/$buildType/$apkName.apk"
    } else {
        "$buildType/$apkName.apk"
    }
}
