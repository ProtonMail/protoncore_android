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

package me.proton.core.auth.presentation.srp

import com.proton.gopenpgp.crypto.Crypto
import me.proton.core.auth.domain.crypto.CryptoProvider
import me.proton.core.auth.domain.entity.Auth
import me.proton.core.auth.domain.entity.KeySecurity
import me.proton.core.auth.domain.entity.KeyType
import javax.inject.Inject

/**
 * @author Dino Kadrikj.
 */
class CryptoProviderImpl @Inject constructor() : CryptoProvider {

    @Suppress("TooGenericExceptionCaught")
    // Proton GopenPGP lib throws this generic exception, so we have to live with this detekt warning
    // until the lib is updated
    override fun passphraseCanUnlockKey(armoredKey: String, passphrase: ByteArray): Boolean {
        return try {
            val unlockedKey = Crypto.newKeyFromArmored(armoredKey).unlock(passphrase)
            unlockedKey.clearPrivateParams()
            true
        } catch (ignored: Exception) {
            // means that the unlock check has failed. This is how gopenpgp works.
            false
        }
    }

    /**
     * Generates new private key.
     *
     * @param username the username for which the key will be generated
     * @param passphrase the mailbox entered/generated passphrase
     * @param keyType the type of key (see [KeyType])
     * @param keySecurity the key length (bits)
     *
     * @return the new private key as String.
     */
    override fun generateNewPrivateKey(
        username: String,
        domain: String,
        passphrase: ByteArray,
        keyType: KeyType,
        keySecurity: KeySecurity
    ): String {
        TODO("Not yet implemented")
    }

    /**
     * Generates new signed key list for a key.
     *
     * @param passphrase the mailbox entered/generated passphrase
     *
     * @return a pair of key-list in JSON format and it's signature
     */
    override fun generateSignedKeyList(key: String, passphrase: ByteArray): Pair<String, String> {
        TODO("Not yet implemented")
    }

    override fun calculatePasswordVerifier(
        username: String,
        passphrase: ByteArray,
        modulusId: String,
        modulus: String
    ): Auth {
        TODO("Not yet implemented")
    }
}
