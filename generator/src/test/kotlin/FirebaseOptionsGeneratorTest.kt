/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.pixnews.gradle.fbase.TargetVisibility.INTERNAL
import ru.pixnews.gradle.fbase.TargetVisibility.PUBLIC
import ru.pixnews.gradle.fbase.internal.FirebaseOptionsGenerator
import ru.pixnews.gradle.fbase.internal.FirebaseOptionsGenerator.PropertyValues
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
class FirebaseOptionsGeneratorTest {
    @TempDir
    var codeGenDir: File? = null

    @Test
    fun `Generated config should compile`() {
        val fbaseOptions1 = FbaseOptions(
            projectId = "PROJECT_ID",
            apiKey = "API_KEY",
            applicationId = "APPLICATION_ID",
            databaseUrl = "DATABASE_TRACKING_URL",
            gaTrackingId = "GA_TRACKING_ID",
            gcmSenderId = "GCM_SENDER_ID",
            storageBucket = "STORAGE_BUCKET",
        )
        val fbaseOptions2 = FbaseOptions(
            projectId = "PROJECT_ID2",
            apiKey = "API_KEY2",
            applicationId = "APPLICATION_ID2",
            databaseUrl = "DATABASE_TRACKING_URL2",
            gaTrackingId = "GA_TRACKING_ID2",
            gcmSenderId = "GCM_SENDER_ID2",
            storageBucket = "STORAGE_BUCKET2",
        )
        val properties = listOf(
            PropertyValues(
                options = fbaseOptions1,
                propertyName = "firebaseOptions",
                visibility = INTERNAL,
            ),
            PropertyValues(
                options = fbaseOptions2,
                propertyName = "firebaseOptions2",
                visibility = PUBLIC,
            ),
        )

        val compilationResult = compileFirebaseOptions(
            properties,
            codeGenDir!!,
        )
        assertThat(compilationResult.exitCode).isEqualTo(OK)
    }

    private fun compileFirebaseOptions(
        properties: List<PropertyValues>,
        codeGenDir: File,
    ): JvmCompilationResult {
        FirebaseOptionsGenerator(
            codeGenDir = codeGenDir,
            outputPackageName = "com.test",
            outputFileName = "GeneratedFirebaseOptions",
            properties = properties,
        ).generate()

        val firebaseOptionsStub = SourceFile.fromPath(
            File(
                javaClass.classLoader.getResource("FirebaseOptions.java")?.file
                    ?: error("No StubFirebaseOptions.java"),
            ),
        )

        val generatedSource = SourceFile.fromPath(
            File(codeGenDir, "com/test/GeneratedFirebaseOptions.kt"),
        )

        return KotlinCompilation().apply {
            sources = listOf(firebaseOptionsStub, generatedSource)
            inheritClassPath = false
            messageOutputStream = System.out
        }.compile()
    }
}
