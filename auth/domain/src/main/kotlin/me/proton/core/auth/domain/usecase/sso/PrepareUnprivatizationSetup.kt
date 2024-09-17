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
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.getPublicKeysInfoOrNull
import javax.inject.Inject

/**
 * Called as a first step in the unprivatization flow for first time login SSO users.
 * The user needs to be unprivatized so that org Admin has access.
 *
 * After successful verification the user needs to enter it's backup password and confirm joining the organization.
 * [PostUnprivatiozationSetup] needs to be called afterwards.
 */
class PrepareUnprivatizationSetup @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val authDeviceRepository: AuthDeviceRepository,
    private val publicAddressRepository: PublicAddressRepository
) {

    sealed interface Result {
        object UnprivatizeStateError : Result
        object PublicAddressKeysError : Result
        object VerificationError : Result
        data class UnprivatizeUserSuccess(
            val organizationPublicKey: Armored
        ) : Result
    }

    suspend operator fun invoke(userId: UserId): Result {
        // 1. Fetch the route GET /core/v4/members/me/unprivatize to get the data
        val unprivatizationInfo = authDeviceRepository.getUnprivatizationInfo(userId)
        if (unprivatizationInfo.state != UnprivatizeState.Pending) {
            return Result.UnprivatizeStateError
        }

        // 2. Fetch the keys from GET /keys/all for the AdminEmail.
        val publicAddressKeys =
            publicAddressRepository.getPublicKeysInfoOrNull(userId, email = unprivatizationInfo.adminEmail)
        // 3. If unable, or the key is not in the Address part (i.e. in KT) error out. // which address?
        if (publicAddressKeys == null || publicAddressKeys.address.keys.any { it.email == unprivatizationInfo.adminEmail }) {
            return Result.PublicAddressKeysError
        }

        // 4. Verify the fingerprint of the OrgPublicKey using the OrgKeyFingerprintSignature and the fetched admin address key. If the signature fails, error out
        val orgPublicKey = unprivatizationInfo.orgPublicKey
        val orgPublicKeySignature = unprivatizationInfo.orgKeyFingerprintSignature
        val publicKey = publicAddressKeys.address.keys.firstOrNull { it.email == unprivatizationInfo.adminEmail }
        if (publicKey == null) {
            return Result.PublicAddressKeysError
        }
        val verificationResult = cryptoContext.pgpCrypto.verifyData(
            data = orgPublicKey.toByteArray(Charsets.UTF_8),
            signature = orgPublicKeySignature,
            publicKey = publicKey.publicKey.key
        )

        // 5. Display the AdminEmail to the user, and prompt them to confirm that they have been invited
        // to the org by the admin. If the user cancels, abort the process.
        return if (verificationResult) {
            Result.UnprivatizeUserSuccess(orgPublicKey)
        } else {
            Result.VerificationError
        }
    }
}