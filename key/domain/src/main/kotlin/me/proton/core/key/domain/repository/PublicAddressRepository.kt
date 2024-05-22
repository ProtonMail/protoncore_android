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
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicAddressInfo
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

@ExcludeFromCoverage // excluded because of the default value in getPublicAddress. we do not test interfaces.
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
    @Deprecated(
        "Deprecated on BE.",
        ReplaceWith("getPublicKeysInfo(sessionUserId, email, internalOnly = TODO(), source)")
    )
    suspend fun getPublicAddress(
        sessionUserId: SessionUserId,
        email: String,
        source: Source = Source.RemoteNoCache,
    ): PublicAddress

    suspend fun getPublicAddressInfo(
        sessionUserId: SessionUserId,
        email: String,
        internalOnly: Boolean = true,
        source: Source = Source.RemoteNoCache
    ): PublicAddressInfo

    /**
     * Get signed key lists published for [email] after [epochId], using [userId].
     *
     * @param userId: the id of the user
     * @param epochId: the id of the epoch after which to fetch new SKLs
     * @param email: the email for which to fetch new SKLs
     *
     * @return the list of signed key lists
     */
    suspend fun getSKLsAfterEpoch(
        userId: UserId,
        epochId: Int,
        email: String
    ): List<PublicSignedKeyList>


    /**
     * Get signed key lists published for [email] at [epochId], using [userId].
     *
     * @param userId: the id of the user
     * @param epochId: the id of the epoch at which to fetch
     * @param email: the email of the SKL to fetch
     *
     * @return the signed key list
     */
    suspend fun getSKLAtEpoch(
        userId: UserId,
        epochId: Int,
        email: String
    ): PublicSignedKeyList

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
@Deprecated(
    "Deprecated on BE.",
    ReplaceWith("getPublicKeysInfoOrNull(sessionUserId, email, internalOnly = TODO(), source)")
)
suspend fun PublicAddressRepository.getPublicAddressOrNull(
    sessionUserId: SessionUserId,
    email: String,
    source: Source = Source.RemoteNoCache,
): PublicAddress? = runCatching {
    getPublicAddress(sessionUserId, email, source)
}.getOrNull()

suspend fun PublicAddressRepository.getPublicKeysInfoOrNull(
    sessionUserId: SessionUserId,
    email: String,
    internalOnly: Boolean = true,
    source: Source = Source.RemoteNoCache
): PublicAddressInfo? = runCatching {
    getPublicAddressInfo(sessionUserId, email, internalOnly, source)
}.getOrNull()

enum class Source { LocalIfAvailable, RemoteOrCached, RemoteNoCache }
