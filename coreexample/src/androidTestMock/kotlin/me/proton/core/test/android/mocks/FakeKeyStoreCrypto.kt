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

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray

class FakeKeyStoreCrypto : KeyStoreCrypto {
    override fun isUsingKeyStore(): Boolean = false
    override fun encrypt(value: String): EncryptedString = value
    override fun decrypt(value: EncryptedString): String = value
    override fun encrypt(value: PlainByteArray): EncryptedByteArray = EncryptedByteArray(value.array)
    override fun decrypt(value: EncryptedByteArray): PlainByteArray = PlainByteArray(value.array)
}
