/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.userrecovery.domain.usecase

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.unlockOrNull
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.repository.UserRemoteDataSource
import javax.inject.Inject

/**
 * Generate a recovery file encrypted with primary recovery secret.
 */
class GetRecoveryFile @Inject constructor(
    private val userManager: UserManager,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val cryptoContext: CryptoContext
) {
    private val pgpCrypto = cryptoContext.pgpCrypto

    suspend operator fun invoke(
        userId: UserId
    ): EncryptedMessage {
        // Get recoverySecret from remote.
        val remoteUser = userRemoteDataSource.fetch(userId)
        val primaryKey = remoteUser.keys.firstOrNull { it.privateKey.isPrimary }
        val primaryKeyRecoverySecret = requireNotNull(primaryKey?.recoverySecret)
        // Continue with local User (with unlockable keys).
        val user = userManager.getUser(userId)
        val activeKeys = user.keys.filter { it.active ?: false }
        val privateKeys = activeKeys.map { it.privateKey }
        val unlockedKeys = privateKeys.mapNotNull { it.unlockOrNull(cryptoContext)?.unlockedKey }
        check(unlockedKeys.isNotEmpty())
        pgpCrypto.serializeKeys(unlockedKeys.map { it.value }).use {
            unlockedKeys.forEach { unlockedKey -> unlockedKey.close() }
            val secret = pgpCrypto.getBase64Decoded(primaryKeyRecoverySecret)
            return pgpCrypto.encryptDataWithPassword(it.array, secret)
        }
    }
}
