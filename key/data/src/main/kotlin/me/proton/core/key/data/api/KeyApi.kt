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

package me.proton.core.key.data.api

import me.proton.core.auth.data.api.response.SRPAuthenticationResponse
import me.proton.core.key.data.api.request.CreateAddressKeyRequest
import me.proton.core.key.data.api.request.SetupInitialKeysRequest
import me.proton.core.key.data.api.request.UpdateKeysForPasswordChangeRequest
import me.proton.core.key.data.api.response.CreateAddressKeyResponse
import me.proton.core.key.data.api.response.KeySaltsResponse
import me.proton.core.key.data.api.response.PublicAddressKeysResponse
import me.proton.core.key.data.api.response.SetupInitialKeysResponse
import me.proton.core.key.data.api.response.SingleSignedKeyListResponse
import me.proton.core.key.data.api.response.SignedKeyListsResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.domain.CacheOverride
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Tag

interface KeyApi : BaseRetrofitApi {

    @GET("core/v4/keys/salts")
    suspend fun getSalts(): KeySaltsResponse

    @GET("core/v4/keys")
    suspend fun getPublicAddressKeys(
        @Query("Email") email: String,
        @Tag cacheOverride: CacheOverride? = null
    ): PublicAddressKeysResponse

    @POST("core/v4/keys/address")
    suspend fun createAddressKey(@Body request: CreateAddressKeyRequest): CreateAddressKeyResponse

    @POST("core/v4/keys")
    suspend fun createAddressKeyOld(@Body request: CreateAddressKeyRequest): CreateAddressKeyResponse

    @POST("core/v4/keys/setup")
    suspend fun setupInitialKeys(@Body request: SetupInitialKeysRequest): SetupInitialKeysResponse

    @PUT("core/v4/keys/private")
    suspend fun updatePrivateKeys(@Body request: UpdateKeysForPasswordChangeRequest): SRPAuthenticationResponse

    @GET("core/v4/keys/signedkeylists")
    suspend fun getSKLsAfterEpoch(
        @Query("Email") email: String,
        @Query("AfterEpochID") epochID: Int,
    ): SignedKeyListsResponse

    @GET("core/v4/keys/signedkeylist")
    suspend fun getSKLAtEpoch(
        @Query("Email") email: String,
        @Query("EpochID") epochID: Int,
    ): SingleSignedKeyListResponse
}
