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
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import java.lang.module.ModuleDescriptor

internal class GetVersionTags(private val repo: Repository, private val versionPrefix: String) {
    operator fun invoke(): List<Ref> {
        val comparator = Comparator<Ref> { o1, o2 ->
            val v1 = ModuleDescriptor.Version.parse(o1.withoutVersionTagPrefix(versionPrefix))
            val v2 = ModuleDescriptor.Version.parse(o2.withoutVersionTagPrefix(versionPrefix))
            v1.compareTo(v2)
        }
        return repo.refDatabase
            .getRefsByPrefix(Constants.R_TAGS + versionPrefix)
            .sortedWith(comparator.reversed())
    }
}
