/*
 * Copyright (c) 2023, the fbase-options-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.options

import com.squareup.kotlinpoet.ClassName
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.pixnews.gradle.fbase.options.data.LocalFirebaseOptions
import ru.pixnews.gradle.fbase.options.data.TargetVisibility.INTERNAL
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
class FirebaseOptionsGeneratorTest {
    @TempDir
    var codeGenDir: File? = null

    @Test
    fun `Generated config should compile`() {
        val localFirebaseOptions = LocalFirebaseOptions(
            projectId = "PROJECT_ID",
            apiKey = "API_KEY",
            applicationId = "APPLICATION_ID",
            databaseUrl = "DATABASE_TRACKING_URL",
            gaTrackingId = "GA_TRACKING_ID",
            gcmSenderId = "GCM_SENDER_ID",
            storageBucket = "STORAGE_BUCKET",
        )
        val compilationResult = compileFirebaseOptions(
            localFirebaseOptions,
            codeGenDir!!,
        )
        compilationResult.exitCode shouldBe OK
    }

    private fun compileFirebaseOptions(
        options: LocalFirebaseOptions,
        codeGenDir: File,
    ): JvmCompilationResult {
        FirebaseOptionsGenerator(
            options = options,
            codeGenDir = codeGenDir,
            outputObjectClassName = ClassName("com.test", "GeneratedFirebaseOptions"),
            propertyName = "firebaseOptions",
            visibility = INTERNAL,
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
