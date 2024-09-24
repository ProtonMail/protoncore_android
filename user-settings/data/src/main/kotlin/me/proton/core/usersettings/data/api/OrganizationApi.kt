/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.usersettings.data.api

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.usersettings.data.api.response.OrganizationKeysResponse
import me.proton.core.usersettings.data.api.response.OrganizationSettingsResponse
import me.proton.core.usersettings.data.api.response.OrganizationSignatureResponse
import me.proton.core.usersettings.data.api.response.SingleOrganizationResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface OrganizationApi : BaseRetrofitApi {
    @GET("core/v4/organizations")
    suspend fun getOrganization(): SingleOrganizationResponse

    @GET("core/v4/organizations/keys")
    suspend fun getOrganizationKeys(): OrganizationKeysResponse

    @GET("core/v4/organizations/keys/signature")
    suspend fun getOrganizationSignature(): OrganizationSignatureResponse

    @GET("core/v4/organizations/settings")
    suspend fun getOrganizationSettings(): OrganizationSettingsResponse

    @Streaming
    @GET("core/v4/organizations/logo/{logoId}")
    suspend fun getOrganizationLogo(@Path("logoId") logoId: String): ResponseBody
}
