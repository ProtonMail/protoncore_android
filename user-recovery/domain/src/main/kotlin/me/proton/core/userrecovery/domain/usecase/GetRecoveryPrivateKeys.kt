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
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.decryptDataWithPasswordOrNull
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.UnlockedPrivateKey
import me.proton.core.key.domain.lock
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.repository.PassphraseRepository
import javax.inject.Inject

class GetRecoveryPrivateKeys @Inject constructor(
    private val userManager: UserManager,
    private val passphraseRepository: PassphraseRepository,
    private val cryptoContext: CryptoContext
) {
    private val pgpCrypto = cryptoContext.pgpCrypto

    suspend operator fun invoke(
        userId: UserId,
        message: EncryptedMessage,
    ): List<PrivateKey> {
        val user = userManager.getUser(userId, refresh = true)
        val secrets = user.keys.mapNotNull { key -> key.recoverySecret }
        secrets.forEach { secret ->
            val decodedSecret = pgpCrypto.getBase64Decoded(secret)
            pgpCrypto.decryptDataWithPasswordOrNull(message, decodedSecret)?.let {
                val unlockedKeys = pgpCrypto.deserializeKeys(it)
                val passphrase = checkNotNull(passphraseRepository.getPassphrase(userId))
                return unlockedKeys.map { key ->
                    UnlockedPrivateKey(TempUnlockedKey(key), isPrimary = false).use { unlocked ->
                        unlocked.lock(cryptoContext, passphrase = passphrase, isPrimary = false)
                    }
                }
            }
        }
        return emptyList()
    }

    // Only used to create PrivateKey from UnlockedPrivateKey.
    private class TempUnlockedKey(override val value: Unarmored) : UnlockedKey {
        override fun close() {
            value.fill(0)
        }
    }
}
