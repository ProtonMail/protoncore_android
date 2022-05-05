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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RenderPartialChangelogTest {
    private lateinit var tested: ChangelogCommand

    @BeforeTest
    fun setUp() {
        tested = ChangelogCommand()
    }

    @Test
    fun `no changes`() {
        val changes = listOf<ConventionalCommit>()
        assertEquals(
            "## [1.2.3] - $today\n",
            tested.renderPartialChangelog(changes, "1.2.3")
        )
    }

    @Test
    fun `single change`() {
        val changes = listOf(
            ConventionalCommit("fix", "", "My fix.", "", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $today
                
                ### Bug Fixes
                
                - My fix.

            """.trimIndent(),
            tested.renderPartialChangelog(changes, "1.2.3")
        )
    }

    @Test
    fun `multiple changes of the same type`() {
        val changes = listOf(
            ConventionalCommit("fix", "", "My fix.", "", emptyList(), false),
            ConventionalCommit("fix", "", "Another fix.", "Long description.", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $today
                
                ### Bug Fixes
                
                - My fix.
                - Another fix.
                  Long description.

            """.trimIndent(),
            tested.renderPartialChangelog(changes, "1.2.3")
        )
    }

    @Test
    fun `multiple changes of the same type with same scopes`() {
        val changes = listOf(
            ConventionalCommit("fix", "auth", "My fix.", "", emptyList(), false),
            ConventionalCommit("fix", "auth", "Another fix.", "", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $today

                ### Bug Fixes

                - auth:
                  - My fix.
                  - Another fix.

            """.trimIndent(),
            tested.renderPartialChangelog(changes, "1.2.3")
        )
    }

    @Test
    fun `multiple changes of the same type with different scopes`() {
        val changes = listOf(
            ConventionalCommit("fix", "", "My fix.", "", emptyList(), false),
            ConventionalCommit("fix", "data", "Data fix.", "", emptyList(), false),
            ConventionalCommit("fix", "auth", "Another fix.", "Long description.", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $today

                ### Bug Fixes

                - My fix.
                - auth:
                  - Another fix.
                    Long description.
                - data:
                  - Data fix.

            """.trimIndent(),
            tested.renderPartialChangelog(changes, "1.2.3")
        )
    }

    @Test
    fun `multiple changes with different type`() {
        val changes = listOf(
            ConventionalCommit("fix", "", "My fix.", "", emptyList(), false),
            ConventionalCommit("feat", "", "Some feature.", "", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $today
                
                ### Features
                
                - Some feature.
                
                ### Bug Fixes
                
                - My fix.

            """.trimIndent(),
            tested.renderPartialChangelog(changes, "1.2.3")
        )
    }

    @Test
    fun `multiple changes with different type and same scopes`() {
        val changes = listOf(
            ConventionalCommit("fix", "auth", "My fix.", "", emptyList(), false),
            ConventionalCommit("feat", "auth", "Some feature.", "", emptyList(), false)
        )
        assertEquals(
            """
                ## [1.2.3] - $today

                ### Features

                - auth:
                  - Some feature.

                ### Bug Fixes

                - auth:
                  - My fix.

            """.trimIndent(),
            tested.renderPartialChangelog(changes, "1.2.3")
        )
    }

    private val today: String get() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
}
