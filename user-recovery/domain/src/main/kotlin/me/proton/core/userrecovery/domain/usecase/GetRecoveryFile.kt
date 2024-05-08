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
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.unlockOrNull
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

/**
 * Generate a recovery file encrypted with primary recovery secret.
 */
class GetRecoveryFile @Inject constructor(
    cryptoContext: CryptoContext,
    private val getExistingVerifiedRecoverySecret: GetExistingVerifiedRecoverySecret,
    private val getUnlockedUserKeys: GetUnlockedUserKeys,
) {
    private val pgpCrypto = cryptoContext.pgpCrypto

    suspend operator fun invoke(
        userId: UserId
    ): Result {
        val recoverySecret = requireNotNull(getExistingVerifiedRecoverySecret(userId)) {
            "The signature of recovery secret is invalid."
        }
        val unlockedKeys = getUnlockedUserKeys(userId)
        check(unlockedKeys.isNotEmpty())
        pgpCrypto.serializeKeys(unlockedKeys.map { it.value }).use {
            unlockedKeys.forEach { unlockedKey -> unlockedKey.close() }
            val secret = pgpCrypto.getBase64Decoded(recoverySecret)
            return Result(
                keyCount = unlockedKeys.size,
                recoveryFile = pgpCrypto.encryptDataWithPassword(it.array, secret)
            )
        }
    }

    /**
     * @param keyCount The number of keys that the [recoveryFile] can recover.
     * @param recoveryFile The recovery file encrypted with a recovery secret.
     */
    data class Result(
        val keyCount: Int,
        val recoveryFile: EncryptedMessage
    )
}
