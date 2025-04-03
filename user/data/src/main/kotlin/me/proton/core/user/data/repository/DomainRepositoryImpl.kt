/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.user.data.repository

import io.github.reactivecircus.cache4k.Cache
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.data.api.DomainApi
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.util.kotlin.coroutine.result
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

@Singleton
class DomainRepositoryImpl @Inject constructor(
    private val provider: ApiProvider
) : DomainRepository {

    private val cache = Cache.Builder<Unit, List<Domain>>().expireAfterWrite(1.minutes).build()

    override suspend fun getAvailableDomains(sessionUserId: UserId?): List<Domain> =
        result("getAvailableDomains") {
            cache.get(Unit) {
                provider.get<DomainApi>(sessionUserId).invoke {
                    getAvailableDomains().domains
                }.valueOrThrow
            }
        }
}
