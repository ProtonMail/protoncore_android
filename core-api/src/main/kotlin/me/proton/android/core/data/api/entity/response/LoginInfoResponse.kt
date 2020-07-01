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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.MODULUS
import me.proton.android.core.data.api.Field.SALT
import me.proton.android.core.data.api.Field.SERVER_EPHEMERAL
import me.proton.android.core.data.api.Field.SRP_SESSION
import me.proton.android.core.data.api.Field.TWO_FACTOR
import me.proton.android.core.data.api.Field.VERSION

@Serializable
internal data class LoginInfoResponse(

    @SerialName(MODULUS)
    val modulus: String,

    @SerialName(SERVER_EPHEMERAL)
    val serverEphemeral: String,

    @SerialName(VERSION)
    val authVersion: Int,

    @SerialName(SALT)
    val salt: String,

    @SerialName(SRP_SESSION)
    val srpSession: String,

    @SerialName(TWO_FACTOR)
    val twoFactor: Int
)
