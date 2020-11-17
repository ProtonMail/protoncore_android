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

package me.proton.core.auth.domain.entity

/**
 * @author Dino Kadrikj.
 */
data class Addresses(
    val addresses: List<Address>
) {
    /**
     * Checks if all addresses are of type [AddressType.EXTERNAL].
     * This is useful when determining the [AccountType]. `true` for [AccountType.External].
     */
    val allExternal = addresses.all {
        it.type == AddressType.EXTERNAL
    }

    /**
     * Returns `true` if the account is of type [AccountType.Username].
     */
    val usernameOnly = addresses.isEmpty()

    /**
     * Client supplies the minimal [accountType] it needs to operate. The result is if current account satisfies the
     * required account.
     */
    fun satisfiesAccountType(accountType: AccountType): Boolean {
        return when (accountType) {
            // if client needs Username account, then it should be fie with any account type
            AccountType.Username -> true
            // if client needs External account, we return true only if current account is External or Internal
            AccountType.External -> !usernameOnly
            // if client needs Internal only account to operate, we return true if current account is Internal only
            AccountType.Internal -> !usernameOnly && !allExternal
        }
    }

    /**
     * Determines and returns current [AccountType].
     */
    fun currentAccountType(): AccountType =
        when {
            usernameOnly -> { AccountType.Username }
            allExternal -> { AccountType.External }
            else -> { AccountType.Internal }
        }
}
