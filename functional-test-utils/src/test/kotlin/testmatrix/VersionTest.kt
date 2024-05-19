/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.gradle.fbase.test.functional.testmatrix

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isLessThan
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VersionTest {
    @Test
    fun `Parsing versions works correctly`() {
        assertThrows<IllegalArgumentException> { Version.parse("12") }
        assertThat(Version.parse("12.451")).isEqualTo(Version(12, 451))
        assertThat(Version.parse("12.451-rc-3")).isEqualTo(Version(12, 451, null, "rc-3"))
        assertThat(Version.parse("12.451.6")).isEqualTo(Version(12, 451, 6))
        assertThat(Version.parse("12.451.6-beta05")).isEqualTo(Version(12, 451, 6, "beta05"))
        assertThrows<IllegalArgumentException> { Version.parse("12.451.6.4-beta05") }
    }

    @Test
    fun `Base versions are generated correctly`() {
        assertThat(Version(12, 451, 6, "beta05").baseVersion()).isEqualTo(Version(12, 451, 6))
    }

    @Test
    fun `Comparing versions works correctly`() {
        assertThat(Version(12, 5)).isGreaterThan(Version(12, 4))
        assertThat(Version(12, 5, 1)).isGreaterThan(Version(12, 5))
        assertThat(Version(12, 3, 17)).isLessThan(Version(12, 4))
        assertThat(Version(12, 3, 17, "alpha03")).isLessThan(Version(12, 3, 17))
    }

    @Test
    fun `String representation works correctly`() {
        assertThat(Version(12, 451).toString()).isEqualTo("12.451")
        assertThat(Version(12, 451, 6).toString()).isEqualTo("12.451.6")
        assertThat(Version(12, 451, 6, "beta05").toString()).isEqualTo("12.451.6-beta05")
        assertThat(Version(12, 451, null, "beta05").toString()).isEqualTo("12.451-beta05")
    }
}
