/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.conventionalcommits.command

import me.proton.core.conventionalcommits.ConventionalCommit
import java.util.Calendar
import java.util.Date
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RenderPartialChangelogTest {
    private val date: Date = Calendar.getInstance().apply { set(2022, 0, 30) }.time
    private val readableDate = "2022-01-30"
    private lateinit var tested: ChangelogRenderer

    @BeforeTest
    fun setUp() {
        tested = ChangelogRenderer()
    }

    @Test
    fun `no changes`() {
        val changes = listOf<ConventionalCommit>()
        assertEquals(
            "## [1.2.3] - $readableDate\n",
            tested(changes, date, "1.2.3")
        )
    }

    @Test
    fun `single change`() {
        val changes = listOf(
            ConventionalCommit("fix", emptyList(), "My fix.", "", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $readableDate
                
                ### Bug Fixes
                
                - My fix.

            """.trimIndent(),
            tested(changes, date, "1.2.3")
        )
    }

    @Test
    fun `single breaking change`() {
        val changes = listOf(
            ConventionalCommit("fix", emptyList(), "My fix.", "", emptyList(), true)
        )
        assertEquals(
            """
                ## [1.2.3] - $readableDate
                
                ### Breaking Changes

                **Bug Fixes**
                
                - My fix.

            """.trimIndent(),
            tested(changes, date, "1.2.3")
        )
    }

    @Test
    fun `non-breaking and breaking change`() {
        val changes = listOf(
            ConventionalCommit("fix", emptyList(), "My non-breaking fix.", "", emptyList(), false),
            ConventionalCommit("fix", emptyList(), "My breaking fix.", "", emptyList(), true)
        )
        assertEquals(
            """
                ## [1.2.3] - $readableDate
                
                ### Breaking Changes

                **Bug Fixes**

                - My breaking fix.

                ### Bug Fixes

                - My non-breaking fix.

            """.trimIndent(),
            tested(changes, date, "1.2.3")
        )
    }

    @Test
    fun `multiple changes of the same type`() {
        val changes = listOf(
            ConventionalCommit("fix", emptyList(), "My fix.", "", emptyList(), false),
            ConventionalCommit("fix", emptyList(), "Another fix.", "Long description.", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $readableDate
                
                ### Bug Fixes
                
                - My fix.
                - Another fix.
                
                  Long description.

            """.trimIndent(),
            tested(changes, date, "1.2.3")
        )
    }

    @Test
    fun `multiple changes of the same type with same scopes`() {
        val changes = listOf(
            ConventionalCommit("fix", listOf("auth"), "My fix.", "", emptyList(), false),
            ConventionalCommit("fix", listOf("auth"), "Another fix.", "", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $readableDate

                ### Bug Fixes

                - auth:
                  - My fix.
                  - Another fix.

            """.trimIndent(),
            tested(changes, date, "1.2.3")
        )
    }

    @Test
    fun `multiple changes of the same type with different scopes`() {
        val changes = listOf(
            ConventionalCommit("fix", emptyList(), "My fix.", "", emptyList(), false),
            ConventionalCommit("fix", listOf("data"), "Data fix.", "", emptyList(), false),
            ConventionalCommit("fix", listOf("auth"), "Another fix.", "Long description.", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $readableDate

                ### Bug Fixes

                - My fix.
                - auth:
                  - Another fix.
                
                    Long description.
                - data:
                  - Data fix.

            """.trimIndent(),
            tested(changes, date, "1.2.3")
        )
    }

    @Test
    fun `multiple changes of the same type with multiple scopes`() {
        val changes = listOf(
            ConventionalCommit("fix", listOf("auth", "data"), "Combined fix.", "", emptyList(), false),
            ConventionalCommit("fix", listOf("auth"), "Auth fix.", "", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $readableDate

                ### Bug Fixes

                - auth:
                  - Combined fix.
                  - Auth fix.
                - data:
                  - Combined fix.

            """.trimIndent(),
            tested(changes, date, "1.2.3")
        )
    }

    @Test
    fun `multiple changes with different type`() {
        val changes = listOf(
            ConventionalCommit("fix", emptyList(), "My fix.", "", emptyList(), false),
            ConventionalCommit("feat", emptyList(), "Some feature.", "", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $readableDate
                
                ### Features
                
                - Some feature.
                
                ### Bug Fixes
                
                - My fix.

            """.trimIndent(),
            tested(changes, date, "1.2.3")
        )
    }

    @Test
    fun `multiple changes with different type and same scopes`() {
        val changes = listOf(
            ConventionalCommit("fix", listOf("auth"), "My fix.", "", emptyList(), false),
            ConventionalCommit("feat", listOf("auth"), "Some feature.", "", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $readableDate

                ### Features

                - auth:
                  - Some feature.

                ### Bug Fixes

                - auth:
                  - My fix.

            """.trimIndent(),
            tested(changes, date, "1.2.3")
        )
    }
}
