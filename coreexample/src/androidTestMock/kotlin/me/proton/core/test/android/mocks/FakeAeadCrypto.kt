/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.test.android.mocks

import me.proton.core.crypto.common.aead.AeadEncryptedByteArray
import me.proton.core.crypto.common.aead.AeadEncryptedString
import me.proton.core.crypto.common.aead.AeadCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray

class FakeAeadCrypto : AeadCrypto {
    override fun encrypt(
        value: String,
        key: ByteArray,
        aad: ByteArray?
    ): AeadEncryptedString = value

    override fun decrypt(
        value: AeadEncryptedString,
        key: ByteArray,
        aad: ByteArray?
    ): String = value

    override fun encrypt(
        value: PlainByteArray,
        key: ByteArray,
        aad: ByteArray?
    ): AeadEncryptedByteArray = AeadEncryptedByteArray(value.array)

    override fun decrypt(
        value: AeadEncryptedByteArray,
        key: ByteArray,
        aad: ByteArray?
    ): PlainByteArray = PlainByteArray(value.array)
}
