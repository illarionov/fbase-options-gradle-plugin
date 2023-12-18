/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("IDENTIFIER_LENGTH")

package ru.pixnews.gradle.fbase.util

import assertk.Assert
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import org.gradle.api.provider.Provider

fun <T> Assert<Provider<T>>.isPresent() = prop("isPresent") { p -> p.isPresent }.isTrue()

fun <T> Assert<Provider<T>>.isNotPresent() = prop("isPresent") { p -> p.isPresent }.isFalse()

fun <T> Assert<Provider<T>>.value(): Assert<T> = prop("get") { p -> p.get() }
