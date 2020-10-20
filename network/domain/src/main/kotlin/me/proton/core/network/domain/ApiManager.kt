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
package me.proton.core.network.domain

/**
 * Enables making [Api] calls by the client, along with error handling.
 *
 * @param Api interface that describe the API
 */
interface ApiManager<Api> {

    /**
     * Performs API call.
     *
     * @param T Call result type.
     * @param block Lambda performing the call on [Api] interface instance.
     * @param forceNoRetryOnConnectionErrors if [true] no logic for recovering from connection
     *   errors will be applied ([false] by default)
     * @return
     */
    suspend operator fun <T> invoke(
        forceNoRetryOnConnectionErrors: Boolean = false,
        block: suspend Api.() -> T
    ): ApiResult<T>

    /**
     * Wrapper for the call lambda with contextual info.
     *
     * @param Api interface that describe the API
     * @param T Call result type.
     * @property timestampMs time of the call.
     * @property block Lambda performing the call on [Api] interface instance.
     */
    data class Call<Api, T>(val timestampMs: Long, val block: suspend Api.() -> T)
}
