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

import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.user.domain.entity.Role
import me.proton.core.user.domain.entity.UserType
import me.proton.core.user.domain.extension.originalOrNull
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

class SetupAccountCheck @Inject constructor(
    private val userRepository: UserRepository,
    private val addressRepository: UserAddressRepository,
    private val sessionProvider: SessionProvider
) {

    sealed class Result {
        /** No setup needed. User can now be unlocked. */
        object NoSetupNeeded : Result()

        /** Setup primary keys, using existing username. */
        object SetupPrimaryKeysNeeded : Result()

        /** Setup a new original address, using existing username. */
        object SetupOriginalAddressNeeded : Result()

        /** Choose a username - user interaction needed. */
        object ChooseUsernameNeeded : Result()

        /** User need to first enter 2nd password to proceed. */
        object TwoPassNeeded : Result()

        /** User need to first change password to proceed. */
        object ChangePasswordNeeded : Result()
    }

    suspend operator fun invoke(
        sessionId: String,
        isTwoPassModeNeeded: Boolean,
        requiredUserType: UserType
    ): Result {
        val userId = sessionProvider.getUserId(SessionId(sessionId))
        checkNotNull(userId) { "Cannot get userId from sessionId = $sessionId" }

        val user = userRepository.getUser(userId, refresh = true)
        val hasUsername = !user.name.isNullOrBlank()
        val hasKeys = user.keys.isNotEmpty()

        // Force private OrganizationMember to change password for the first login.
        // We assume, after changing password, user.keys will not be empty (added by web).
        if (!hasKeys && user.role == Role.OrganizationMember && user.private) return Result.ChangePasswordNeeded

        // API bug: TwoPassMode is not needed if user has no key!
        if (isTwoPassModeNeeded && hasKeys) return Result.TwoPassNeeded

        val addresses = addressRepository.getAddresses(userId, refresh = true)
        val hasOriginalAddressKey = addresses.originalOrNull()?.keys?.isNotEmpty() ?: false

        return when (requiredUserType) {
            UserType.Username -> Result.NoSetupNeeded
            UserType.External -> Result.NoSetupNeeded
            UserType.Internal -> when {
                !hasUsername -> Result.ChooseUsernameNeeded
                !hasKeys -> Result.SetupPrimaryKeysNeeded
                !hasOriginalAddressKey -> Result.SetupOriginalAddressNeeded
                else -> Result.NoSetupNeeded
            }
        }
    }
}
