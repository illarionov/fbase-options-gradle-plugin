/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.android.apk

import com.android.tools.apk.analyzer.Archives
import com.android.tools.apk.analyzer.BinaryXmlParser.formatValue
import com.android.tools.apk.analyzer.dex.DexDisassembler
import com.android.tools.apk.analyzer.dex.DexFiles
import com.android.tools.apk.analyzer.dex.PackageTreeCreator
import com.android.tools.apk.analyzer.internal.SigUtils
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceFile
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceValue
import com.google.devrel.gmscore.tools.apk.arsc.Chunk
import com.google.devrel.gmscore.tools.apk.arsc.ResourceTableChunk
import com.google.devrel.gmscore.tools.apk.arsc.TypeSpecChunk
import java.io.File
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.readBytes
import kotlin.io.path.useDirectoryEntries

class ApkAnalyzer(
    val path: File,
) {
    fun getStringResource(
        name: String = "google_app_id",
        resourcePackage: String? = null,
        config: String = "default",
        type: String = "string",
    ): String? {
        return Archives.open(path.toPath()).use { archiveContext ->
            val resContents = archiveContext.archive.contentRoot.resolve("resources.arsc").readBytes()
            val binaryRes = BinaryResourceFile(resContents)
            val chunks: List<Chunk> = binaryRes.chunks
            if (chunks.isEmpty()) {
                throw IOException("no chunks")
            }
            if (chunks[0] !is ResourceTableChunk) {
                throw IOException("no res table chunk")
            }
            val resourceTableChunk = chunks[0] as ResourceTableChunk
            val stringPoolChunk = resourceTableChunk.stringPool
            val packageChunk = if (resourcePackage != null) {
                resourceTableChunk.getPackage(resourcePackage)
            } else {
                resourceTableChunk.packages.firstOrNull()
            }
            checkNotNull(packageChunk) {
                val resourcePackageString = resourcePackage?.let { "($it)" } ?: ""
                "Can't find package chunk $resourcePackageString"
            }
            val typeSpecChunk: TypeSpecChunk = packageChunk.getTypeSpecChunk(type)
            val typeChunks = packageChunk.getTypeChunks(typeSpecChunk.id)
            typeChunks
                .filter { it.configuration.toString() == config }
                .flatMap { typeChunk ->
                    typeChunk.entries.values
                        .asSequence()
                        .filter { name == it.key() }
                        .mapNotNull { typeEntry ->
                            val value: BinaryResourceValue? = typeEntry.value()
                            if (value != null) {
                                formatValue(value, stringPoolChunk)
                            } else {
                                typeEntry.values()
                                    ?.values
                                    ?.joinToString(", ") { formatValue(it, stringPoolChunk) }
                            }
                        }
                        .take(1)
                }
                .firstOrNull()
        }
    }

    fun getDexCode(
        classFqcn: String = "com.example.samplefbase.config.FirebaseOptionsKt",
        methodSignature: String? = "<clinit>()V",
    ): String? {
        return Archives.open(path.toPath()).use { archiveContext ->
            archiveContext.archive.contentRoot.useDirectoryEntries("*.dex") { dexPaths ->
                dexPaths.firstNotNullOfOrNull {
                    getDexCodeOrNull(it, classFqcn, methodSignature)
                }
            }
        }
    }

    @Suppress("SwallowedException")
    private fun getDexCodeOrNull(
        dexPath: Path,
        classFqcn: String,
        methodSignature: String?,
    ): String? {
        val disassembler = DexDisassembler(DexFiles.getDexFile(dexPath), null)
        if (methodSignature == null) {
            try {
                return disassembler.disassembleClass(classFqcn)
            } catch (e: IllegalStateException) {
                // this dex file doesn't contain the given class.
                // continue searching
            }
        } else {
            try {
                val originalFqcn = PackageTreeCreator.decodeClassName(SigUtils.typeToSignature(classFqcn), null)
                return disassembler.disassembleMethod(
                    classFqcn,
                    SigUtils.typeToSignature(originalFqcn) + "->" + methodSignature,
                )
            } catch (e: IllegalStateException) {
                // this dex file doesn't contain the given method.
                // continue searching
            }
        }
        return null
    }
}
