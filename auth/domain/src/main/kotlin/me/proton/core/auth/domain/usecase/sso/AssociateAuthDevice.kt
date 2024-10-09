/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.auth.domain.usecase.sso

import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.DeviceTokenString
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.HttpResponseCodes.HTTP_BAD_REQUEST
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNPROCESSABLE
import me.proton.core.network.domain.ResponseCodes.AUTH_DEVICE_NOT_ACTIVE
import me.proton.core.network.domain.ResponseCodes.AUTH_DEVICE_NOT_FOUND
import me.proton.core.network.domain.ResponseCodes.AUTH_DEVICE_REJECTED
import me.proton.core.network.domain.ResponseCodes.AUTH_DEVICE_TOKEN_INVALID
import me.proton.core.network.domain.ResponseCodes.NOT_ALLOWED
import me.proton.core.network.domain.hasProtonErrorCode
import me.proton.core.network.domain.isHttpError
import javax.inject.Inject

/**
 * Associate Device with user.
 *
 * Return an EncryptedSecret (that can be decrypted with a DeviceSecret).
 */
class AssociateAuthDevice @Inject constructor(
    private val context: CryptoContext,
    private val authDeviceRepository: AuthDeviceRepository,
    private val deviceSecretRepository: DeviceSecretRepository
) {
    suspend operator fun invoke(
        userId: UserId,
        deviceId: AuthDeviceId,
        deviceToken: DeviceTokenString,
    ): Result {
        return try {
            val encryptedSecret = authDeviceRepository.associateDevice(
                userId = userId,
                deviceId = deviceId,
                deviceToken = deviceToken.decrypt(context.keyStoreCrypto)
            )
            Result.Success(encryptedSecret)
        } catch (e: ApiException) {
            e.handleOrThrow(deviceId, userId)
        }
    }

    private suspend fun ApiException.handleOrThrow(
        deviceId: AuthDeviceId,
        userId: UserId
    ): Result.Error = when {
        isHttpError(HTTP_UNPROCESSABLE) -> when {
            hasProtonErrorCode(AUTH_DEVICE_NOT_FOUND) -> onDeviceNotFound(deviceId, userId)
            hasProtonErrorCode(AUTH_DEVICE_NOT_ACTIVE) -> onDeviceNotActive()
            hasProtonErrorCode(AUTH_DEVICE_REJECTED) -> onDeviceRejected(deviceId, userId)
            hasProtonErrorCode(AUTH_DEVICE_TOKEN_INVALID) -> onDeviceTokenInvalid(userId)
            else -> throw this
        }

        isHttpError(HTTP_BAD_REQUEST) && hasProtonErrorCode(NOT_ALLOWED) -> onSessionAlreadyAssociated()
        else -> throw this
    }

    private suspend fun onDeviceNotFound(deviceId: AuthDeviceId, userId: UserId): Result.Error {
        authDeviceRepository.deleteByDeviceId(userId, deviceId)
        return Result.Error.DeviceNotFound
    }

    private fun onDeviceNotActive(): Result.Error {
        return Result.Error.DeviceNotActive
    }

    private suspend fun onDeviceRejected(deviceId: AuthDeviceId, userId: UserId): Result.Error {
        authDeviceRepository.deleteByDeviceId(userId, deviceId)
        return Result.Error.DeviceRejected // Continue by showing the error to the user and logging out.
    }

    private suspend fun onDeviceTokenInvalid(userId: UserId): Result.Error {
        deviceSecretRepository.deleteAll(userId)
        return Result.Error.DeviceTokenInvalid // Continue with CreateAuthDevice
    }

    private fun onSessionAlreadyAssociated(): Result.Error {
        return Result.Error.SessionAlreadyAssociated // Continue the process as if it's successful?
    }

    sealed interface Result {
        sealed interface Error : Result {
            data object DeviceNotFound : Error
            data object DeviceRejected : Error
            data object DeviceNotActive : Error
            data object DeviceTokenInvalid : Error
            data object SessionAlreadyAssociated : Error
        }

        // The caller can continue with DecryptEncryptedSecret:
        // - The FE decrypts the EncryptedSecret with the stored DeviceSecret using plain AES256-GCM.
        // - The FE continues with the normal login flow, decrypting the user keys using the secret.
        data class Success(val encryptedSecret: Based64EncodedAeadEncryptedSecret) : Result
    }
}
