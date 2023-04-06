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

import io.github.reactivecircus.cache4k.Cache
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.data.api.DomainApi
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.repository.DomainRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

@Singleton
class DomainRepositoryImpl @Inject constructor(
    private val provider: ApiProvider
) : DomainRepository {

    private val cache = Cache.Builder().expireAfterWrite(1.minutes).build<Unit, List<Domain>>()

    override suspend fun getAvailableDomains(): List<Domain> = cache.get(Unit) {
        provider.get<DomainApi>().invoke {
            getAvailableDomains().domains
        }.valueOrThrow
    }
}
