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

package me.proton.core.auth.presentation.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import me.proton.core.auth.domain.entity.AccountType
import me.proton.core.auth.domain.entity.User

@Parcelize
data class UserResult(
    val id: String,
    val name: String?,
    val usedSpace: Long,
    val currency: String,
    val credit: Int,
    val maxSpace: Long,
    val maxUpload: Long,
    val role: Int,
    val private: Boolean,
    val subscribed: Int,
    val delinquent: Int,
    val email: String?,
    val displayName: String?,
    val passphrase: ByteArray?,
    val satisfiesAccountType: Boolean
) : Parcelable {

    companion object {
        fun from(user: User, requiredAccountType: AccountType) = UserResult(
            id = user.id,
            name = user.name,
            usedSpace = user.usedSpace,
            currency = user.currency,
            credit = user.credit,
            maxSpace = user.maxSpace,
            maxUpload = user.maxUpload,
            role = user.role,
            private = user.private,
            subscribed = user.subscribed,
            delinquent = user.delinquent,
            email = user.email,
            displayName = user.displayName,
            passphrase = user.passphrase,
            satisfiesAccountType = user.addresses.satisfiesAccountType(requiredAccountType)
        )
    }
}
