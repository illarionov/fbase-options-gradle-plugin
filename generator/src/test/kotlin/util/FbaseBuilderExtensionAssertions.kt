/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.util

import assertk.Assert
import assertk.assertions.prop
import ru.pixnews.gradle.fbase.FbaseBuilderExtension

fun Assert<FbaseBuilderExtension>.targetPackage() = prop(FbaseBuilderExtension::targetPackage)
fun Assert<FbaseBuilderExtension>.targetFileName() = prop(FbaseBuilderExtension::targetFileName)
fun Assert<FbaseBuilderExtension>.propertyName() = prop(FbaseBuilderExtension::propertyName)
fun Assert<FbaseBuilderExtension>.visibility() = prop(FbaseBuilderExtension::visibility)
fun Assert<FbaseBuilderExtension>.nameProp() = prop(FbaseBuilderExtension::getName)
