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

package me.proton.android.core.data.api.entity.request

import ch.protonmail.libs.core.HTTP_PROTONMAIL_CH
import kotlinx.serialization.SerialName
import me.proton.android.core.data.api.Field

/**
 * Created by dinokadrikj on 4/26/20.
 */
data class RefreshBody @JvmOverloads constructor(

    @SerialName(Field.REFRESH_TOKEN)
    private val refreshToken: String,

    @SerialName(Field.RESPONSE_TYPE)
    private val responseType: String = "token",

    @SerialName(Field.GRANT_TYPE)
    private val grantType: String = "refresh_token",

    @SerialName(Field.REDIRECT_URI)
    private val redirectURI: String = HTTP_PROTONMAIL_CH
)