/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.test.quark.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.test.quark.data.User
import me.proton.core.util.kotlin.EMPTY_STRING

@Serializable
public data class CreateUserQuarkResponse(
    @SerialName("ID")
    val userId: String,

    @SerialName("Dec_ID")
    val decryptedUserId: Long,

    @SerialName("Name")
    val name: String?,

    @SerialName("Password")
    val password: String,

    @SerialName("Status")
    val status: Int,

    @SerialName("Recovery")
    val recovery: String,

    @SerialName("RecoveryPhone")
    val recoveryPhone: String,

    @SerialName("AuthVersion")
    val authVersion: Int,

    @SerialName("Email")
    val email: String? = null,

    @SerialName("AddressID")
    val addressID: String? = null,

    @SerialName("StatusInfo")
    val statusInfo: String
)
