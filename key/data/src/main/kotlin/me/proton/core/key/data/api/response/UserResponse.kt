/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.key.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsersResponse(
    @SerialName("User")
    val user: UserResponse
)

@Serializable
data class UserResponse(
    @SerialName("ID")
    val id: String,
    @SerialName("Name")
    val name: String? = null,
    @SerialName("UsedSpace")
    val usedSpace: Long,
    @SerialName("Currency")
    val currency: String,
    @SerialName("Credit")
    val credit: Int,
    @SerialName("CreateTime")
    val createTimeSeconds: Long,
    @SerialName("MaxSpace")
    val maxSpace: Long,
    @SerialName("MaxUpload")
    val maxUpload: Long,
    @SerialName("Role")
    val role: Int,
    @SerialName("Private")
    val private: Int, // boolean
    @SerialName("Subscribed")
    val subscribed: Int,
    @SerialName("Services")
    val services: Int,
    @SerialName("Delinquent")
    val delinquent: Int,
    @SerialName("Email")
    val email: String? = null,
    @SerialName("DisplayName")
    val displayName: String? = null,
    @SerialName("AccountRecovery")
    val recovery: UserRecoveryResponse? = null,
    @SerialName("Keys")
    val keys: List<UserKeyResponse>
)
