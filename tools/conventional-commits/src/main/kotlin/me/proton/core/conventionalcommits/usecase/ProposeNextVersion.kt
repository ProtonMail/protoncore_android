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

package me.proton.core.conventionalcommits.usecase

import me.proton.core.conventionalcommits.ext.withoutVersionTagPrefix
import org.eclipse.jgit.lib.Ref

internal class ProposeNextVersion(
    private val getConventionalCommits: GetConventionalCommits,
    private val minorTypes: List<String>,
    private val versionPrefix: String
) {
    private val initialVersion = "0.1.0"
    private val simpleVersionRegex = Regex("([\\d]+)\\.([\\d]+)\\.([\\d]+).*")

    operator fun invoke(latestTag: Ref?): String {
        latestTag ?: return initialVersion
        val currentVersion = latestTag.withoutVersionTagPrefix(versionPrefix)
        val changes = getConventionalCommits(since = latestTag.objectId).map { it.second }

        var incrementMajor = false
        var incrementMinor = false

        changes.forEach { commit ->
            when {
                commit.breaking -> incrementMajor = true
                commit.type in minorTypes -> incrementMinor = true
            }
        }

        val matchResult = requireNotNull(simpleVersionRegex.matchEntire(currentVersion)) {
            "Current version $currentVersion has invalid format."
        }

        val major = matchResult.groupValues[1].toLong()
        val minor = matchResult.groupValues[2].toLong()
        val patch = matchResult.groupValues[3].toLong()

        return when {
            incrementMajor -> "${major + 1}.0.0"
            incrementMinor -> "$major.${minor + 1}.0"
            else -> "$major.$minor.${patch + 1}"
        }
    }
}
