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

package me.proton.core.conventionalcommits

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConventionalCommitParserTest {
    @Test
    fun `type and message`() {
        val commit = ConventionalCommitParser.parse("fix: message")
        assertEquals("fix", commit?.type)
        assertEquals("message", commit?.description)
    }

    @Test
    fun `non-conventional message`() {
        val commit = ConventionalCommitParser.parse("fix bugs: message")
        assertNull(commit)
    }

    @Test
    fun `breaking commit`() {
        val commit = ConventionalCommitParser.parse("fix!: message")
        assertEquals("fix", commit?.type)
        assertEquals("message", commit?.description)
        assertTrue(commit?.breaking == true)
    }

    @Test
    fun `breaking commit with scope`() {
        val commit = ConventionalCommitParser.parse("fix(auth)!: message")
        assertEquals("fix", commit?.type)
        assertEquals("auth", commit?.scope)
        assertEquals("message", commit?.description)
        assertTrue(commit?.breaking == true)
    }

    @Test
    fun `mistyped breaking commit`() {
        val commit = ConventionalCommitParser.parse("fix!(auth): message")
        assertNull(commit)
    }

    @Test
    fun `breaking commit footer`() {
        val commit = ConventionalCommitParser.parse("fix: message\n\nBREAKING CHANGE: Value")
        assertEquals("fix", commit?.type)
        assertEquals("message", commit?.description)
        assertContentEquals(listOf(CommitFooter("BREAKING CHANGE", "Value")), commit?.footers)
        assertTrue(commit?.breaking == true)
    }

    @Test
    fun `breaking commit footer alternative`() {
        val commit = ConventionalCommitParser.parse("fix: message\n\nBREAKING-CHANGE: Value")
        assertEquals("fix", commit?.type)
        assertEquals("message", commit?.description)
        assertContentEquals(listOf(CommitFooter("BREAKING-CHANGE", "Value")), commit?.footers)
        assertTrue(commit?.breaking == true)
    }

    @Test
    fun `type, scope and message`() {
        val commit = ConventionalCommitParser.parse("fix(core): message")
        assertEquals("fix", commit?.type)
        assertEquals("core", commit?.scope)
        assertEquals("message", commit?.description)
    }

    @Test
    fun `multiple scopes`() {
        val commit = ConventionalCommitParser.parse("fix(account, auth): message")
        assertEquals("fix", commit?.type)
        assertEquals("account, auth", commit?.scope)
        assertEquals("message", commit?.description)
    }

    @Test
    fun `single-line body`() {
        val commit = ConventionalCommitParser.parse("fix(core): message\n\nbody")
        assertEquals("body", commit?.body)
    }

    @Test
    fun `multi-line body`() {
        val commit = ConventionalCommitParser.parse("fix(core): message\n\nbody\nsecond line\n\nend")
        assertEquals("body\nsecond line\n\nend", commit?.body)
    }

    @Test
    fun `single footer`() {
        val commit = ConventionalCommitParser.parse("fix(core): message\n\nFooter: value")
        assertContentEquals(listOf(CommitFooter("Footer", "value")), commit?.footers, "Parsed $commit")
    }

    @Test
    fun `multi-line footer`() {
        val commit = ConventionalCommitParser.parse("fix(core): message\n\nFooter: value\ncontinued")
        assertContentEquals(listOf(CommitFooter("Footer", "value\ncontinued")), commit?.footers, "Parsed $commit")
    }

    @Test
    fun `multiple footers`() {
        val commit = ConventionalCommitParser.parse("fix(core): message\n\nFooterA: a\n\nBREAKING CHANGE: b")
        assertContentEquals(
            listOf(CommitFooter("FooterA", "a"), CommitFooter("BREAKING CHANGE", "b")),
            commit?.footers, "Parsed: $commit"
        )
    }

    @Test
    fun `body with footer`() {
        val commit = ConventionalCommitParser.parse("fix(core): message\n\nBody message\n\nFooter: value")
        assertEquals("Body message", commit?.body, "Parsed: $commit")
        assertContentEquals(listOf(CommitFooter("Footer", "value")), commit?.footers, "Parsed: $commit")
    }

    @Test
    fun `body that looks like footer`() {
        val commit = ConventionalCommitParser.parse("fix(core): message\n\nBody message: test")
        assertEquals("Body message: test", commit?.body, "Parsed: $commit")
    }

    @Test
    fun `multi-line body with footers`() {
        val commit =
            ConventionalCommitParser.parse("fix(core): message\n\nline1\nline2\n\nFooter: value\nBREAKING-CHANGE: value2")
        assertEquals("line1\nline2", commit?.body, "Parsed: $commit")
        assertContentEquals(
            listOf(CommitFooter("Footer", "value"), CommitFooter("BREAKING-CHANGE", "value2")),
            commit?.footers, "Parsed: $commit"
        )
    }

    @Test
    fun `multi-line body with multi-line footers`() {
        val commit =
            ConventionalCommitParser.parse("fix(core): message\n\nline1\nline2\nline3\n\nFooter-a: a\nFooter-b: b\ncontinued\nvalue")
        assertEquals("line1\nline2\nline3", commit?.body, "Parsed: $commit")
        assertContentEquals(
            listOf(CommitFooter("Footer-a", "a"), CommitFooter("Footer-b", "b\ncontinued\nvalue")),
            commit?.footers,
            "Parsed: $commit"
        )
    }
}