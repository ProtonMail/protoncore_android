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

package me.proton.core.auth.domain.crypto

import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.Radix64Encoder
import com.google.crypto.tink.subtle.Base64

/**
 * Provides the gopenpgp interfaces for keys passphrase generation and validation.
 * @author Dino Kadrikj.
 */
interface CryptoProvider {

    /**
     * Generates passphrase using provided salt.
     *
     * @param passphrase the passphrase from which the generated BCrypt phrase should be derived
     * @param encodedSalt the Base64 encoded salt
     *
     * @return BCrypt version of the passphrase
     */
    fun generateMailboxPassphrase(passphrase: ByteArray, encodedSalt: String): ByteArray {
        val decodedKeySalt: ByteArray = Base64.decode(encodedSalt, Base64.DEFAULT)
        val generatedUserPassphraseByteRawHash =
            BCrypt.with(BCrypt.Version.VERSION_2Y).hashRaw(10, decodedKeySalt, passphrase).rawHash
        return Radix64Encoder.Default().encode(generatedUserPassphraseByteRawHash)
    }


    /**
     * Checks if a key could be unlocked by a passphrase.
     *
     * @param armoredKey the protected key
     * @param passphrase the passphrase to check
     *
     * @return whether the [armoredKey] could be unlocked with the [passphrase] provided.
     */
    fun passphraseCanUnlockKey(armoredKey: String, passphrase: ByteArray): Boolean
}
