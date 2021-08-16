/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.user.data.repository

import me.proton.core.network.data.ApiProvider
import me.proton.core.user.data.DefaultDomainHost
import me.proton.core.user.data.api.DomainApi
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.repository.DomainRepository

class DomainRepositoryImpl(
    @DefaultDomainHost private val defaultDomain: Domain,
    private val provider: ApiProvider
) : DomainRepository {

    override suspend fun getAvailableDomains(): List<Domain> {
        val domains = provider.get<DomainApi>().invoke {
            getAvailableDomains().domains
        }.valueOrNull
        return when {
            // Fallback to default domain.
            domains.isNullOrEmpty() -> listOf(defaultDomain)
            else -> (domains.toSet() + setOf(defaultDomain)).toList()
        }
    }
}
