/*
 * Copyright (c) 2024, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

plugins {
    `kotlin-dsl`
}

group = "ru.pixnews.gradle.fbase.build-logic.settings"

dependencies {
    implementation("com.gradle:gradle-enterprise-gradle-plugin:3.15.1")
}
