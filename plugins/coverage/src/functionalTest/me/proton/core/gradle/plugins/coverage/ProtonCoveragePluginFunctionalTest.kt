/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.gradle.plugins.coverage

import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ProtonCoveragePluginFunctionalTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    private lateinit var buildFile: File

    @Before
    fun setUp() {
        buildFile = testProjectDir.newFile("build.gradle.kts")
        buildFile.writeText(
            """
                plugins {
                    id("me.proton.core.gradle-plugins.coverage")
                }
            """.trimIndent()
        )
    }

    @Test
    fun `can configure proton coverage extension`() {
        buildFile.appendText(
            """
                protonCoverage {
                    disabled.set(false)
                    excludes.add {
                        annotatedBy("kotlinx.serialization.Serializable")
                    }
                    minBranchCoveragePercentage.set(74)
                    minLineCoveragePercentage.set(42)
                }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(buildFile.parentFile)
            .withArguments("tasks")
            .withPluginClasspath()
            .build()

        result.output.contains("koverVerify")
        result.output.contains("koverXmlReport")
        result.output.contains("coberturaXmlReport")
        result.output.contains("BUILD SUCCESSFUL")
    }
}
