/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.internal

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import ru.pixnews.gradle.fbase.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.TargetVisibility
import java.io.File

internal class FirebaseOptionsGenerator(
    private val codeGenDir: File,
    private val outputPackageName: String,
    private val outputFileName: String,
    private val properties: List<PropertyValues>,
) {
    fun generate() {
        val propertiesSpec = properties.map(::generateProperty)
        val builder = FileSpec.builder(outputPackageName, outputFileName)
        propertiesSpec.forEach(builder::addProperty)
        builder.build().writeTo(codeGenDir)
    }

    private fun generateProperty(
        property: PropertyValues,
    ): PropertySpec = PropertySpec.builder(
        name = property.propertyName,
        type = firebaseOptionsClassName,
        property.visibility.toModifier(),
    )
        .initializer(
            buildCodeBlock {
                addStatement("%T()", firebaseOptionsBuilderClassName)
                val options: LocalFirebaseOptions = property.options
                firebaseBuilderMethods.forEach { (statement, valueFactory) ->
                    valueFactory(options)?.let {
                        addStatement(".$statement(%S)", it)
                    }
                }
                addStatement(".build()")
            },
        )
        .build()

    internal class PropertyValues(
        val options: LocalFirebaseOptions,
        val propertyName: String,
        val visibility: TargetVisibility,
    )

    private companion object {
        const val DUMMY_APPLICATION_ID = "DUMMY_APPLICATION_ID"
        const val DUMMY_API_KEY = "DUMMY_API_KEY"
        val firebaseOptionsClassName = ClassName(
            "com.google.firebase",
            "FirebaseOptions",
        )
        val firebaseOptionsBuilderClassName = ClassName(
            "com.google.firebase",
            "FirebaseOptions",
            "Builder",
        )
        val firebaseBuilderMethods: List<Pair<String, (LocalFirebaseOptions) -> String?>> = listOf(
            "setProjectId" to LocalFirebaseOptions::projectId,
            "setApiKey" to { options -> options.apiKey ?: DUMMY_API_KEY },
            "setApplicationId" to { options -> options.applicationId ?: DUMMY_APPLICATION_ID },
            "setDatabaseUrl" to LocalFirebaseOptions::databaseUrl,
            "setGaTrackingId" to LocalFirebaseOptions::gaTrackingId,
            "setGcmSenderId" to LocalFirebaseOptions::gcmSenderId,
            "setStorageBucket" to LocalFirebaseOptions::storageBucket,
        )

        fun TargetVisibility.toModifier(): KModifier = when (this) {
            TargetVisibility.INTERNAL -> KModifier.INTERNAL
            TargetVisibility.PUBLIC -> KModifier.PUBLIC
        }
    }
}
