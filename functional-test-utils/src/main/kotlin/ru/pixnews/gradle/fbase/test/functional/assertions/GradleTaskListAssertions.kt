/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.assertions

import assertk.Assert
import assertk.assertions.isNotNull
import assertk.assertions.prop
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.TaskOutcome

public fun Assert<BuildResult>.outcomeOfTask(taskName: String): Assert<TaskOutcome> = prop(BuildResult::getTasks)
    .outcomeOf(taskName)

public fun Assert<BuildResult>.resultOfTask(taskName: String): Assert<BuildTask> = prop(BuildResult::getTasks)
    .resultOf(taskName)

public fun Assert<List<BuildTask>>.outcomeOf(taskName: String): Assert<TaskOutcome> = resultOf(taskName)
    .prop(BuildTask::getOutcome)

public fun Assert<List<BuildTask>>.resultOf(taskName: String): Assert<BuildTask> = this.transform { tasks ->
    tasks.singleOrNull { it.path == taskName }
}.isNotNull()
