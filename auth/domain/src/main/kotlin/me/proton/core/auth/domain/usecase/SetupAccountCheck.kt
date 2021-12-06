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

package me.proton.core.auth.domain.usecase

import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.usecase.SetupAccountCheck.Result.ChangePasswordNeeded
import me.proton.core.auth.domain.usecase.SetupAccountCheck.Result.ChooseUsernameNeeded
import me.proton.core.auth.domain.usecase.SetupAccountCheck.Result.NoSetupNeeded
import me.proton.core.auth.domain.usecase.SetupAccountCheck.Result.SetupInternalAddressNeeded
import me.proton.core.auth.domain.usecase.SetupAccountCheck.Result.SetupPrimaryKeysNeeded
import me.proton.core.auth.domain.usecase.SetupAccountCheck.Result.TwoPassNeeded
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.extension.hasInternalAddressKey
import me.proton.core.user.domain.extension.hasKeys
import me.proton.core.user.domain.extension.hasUsername
import me.proton.core.user.domain.extension.isPrivate
import me.proton.core.user.domain.extension.isSubUser
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

class SetupAccountCheck @Inject constructor(
    private val product: Product,
    private val userRepository: UserRepository,
    private val addressRepository: UserAddressRepository
) {

    sealed class Result {
        /** No setup needed. User can now be unlocked. */
        object NoSetupNeeded : Result()

        /** Setup primary keys, using existing username. */
        object SetupPrimaryKeysNeeded : Result()

        /** Setup a new internal address, using existing username. */
        object SetupInternalAddressNeeded : Result()

        /** Choose a username - user interaction needed. */
        object ChooseUsernameNeeded : Result()

        /** User need to first enter 2nd password to proceed. */
        object TwoPassNeeded : Result()

        /** User need to first change password to proceed (currently, via Web). */
        object ChangePasswordNeeded : Result()
    }

    suspend operator fun invoke(
        userId: UserId,
        isTwoPassModeNeeded: Boolean,
        requiredAccountType: AccountType
    ): Result {
        val user = userRepository.getUser(userId, refresh = true)
        return when (requiredAccountType) {
            AccountType.Username -> {
                NoSetupNeeded
            }
            AccountType.External -> {
                when {
                    product == Product.Vpn -> NoSetupNeeded
                    !user.hasKeys() -> SetupPrimaryKeysNeeded
                    isTwoPassModeNeeded -> TwoPassNeeded
                    else -> NoSetupNeeded
                }
            }
            AccountType.Internal -> {
                val addresses = addressRepository.getAddresses(userId, refresh = true)
                when {
                    !user.hasUsername() -> ChooseUsernameNeeded
                    !user.hasKeys() && user.isSubUser() && user.isPrivate() -> ChangePasswordNeeded
                    !user.hasKeys() -> SetupPrimaryKeysNeeded
                    !addresses.hasInternalAddressKey() -> SetupInternalAddressNeeded
                    isTwoPassModeNeeded -> TwoPassNeeded
                    else -> NoSetupNeeded
                }
            }
        }
    }
}
