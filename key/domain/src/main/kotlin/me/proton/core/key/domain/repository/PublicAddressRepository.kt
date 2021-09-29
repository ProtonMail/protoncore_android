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

package me.proton.core.key.domain.repository

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.key.domain.entity.key.PublicAddress

interface PublicAddressRepository {
    /**
     * Get [PublicAddress], by [email], using [sessionUserId].
     *
     * @return value from cache/disk if [source] is [Source.LocalIfAvailable] - it will fallback to network if it can't
     * find any -, otherwise from fetcher if it's [Source.RemoteOrCached], using [Source.RemoteNoCache] to ask for a
     * guaranteed fresh copy from the remote server.
     *
     * @see [getPublicAddressOrNull]
     */
    suspend fun getPublicAddress(
        sessionUserId: SessionUserId,
        email: String,
        source: Source = Source.RemoteNoCache,
    ): PublicAddress

    /**
     * Clear all persisted [PublicAddress].
     */
    suspend fun clearAll()
}

/**
 * Get [PublicAddress], by [email], using [sessionUserId].
 *
 * @return [PublicAddress] or `null` if it can't be returned for [email].
 *
 * @see [PublicAddressRepository.getPublicAddress]
 */
suspend fun PublicAddressRepository.getPublicAddressOrNull(
    sessionUserId: SessionUserId,
    email: String,
    source: Source = Source.RemoteNoCache,
): PublicAddress? = runCatching {
    getPublicAddress(sessionUserId, email, source)
}.getOrNull()

enum class Source { LocalIfAvailable, RemoteOrCached, RemoteNoCache }
