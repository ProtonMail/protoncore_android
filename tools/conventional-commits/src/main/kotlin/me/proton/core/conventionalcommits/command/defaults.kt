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

internal val defaultAllowedTypes =
    listOf("build", "chore", "ci", "docs", "feat", "fix", "perf", "refactor", "revert", "style", "test", "theme")

/** Commits with these types, will trigger a minor version bump. */
internal val defaultMinorTypes = listOf("feat")

/** These types won't be used when generating the changelog. */
internal val defaultChangelogSkipTypes = listOf("build", "ci", "docs", "style", "test")
