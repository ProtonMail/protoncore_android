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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import me.proton.core.conventionalcommits.usecase.GetCommits
import me.proton.core.conventionalcommits.usecase.GetConventionalCommits
import me.proton.core.conventionalcommits.usecase.GetLatestTag
import me.proton.core.conventionalcommits.usecase.GetVersionTags
import me.proton.core.conventionalcommits.usecase.ProposeNextVersion

/** Calculate next version number. */
class NextVersionCommand : CliktCommand() {
    private val minorTypes by option().multiple(default = defaultMinorTypes)
    private val repoDir by repoDirOption().required()
    private val versionPrefix by versionPrefixOption()

    override fun run() {
        val repo = buildRepository(repoDir)
        val latestTag = GetLatestTag(GetVersionTags(repo, versionPrefix)).invoke()
        val getConventionalCommits = GetConventionalCommits(GetCommits(repo))
        val proposedVersion = ProposeNextVersion(
            getConventionalCommits,
            minorTypes,
            versionPrefix
        ).invoke(latestTag)
        echo(proposedVersion)
    }
}
