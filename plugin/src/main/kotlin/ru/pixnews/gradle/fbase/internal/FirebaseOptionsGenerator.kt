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
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import ru.pixnews.gradle.fbase.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.TargetVisibility
import java.io.File

internal class FirebaseOptionsGenerator(
    private val options: LocalFirebaseOptions,
    private val codeGenDir: File,
    private val outputObjectClassName: ClassName,
    private val propertyName: String,
    private val visibility: TargetVisibility,
) {
    fun generate() {
        val outputFileName = outputObjectClassName.simpleName
        val packageName = outputObjectClassName.packageName

        val firebaseOptionsProperty = PropertySpec.builder(
            name = propertyName,
            type = firebaseOptionsClassName,
            visibility.toModifier(),
        )
            .initializer(
                buildCodeBlock {
                    addStatement("%T()", firebaseOptionsBuilderClassName)
                    firebaseBuilderMethods.forEach { (statement, valueFactory) ->
                        valueFactory(options)?.let {
                            addStatement(".$statement(%S)", it)
                        }
                    }
                    addStatement(".build()")
                },
            )
            .build()

        val objectSpec = TypeSpec.objectBuilder(outputObjectClassName)
            .addModifiers(visibility.toModifier())
            .addProperty(firebaseOptionsProperty)
            .build()

        val fileContent = FileSpec.builder(packageName, outputFileName)
            .addType(objectSpec)
            .build()
        fileContent.writeTo(codeGenDir)
    }

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
