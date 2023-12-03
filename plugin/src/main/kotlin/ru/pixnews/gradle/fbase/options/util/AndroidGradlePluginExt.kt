/*
 * Copyright (c) 2023, the fbase-options-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.options.util

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestPlugin
import org.gradle.api.Project

internal val androidGradlePlugins: List<Class<out BasePlugin>> = listOf(
    AppPlugin::class.java,
    LibraryPlugin::class.java,
    DynamicFeaturePlugin::class.java,
    TestPlugin::class.java,
)

internal fun Project.withAnyOfAndroidPlugins(
    block: (plugin: BasePlugin, componentsExtension: AndroidComponentsExtension<*, *, *>) -> Unit,
) {
    androidGradlePlugins.forEach { agpPlugin ->
        plugins.withType(agpPlugin) { plugin ->
            extensions.configure(AndroidComponentsExtension::class.java) { componentExtension ->
                block(plugin, componentExtension)
            }
        }
    }
}
