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

import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.auth.domain.usecase.SetupPrimaryKeys
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

/**
 * Called as a second step in the unprivatization flow for SSO users after the user has entered it's backup password
 * and accepted to join the organization.
 */
class PostUnprivatizationSetup @Inject constructor(
    private val context: CryptoContext,
    private val deviceSecretRepository: DeviceSecretRepository,
    private val setupPrimaryKeys: SetupPrimaryKeys,
    private val getEncryptedSecret: GetEncryptedSecret
) {
    suspend operator fun invoke(
        userId: UserId,
        password: EncryptedString,
        organizationPublicKey: Armored
    ) {
        password.decrypt(context.keyStoreCrypto).toByteArray().use { decryptedPassword ->
            val deviceSecret = requireNotNull(deviceSecretRepository.getByUserId(userId))
            setupPrimaryKeys(
                userId = userId,
                password = password,
                accountType = AccountType.Internal,
                internalDomain = null,
                organizationPublicKey = organizationPublicKey,
                encryptedSecret = getEncryptedSecret(decryptedPassword, deviceSecret.secret)
            )
        }
    }
}