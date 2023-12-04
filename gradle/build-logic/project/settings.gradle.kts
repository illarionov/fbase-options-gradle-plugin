/*
 * Copyright (c) 2023, the fbase-options-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
pluginManagement {
    includeBuild("../settings")
}

plugins {
    id("ru.pixnews.gradle.fbase.build-logic.settings.root")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../libs.versions.toml"))
        }
    }
}

include("kotlin")
include("lint")

rootProject.name = "gradle-project-plugins"
