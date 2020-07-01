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

import me.proton.android.core.data.api.PasswordMode
import me.proton.android.core.data.api.entity.HasAccessToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.ACCESS_TOKEN
import me.proton.android.core.data.api.Field.KEY_SALT
import me.proton.android.core.data.api.Field.PASSWORD_MODE
import me.proton.android.core.data.api.Field.PRIVATE_KEY
import me.proton.android.core.data.api.Field.REFRESH_TOKEN
import me.proton.android.core.data.api.Field.SERVER_PROOF
import me.proton.android.core.data.api.Field.UID

@Serializable
internal class LoginResponse(

    @SerialName(ACCESS_TOKEN)
    override val accessToken: String,

    @SerialName(UID)
    val uid: String,

    @SerialName(REFRESH_TOKEN)
    val refreshToken: String,

    @SerialName(PRIVATE_KEY)
    val privateKey: String,

    /** TODO: Can be null? */
    @SerialName(KEY_SALT)
    val keySalt: String?,

    @SerialName(PASSWORD_MODE)
    val passwordMode: PasswordMode,

    @SerialName(SERVER_PROOF)
    override val serverProof: String

) : HasAccessToken,
    HasServerProof