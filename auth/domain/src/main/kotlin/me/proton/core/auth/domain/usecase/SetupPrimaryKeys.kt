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
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.primary
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.user.domain.entity.emailSplit
import me.proton.core.user.domain.extension.firstInternalOrNull
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import javax.inject.Inject

/**
 * Setup a new primary [UserKey], [UserAddress], and [UserAddressKey].
 */
class SetupPrimaryKeys @Inject constructor(
    private val userManager: UserManager,
    private val userAddressRepository: UserAddressRepository,
    private val authRepository: AuthRepository,
    private val domainRepository: DomainRepository,
    private val srpCrypto: SrpCrypto,
    private val keyStoreCrypto: KeyStoreCrypto
) {

    suspend operator fun invoke(
        userId: UserId,
        password: EncryptedString,
        accountType: AccountType,
    ) {
        val user = userManager.getUser(userId, refresh = true)
        if (user.keys.primary() != null) return

        val username = when (accountType) {
            AccountType.External -> checkNotNull(user.emailSplit?.username) { "User email username is needed." }
            else -> checkNotNull(user.name) { "Username is needed." }
        }

        val email = when (accountType) {
            AccountType.External -> checkNotNull(user.emailSplit) { "Email is needed." }
            AccountType.Internal -> getOrCreateInternalAddress(userId, username).emailSplit
            AccountType.Username -> return
        }

        password.decrypt(keyStoreCrypto).toByteArray().use { decryptedPassword ->
            val modulus = authRepository.randomModulus()
            val auth = srpCrypto.calculatePasswordVerifier(
                username = email.value,
                password = decryptedPassword.array,
                modulusId = modulus.modulusId,
                modulus = modulus.modulus
            )
            userManager.setupPrimaryKeys(
                sessionUserId = userId,
                username = username,
                domain = email.domain,
                auth = auth,
                password = decryptedPassword.array
            )
        }
    }

    private suspend fun getOrCreateInternalAddress(
        userId: UserId,
        username: String,
    ): UserAddress = userAddressRepository.getAddresses(userId, refresh = true).firstInternalOrNull()
        ?: userAddressRepository.createAddress(
            sessionUserId = userId,
            displayName = username,
            domain = domainRepository.getAvailableDomains().first()
        )
}
