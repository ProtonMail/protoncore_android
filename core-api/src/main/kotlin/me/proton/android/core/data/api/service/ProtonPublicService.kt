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

package me.proton.android.core.data.api.service

import me.proton.android.core.data.api.JSON_CONTENT_TYPE
import me.proton.android.core.data.api.PM_ACCEPT_HEADER_V1
import me.proton.android.core.data.api.entity.AuthTag
import me.proton.android.core.data.api.entity.request.RefreshBody
import me.proton.android.core.data.api.entity.response.RefreshResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by dinokadrikj on 4/26/20.
 *
 * This interface should serve as a base interface for the Proton products API. It will implement
 * only the common APIs that are used by all Proton Android products inclusive. The routes that
 * are product dependant should be added as an extension of this interface.
 */
interface ProtonPublicService {

    @POST("auth/refresh")
    @Headers(
        JSON_CONTENT_TYPE,
        PM_ACCEPT_HEADER_V1
    )
    fun refreshSync(@Body refreshBody: RefreshBody, @Tag userAuthTag: AuthTag? = null): Call<RefreshResponse>
}
