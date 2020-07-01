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

package me.proton.android.core.data.api.entity.response

import me.proton.android.core.data.api.entity.HasAccessToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.ACCESS_TOKEN
import me.proton.android.core.data.api.Field.EXPIRES_IN
import me.proton.android.core.data.api.Field.PRIVATE_KEY
import me.proton.android.core.data.api.Field.REFRESH_TOKEN
import me.proton.android.core.data.api.Field.SCOPE
import me.proton.android.core.data.api.Field.TOKEN_TYPE
import me.proton.android.core.data.api.Field.USER_STATUS

/**
 * Created by dinokadrikj on 4/16/20.
 */
@Serializable
data class RefreshResponse(
    @SerialName(ACCESS_TOKEN)
    override val accessToken: String? = null,

    @SerialName(EXPIRES_IN)
    private val expiresIn: Long = 0,

    @SerialName(TOKEN_TYPE)
    private val tokenType: String? = null,

    @SerialName(SCOPE)
    val scope: String? = null,

    @SerialName(USER_STATUS)
    private val userStatus: Int = 0,

    @SerialName(PRIVATE_KEY)
    val privateKey: String? = null,

    @SerialName(REFRESH_TOKEN)
    val refreshToken: String? = null
): HasAccessToken
