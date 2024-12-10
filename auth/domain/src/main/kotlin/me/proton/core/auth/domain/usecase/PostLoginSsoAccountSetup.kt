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

package me.proton.core.auth.domain.usecase

import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup.Result
import me.proton.core.auth.domain.usecase.sso.CheckDeviceSecret
import me.proton.core.auth.domain.usecase.sso.DecryptEncryptedSecret
import me.proton.core.auth.domain.usecase.sso.VerifyUnprivatization
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.isNotExists
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.UserManager.UnlockResult
import me.proton.core.user.domain.extension.hasTemporaryPassword
import me.proton.core.user.domain.repository.PassphraseRepository
import javax.inject.Inject

/**
 * Performs the account check after SSO logging in to determine what actions are needed.
 */
class PostLoginSsoAccountSetup @Inject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val userCheck: PostLoginAccountSetup.UserCheck,
    private val userManager: UserManager,
    private val sessionManager: SessionManager,
    private val passphraseRepository: PassphraseRepository,
    private val checkDeviceSecret: CheckDeviceSecret,
    private val decryptEncryptedSecret: DecryptEncryptedSecret,
    private val authDeviceRepository: AuthDeviceRepository
) {
    suspend operator fun invoke(userId: UserId): Result {
        // Make sure we have a fresh User in DB.
        val user = userManager.getUser(userId, refresh = true)
        return when {
            user.keys.isEmpty() -> firstLoginCheck(userId)
            else -> secretCheck(userId)
        }
    }

    private suspend fun firstLoginCheck(userId: UserId): Result {
        // Try to get UnprivatizationInfo, if NotExists (2501), fallback on userCheck only.
        val result = runCatching { authDeviceRepository.getUnprivatizationInfo(userId) }
        val exception = result.exceptionOrNull()
        return when {
            exception == null -> secretCheck(userId)
            exception.isNotExists() -> userCheck(userId)
            else -> secretCheck(userId)
        }
    }

    private suspend fun secretCheck(userId: UserId): Result {
        val passphrase = passphraseRepository.getPassphrase(userId)
        return when {
            passphrase != null -> unlockUserWithPassphrase(userId, passphrase)
            else -> {
                val encryptedSecret = checkDeviceSecret.invoke(userId)
                val decryptedSecret = decryptEncryptedSecret.invoke(userId, encryptedSecret)
                return when (decryptedSecret) {
                    null -> deviceSecretNeeded(userId)
                    else -> unlockUserWithPassphrase(userId, decryptedSecret)
                }
            }
        }
    }

    private suspend fun deviceSecretNeeded(userId: UserId): Result {
        accountWorkflow.handleDeviceSecretNeeded(userId)
        return Result.Need.DeviceSecret(userId)
    }

    private suspend fun unlockUserWithPassphrase(
        userId: UserId,
        passphrase: EncryptedByteArray
    ): Result {
        return when (val result = userManager.unlockWithPassphrase(userId, passphrase)) {
            UnlockResult.Error.NoKeySaltsForPrimaryKey -> unrecoverable(userId, result)
            UnlockResult.Error.NoPrimaryKey -> unrecoverable(userId, result)
            UnlockResult.Error.PrimaryKeyInvalidPassphrase -> recoverable(userId, result)
            UnlockResult.Success -> userCheck(userId)
        }
    }

    private suspend fun unrecoverable(userId: UserId, result: UnlockResult): Result {
        accountWorkflow.handleUnlockFailed(userId)
        return Result.Error.UnlockPrimaryKeyError(result as UnlockResult.Error)
    }

    private suspend fun recoverable(userId: UserId, result: UnlockResult): Result {
        accountWorkflow.handleUnlockFailed(userId)
        return Result.Error.UnlockPrimaryKeyError(result as UnlockResult.Error)
    }

    private suspend fun userCheck(userId: UserId): Result {
        // Refresh scopes.
        sessionManager.refreshScopes(checkNotNull(sessionManager.getSessionId(userId)))
        // First get the User to invoke UserCheck.
        val user = userManager.getUser(userId)
        return when (val userCheckResult = userCheck.invoke(user)) {
            is PostLoginAccountSetup.UserCheckResult.Error -> {
                // Disable account and prevent login.
                accountWorkflow.handleAccountDisabled(userId)
                Result.Error.UserCheckError(userCheckResult)
            }

            is PostLoginAccountSetup.UserCheckResult.Success -> {
                when (user.hasTemporaryPassword()) {
                    true -> {
                        accountWorkflow.handleDeviceSecretNeeded(userId)
                        Result.Need.ChangePassword(userId)
                    }
                    false -> {
                        // Last step, change account state to Ready.
                        accountWorkflow.handleAccountReady(userId)
                        Result.AccountReady(userId)
                    }
                }
            }
        }
    }
}
