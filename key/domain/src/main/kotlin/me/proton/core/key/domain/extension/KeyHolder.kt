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
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decryptWith
import me.proton.core.key.domain.entity.key.Key
import me.proton.core.key.domain.entity.keyholder.KeyHolderPrivateKey

fun KeyHolderPrivateKey.updatePrivateKey(
    keyStoreCrypto: KeyStoreCrypto,
    cryptoContext: CryptoContext,
    newPassphrase: ByteArray
): Key? {
    val passphrase = privateKey.passphrase?.decryptWith(keyStoreCrypto)?.array ?: return null
    val armored = cryptoContext.pgpCrypto.updatePrivateKeyPassphrase(
        privateKey = privateKey.key,
        oldPassphrase = passphrase,
        newPassphrase = newPassphrase
    )
    return if (armored != null) Key(armored, keyId.id) else null
}

fun String.updateOrganizationPrivateKey(
    cryptoContext: CryptoContext,
    currentPassphrase: ByteArray,
    newPassphrase: ByteArray
): String? {
    return cryptoContext.pgpCrypto.updatePrivateKeyPassphrase(
        privateKey = this,
        oldPassphrase = currentPassphrase,
        newPassphrase = newPassphrase
    )
}