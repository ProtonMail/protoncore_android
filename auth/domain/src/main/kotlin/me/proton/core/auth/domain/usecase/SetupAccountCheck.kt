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
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.Role
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.firstInternalOrNull
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

class SetupAccountCheck @Inject constructor(
    private val userRepository: UserRepository,
    private val addressRepository: UserAddressRepository,
    private val userCheck: UserCheck
) {

    sealed class Result {
        /** User check failed, cannot proceed. */
        data class UserCheckError(val error: UserCheckResult.Error) : Result()

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

        /** User need to first change password to proceed. */
        object ChangePasswordNeeded : Result()
    }

    suspend operator fun invoke(
        userId: UserId,
        isTwoPassModeNeeded: Boolean,
        requiredAccountType: AccountType
    ): Result {
        val user = userRepository.getUser(userId, refresh = true)
        val userCheckResult = userCheck.invoke(user)
        if (userCheckResult is UserCheckResult.Error) return Result.UserCheckError(userCheckResult)

        val hasUsername = !user.name.isNullOrBlank()
        val hasKeys = user.keys.isNotEmpty()

        // Force private OrganizationMember to change password for the first login.
        // We assume, after changing password, user.keys will not be empty (added by web).
        if (!hasKeys && user.role == Role.OrganizationMember && user.private) return Result.ChangePasswordNeeded

        // API bug: TwoPassMode is not needed if user has no key!
        if (isTwoPassModeNeeded && hasKeys) return Result.TwoPassNeeded

        val addresses = addressRepository.getAddresses(userId, refresh = true)
        val hasInternalAddressKey = addresses.firstInternalOrNull()?.keys?.isNotEmpty() ?: false

        return when (requiredAccountType) {
            AccountType.Username -> Result.NoSetupNeeded
            AccountType.External -> when {
                !hasKeys -> Result.SetupPrimaryKeysNeeded
                else -> Result.NoSetupNeeded
            }
            AccountType.Internal -> when {
                !hasUsername -> Result.ChooseUsernameNeeded
                !hasKeys -> Result.SetupPrimaryKeysNeeded
                !hasInternalAddressKey -> Result.SetupInternalAddressNeeded
                else -> Result.NoSetupNeeded
            }
        }
    }

    sealed class Action(open val name: String) {
        data class OpenUrl(override val name: String, val url: String) : Action(name)
    }

    sealed class UserCheckResult {
        object Success : UserCheckResult()
        data class Error(val localizedMessage: String, val action: Action? = null) : UserCheckResult()
    }

    interface UserCheck {

        /**
         * Check if [User] match criteria to continue the setup account process.
         */
        suspend operator fun invoke(user: User): UserCheckResult
    }
}
