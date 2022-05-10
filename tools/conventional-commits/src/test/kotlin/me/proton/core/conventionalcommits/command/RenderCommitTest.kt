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

import me.proton.core.conventionalcommits.CommitFooter
import me.proton.core.conventionalcommits.ConventionalCommit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RenderCommitTest {
    private lateinit var tested: ChangelogRenderer

    @BeforeTest
    fun setUp() {
        tested = ChangelogRenderer()
    }

    @Test
    fun `render simple commit`() {
        val commit = ConventionalCommit("fix", emptyList(), "fixed bugs", "", emptyList(), false)
        assertEquals(
            "- fixed bugs",
            tested.renderCommit(commit)
        )
    }

    @Test
    fun `render simple commit with indent`() {
        val commit = ConventionalCommit("fix", emptyList(), "fixed bugs", "", emptyList(), false)
        assertEquals(
            "  - fixed bugs",
            tested.renderCommit(commit, indentLevel = 1U)
        )
    }

    @Test
    fun `render commit with scope`() {
        val commit = ConventionalCommit("fix", listOf("auth"), "fixed login", "", emptyList(), false)
        assertEquals(
            "- fixed login",
            tested.renderCommit(commit)
        )
    }

    @Test
    fun `render commit with body`() {
        val commit = ConventionalCommit("fix", emptyList(), "fixed login", "Body", emptyList(), false)
        assertEquals(
            """
                - fixed login
                
                  Body
            """.trimIndent(),
            tested.renderCommit(commit)
        )
    }

    @Test
    fun `render commit with multi-line body`() {
        val commit = ConventionalCommit("fix", emptyList(), "fixed login", "Line1\nLine2", emptyList(), false)
        assertEquals(
            """
                - fixed login
                
                  Line1
                  Line2
            """.trimIndent(),
            tested.renderCommit(commit)
        )
    }

    @Test
    fun `render commit with footer`() {
        val commit = ConventionalCommit("fix", emptyList(), "fixed bugs", "", listOf(CommitFooter("Author", "Me")), false)
        assertEquals(
            """
                - fixed bugs
                
                  Author: Me
            """.trimIndent(),
            tested.renderCommit(commit)
        )
    }

    @Test
    fun `render commit with multi-line footer`() {
        val commit = ConventionalCommit(
            "fix", emptyList(), "fixed bugs", "",
            listOf(CommitFooter("Author", "Me\nOthers")), false
        )
        assertEquals(
            """
                - fixed bugs
                
                  Author: Me
                  Others
            """.trimIndent(),
            tested.renderCommit(commit)
        )
    }

    @Test
    fun `render commit with multiple footers`() {
        val commit = ConventionalCommit(
            "fix",
            emptyList(),
            "fixed bugs",
            "",
            listOf(CommitFooter("Author", "Me"), CommitFooter("Commit", "123abcde")),
            false
        )
        assertEquals(
            """
                - fixed bugs
                
                  Author: Me
                  Commit: 123abcde
            """.trimIndent(),
            tested.renderCommit(commit)
        )
    }

    @Test
    fun `render commit with breaking change`() {
        val commit = ConventionalCommit("fix", emptyList(), "fixed bugs", "", emptyList(), true)
        assertEquals(
            "- fixed bugs",
            tested.renderCommit(commit)
        )
    }

    @Test
    fun `render complex commit`() {
        val commit = ConventionalCommit(
            "fix",
            listOf("auth"),
            "fixed bugs",
            "Line1\nLine2",
            listOf(CommitFooter("Author", "Me\nand Others"), CommitFooter("Commit", "123abcde")),
            true
        )
        assertEquals(
            """
                - fixed bugs
                
                  Line1
                  Line2
                
                  Author: Me
                  and Others
                  Commit: 123abcde
            """.trimIndent(),
            tested.renderCommit(commit)
        )
    }
}
