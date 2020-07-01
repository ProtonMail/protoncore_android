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

import me.proton.android.core.data.api.ProtonAuthConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.CLIENT_EPHEMERAL
import me.proton.android.core.data.api.Field.CLIENT_PROOF
import me.proton.android.core.data.api.Field.CLIENT_SECRET
import me.proton.android.core.data.api.Field.SRP_SESSION
import me.proton.android.core.data.api.Field.TWO_FACTOR_CODE
import me.proton.android.core.data.api.Field.USERNAME

@Serializable
internal class LoginBody(

    @SerialName(USERNAME)
    private val username: String,

    @SerialName(SRP_SESSION)
    override val srpSession: String,

    @SerialName(CLIENT_EPHEMERAL)
    override val clientEphemeral: String,

    @SerialName(CLIENT_PROOF)
    override val clientProof: String,

    @SerialName(TWO_FACTOR_CODE)
    override val twoFactorCode: String,

    @SerialName(CLIENT_SECRET)
    private val clientSecret: String = ProtonAuthConfig.clientSecret

) : SrpRequestBody
