/*
 * Copyright (c) 2021 Proton Technologies AG
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
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

/** Performs the account check after logging in to determine what actions are needed. */
class PostLoginAccountSetup @Inject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val setupAccountCheck: SetupAccountCheck,
    private val setupInternalAddress: SetupInternalAddress,
    private val setupPrimaryKeys: SetupPrimaryKeys,
    private val unlockUserPrimaryKey: UnlockUserPrimaryKey
) {
    sealed class Result {
        sealed class Error : Result() {
            data class CannotUnlockPrimaryKey(val error: UserManager.UnlockResult.Error) : Error()
            data class UserCheckError(val error: SetupAccountCheck.UserCheckResult.Error) : Error()
        }

        sealed class Need : Result() {
            data class SecondFactor(val userId: UserId) : Need()
            data class TwoPassMode(val userId: UserId) : Need()
            data class ChangePassword(val userId: UserId) : Need()
            data class ChooseUsername(val userId: UserId) : Need()
        }

        data class UserUnlocked(val userId: UserId) : Result()
    }

    suspend operator fun invoke(
        sessionInfo: SessionInfo,
        encryptedPassword: EncryptedString,
        requiredAccountType: AccountType
    ): Result {
        val userId = sessionInfo.userId

        // If SecondFactorNeeded, we cannot proceed without.
        if (sessionInfo.isSecondFactorNeeded) {
            return Result.Need.SecondFactor(userId)
        }

        return when (val result = setupAccountCheck(userId, sessionInfo.isTwoPassModeNeeded, requiredAccountType)) {
            is SetupAccountCheck.Result.UserCheckError -> {
                accountWorkflow.handleAccountDisabled(userId)
                Result.Error.UserCheckError(result.error)
            }
            is SetupAccountCheck.Result.TwoPassNeeded -> {
                accountWorkflow.handleTwoPassModeNeeded(userId)
                Result.Need.TwoPassMode(userId)
            }
            is SetupAccountCheck.Result.ChangePasswordNeeded -> {
                accountWorkflow.handleAccountDisabled(userId)
                Result.Need.ChangePassword(userId)
            }
            is SetupAccountCheck.Result.ChooseUsernameNeeded -> {
                accountWorkflow.handleCreateAddressNeeded(userId)
                Result.Need.ChooseUsername(userId)
            }
            is SetupAccountCheck.Result.SetupPrimaryKeysNeeded -> {
                setupPrimaryKeys.invoke(userId, encryptedPassword, requiredAccountType)
                unlockUserPrimaryKey(userId, encryptedPassword)
            }
            is SetupAccountCheck.Result.SetupInternalAddressNeeded -> unlockUserPrimaryKey(
                userId,
                encryptedPassword,
                withInternalAddressSetup = true
            )
            is SetupAccountCheck.Result.NoSetupNeeded -> unlockUserPrimaryKey(userId, encryptedPassword)
        }
    }

    private suspend fun unlockUserPrimaryKey(
        userId: UserId,
        password: EncryptedString,
        withInternalAddressSetup: Boolean = false
    ): Result {
        return when (val result = unlockUserPrimaryKey.invoke(userId, password)) {
            is UserManager.UnlockResult.Success -> {
                if (withInternalAddressSetup) setupInternalAddress.invoke(userId)
                accountWorkflow.handleAccountReady(userId)
                Result.UserUnlocked(userId)
            }
            is UserManager.UnlockResult.Error -> {
                accountWorkflow.handleUnlockFailed(userId)
                Result.Error.CannotUnlockPrimaryKey(result)
            }
        }
    }
}
