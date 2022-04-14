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

package me.proton.core.conventionalcommits.ext

import me.proton.core.conventionalcommits.ConventionalCommit
import me.proton.core.conventionalcommits.ConventionalCommitParser
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit

internal fun RevCommit.toConventionalCommit(): ConventionalCommit? {
    return ConventionalCommitParser.parse(fullMessage)
}

internal fun Ref.withoutVersionTagPrefix(versionPrefix: String): String {
    return name.removePrefix(Constants.R_TAGS + versionPrefix)
}
