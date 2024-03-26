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

import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.unlockOrNull
import me.proton.core.user.domain.UserManager
import me.proton.core.util.kotlin.serialize
import javax.inject.Inject

/**
 * Generate a recovery file encrypted with primary recovery secret.
 */
class GetRecoveryFile @Inject constructor(
    private val userManager: UserManager,
    private val cryptoContext: CryptoContext
) {
    private val pgpCrypto = cryptoContext.pgpCrypto

    suspend operator fun invoke(
        userId: UserId
    ): EncryptedMessage {
        val user = userManager.getUser(userId)
        val primaryKey = user.keys.firstOrNull { it.privateKey.isPrimary }
        val primaryKeyRecoverySecret = requireNotNull(primaryKey?.recoverySecret)
        val activeKeys = user.keys.filter { it.active ?: false }
        val privateKeys = activeKeys.map { it.privateKey }
        val unlockedKeys = privateKeys.mapNotNull { it.unlockOrNull(cryptoContext)?.unlockedKey }
        check(unlockedKeys.isNotEmpty())
        val byteArrayList = ByteArrayList(unlockedKeys.map { it.value })
        val fileByteArray = byteArrayList.serialize().encodeToByteArray()
        val secret = pgpCrypto.getBase64Decoded(primaryKeyRecoverySecret)
        return pgpCrypto.encryptDataWithPassword(fileByteArray, secret)
    }
}

@Deprecated("Replace with proper binary serialization (pgp).")
@Serializable
data class ByteArrayList(
    val keys: List<ByteArray>
)
