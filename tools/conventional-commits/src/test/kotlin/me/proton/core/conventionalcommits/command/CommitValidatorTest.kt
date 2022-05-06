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

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class CommitValidatorTest {
    private val allowedTypes = listOf("fix", "feat")
    private lateinit var tested: CommitValidator

    @BeforeTest
    fun setUp() {
        tested = CommitValidator(allowedTypes)
    }

    @Test
    fun `not conventional commit`() {
        assertIs<VerificationResult.ParseError>(tested("Not conventional"))
    }

    @Test
    fun `type not allowed`() {
        assertIs<VerificationResult.TypeNotAllowed>(tested("test: Description."))
    }

    @Test
    fun `missing dot`() {
        assertIs<VerificationResult.InvalidDescriptionSentence>(tested("fix: Description"))
    }

    @Test
    fun `missing uppercase`() {
        assertIs<VerificationResult.InvalidDescriptionSentence>(tested("fix: description."))
    }

    @Test
    fun `valid commit`() {
        assertIs<VerificationResult.Success>(tested("feat: Some description."))
    }
}
