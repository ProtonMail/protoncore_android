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
import kotlin.test.assertEquals

class UpdateChangelogTest {
    private lateinit var tested: ChangelogCommand

    @BeforeTest
    fun setUp() {
        tested = ChangelogCommand()
    }

    @Test
    fun `update empty changelog`() {
        assertEquals(
            "ABC\n",
            tested.updateChangelog("ABC", "")
        )
    }

    @Test
    fun `update simple changelog`() {
        assertEquals(
            "## [Unreleased]\n\n## 1.2.3\n- Fixes\n",
            tested.updateChangelog("## 1.2.3\n- Fixes", "## [Unreleased]")
        )
    }

    @Test
    fun `update changelog with title and existing version`() {
        assertEquals(
            "# Changelog\n\nABC\n\n## [1.2.3]\n- Fixes\n",
            tested.updateChangelog("ABC", "# Changelog\n\n## [1.2.3]\n- Fixes")
        )
    }

    @Test
    fun `update changelog with unreleased logs`() {
        assertEquals(
            "## [Unreleased]\n\n## 1.2.3\n- Fixes\n\n- feat1\n- feat2\n",
            tested.updateChangelog("## 1.2.3\n- Fixes", "## [Unreleased]\n- feat1\n- feat2")
        )
    }

    @Test
    fun `long changelog`() {
        val originalChangelog = """
            # Changelog
            All notable changes to this project will be documented in this file.

            The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
            and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

            ## [Unreleased]

            ### Fixes

            - Clear consumed Payment Token (HumanVerification token).

            ## [7.1.11]

            ### Changes
            
            When providing the AuthRepository & UserRepository there is a new `Product` parameter that is needed:
        """.trimIndent()

        val newChangelog = """
            # Changelog
            All notable changes to this project will be documented in this file.

            The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
            and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

            ## [Unreleased]
            
            ## [7.1.12]

            ### Fixes

            - Clear consumed Payment Token (HumanVerification token).

            ## [7.1.11]

            ### Changes
            
            When providing the AuthRepository & UserRepository there is a new `Product` parameter that is needed:

        """.trimIndent()

        assertEquals(
            newChangelog,
            tested.updateChangelog("## [7.1.12]", originalChangelog)
        )
    }
}
