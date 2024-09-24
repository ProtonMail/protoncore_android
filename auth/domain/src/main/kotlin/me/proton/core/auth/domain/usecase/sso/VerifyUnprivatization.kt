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

import me.proton.core.auth.domain.entity.UnprivatizeState
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.VerificationContext
import me.proton.core.crypto.common.pgp.VerificationContext.ContextRequirement
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.publicKeyRing
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.getPublicKeysInfoOrNull
import me.proton.core.key.domain.verifyText
import me.proton.core.util.kotlin.deserialize
import javax.inject.Inject

/**
 * Called as a first step in the unprivatization flow for first time login SSO users.
 * The user needs to be unprivatized so that org Admin has access.
 */
class VerifyUnprivatization @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val authDeviceRepository: AuthDeviceRepository,
    private val publicAddressRepository: PublicAddressRepository
) {

    sealed interface Result {
        object UnprivatizeStateError : Result
        object PublicAddressKeysError : Result
        object VerificationError : Result
        data class UnprivatizeUserSuccess(
            val adminEmail: String,
            val organizationPublicKey: Armored
        ) : Result
    }

    suspend operator fun invoke(userId: UserId): Result {
        // Fetch the route GET /core/v4/members/me/unprivatize to get the data info.
        val unprivatizationInfo = authDeviceRepository.getUnprivatizationInfo(userId)
        if (unprivatizationInfo.state != UnprivatizeState.Pending) {
            return Result.UnprivatizeStateError
        }
        // Fetch the keys from GET /keys/all for the AdminEmail.
        val adminEmail = unprivatizationInfo.adminEmail
        val publicAddress = publicAddressRepository.getPublicKeysInfoOrNull(userId, adminEmail, internalOnly = false)
        // If unable, or the key is not in the Address part (i.e. in KT) error out.
        val publicAddressKey = publicAddress?.address?.keys?.firstOrNull { it.email == adminEmail }
        when  {
            publicAddress == null -> return Result.PublicAddressKeysError
            publicAddress.email != adminEmail -> return Result.PublicAddressKeysError
            publicAddressKey == null -> return Result.PublicAddressKeysError
            else -> Unit
        }
        // Verify the SHA265 fingerprint of the OrgPublicKey using the OrgKeyFingerprintSignature and the fetched admin address key.
        val jsonFingerprint = cryptoContext.pgpCrypto.getJsonSHA256Fingerprints(unprivatizationInfo.orgPublicKey)
        val fingerprint = jsonFingerprint.deserialize<List<String>>().first()
        val verificationResult = publicAddressKey.publicKeyRing().verifyText(
            context = cryptoContext,
            text = fingerprint,
            trimTrailingSpaces = false,
            signature = unprivatizationInfo.orgKeyFingerprintSignature,
            verificationContext = VerificationContext("account.organization-fingerprint", ContextRequirement.Required.Always)
        )
        return if (verificationResult) {
            Result.UnprivatizeUserSuccess(
                adminEmail = adminEmail,
                organizationPublicKey = unprivatizationInfo.orgPublicKey
            )
        } else {
            Result.VerificationError
        }
    }
}
