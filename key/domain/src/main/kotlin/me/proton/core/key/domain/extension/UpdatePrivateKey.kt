/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.key.domain.extension

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.updatePrivateKeyPassphraseOrNull
import me.proton.core.key.domain.canUnlock
import me.proton.core.key.domain.entity.key.Key
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.keyholder.KeyHolderPrivateKey

fun KeyHolderPrivateKey.updatePrivateKeyPassphraseOrNull(
    cryptoContext: CryptoContext,
    newPassphrase: ByteArray
): Key? {
    val passphrase = privateKey.passphrase?.decrypt(cryptoContext.keyStoreCrypto)?.array ?: return null
    return cryptoContext.pgpCrypto.updatePrivateKeyPassphraseOrNull(
        privateKey = privateKey.key,
        passphrase = passphrase,
        newPassphrase = newPassphrase
    )?.let { Key(keyId, it) }
}

fun Armored.updatePrivateKeyPassphrase(
    cryptoContext: CryptoContext,
    passphrase: ByteArray,
    newPassphrase: ByteArray
): Armored {
    return cryptoContext.pgpCrypto.updatePrivateKeyPassphrase(
        privateKey = this,
        passphrase = passphrase,
        newPassphrase = newPassphrase
    )
}

/**
 * Copy instance and replace [PrivateKey.passphrase] and [PrivateKey.isActive] using [canUnlock].
 */
fun PrivateKey.updateIsActive(context: CryptoContext, passphrase: EncryptedByteArray?) = copy(
    passphrase = passphrase,
    isActive = canUnlock(context, passphrase)
)
