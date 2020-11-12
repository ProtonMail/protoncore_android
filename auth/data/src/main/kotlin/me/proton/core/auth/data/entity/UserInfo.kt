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

package me.proton.core.auth.data.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.auth.domain.entity.User
import me.proton.core.util.kotlin.toBoolean

@Serializable
data class UserInfo(
    @SerialName("ID")
    val id: String,
    @SerialName("Name")
    val name: String?,
    @SerialName("UsedSpace")
    val usedSpace: Long,
    @SerialName("Currency")
    val currency: String,
    @SerialName("Credit")
    val credit: Int,
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
    val services: Int, // boolean
    @SerialName("Delinquent")
    val delinquent: Int, // boolean
    @SerialName("Email")
    val email: String? = null,
    @SerialName("DisplayName")
    val displayName: String?,
    @SerialName("Keys")
    val keys: List<UserKeyInfo>
) {

    fun toUser(): User = User(
        id = id,
        name = name,
        usedSpace = usedSpace,
        currency = currency,
        credit = credit,
        maxSpace = maxSpace,
        maxUpload = maxUpload,
        role = role,
        private = private.toBoolean(),
        subscribed = subscribed.toBoolean(),
        delinquent = delinquent,
        email = email,
        displayName = displayName,
        keys = keys.map {
            it.toUserKey()
        }
    )
}
