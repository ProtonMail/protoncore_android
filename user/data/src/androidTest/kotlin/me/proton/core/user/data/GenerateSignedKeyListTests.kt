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

package me.proton.core.user.data

import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.pgp.VerificationContext
import me.proton.core.crypto.common.pgp.VerificationTime
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.useKeys
import me.proton.core.key.domain.verifyText
import me.proton.core.test.kotlin.assertEquals
import me.proton.core.test.kotlin.assertTrue
import me.proton.core.user.data.usecase.GenerateSignedKeyList
import me.proton.core.user.domain.Constants
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import org.junit.Test
import kotlin.test.BeforeTest

class GenerateSignedKeyListTests {
    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = object : KeyStoreCrypto {
            override fun isUsingKeyStore(): Boolean = false
            override fun encrypt(value: String): EncryptedString = value
            override fun decrypt(value: EncryptedString): String = value
            override fun encrypt(value: PlainByteArray): EncryptedByteArray = EncryptedByteArray(value.array.copyOf())
            override fun decrypt(value: EncryptedByteArray): PlainByteArray = PlainByteArray(value.array.copyOf())
        }
    )

    private lateinit var generateSignedKeyList: GenerateSignedKeyList

    @BeforeTest
    fun setUp() {
        generateSignedKeyList = GenerateSignedKeyList(cryptoContext)
    }

    @Test
    fun signKeyListTest() {
        // given
        val userAddress = UserAddress(
            userId = UserId("userId"),
            addressId = AddressId("addressId"),
            email = "email",
            canSend = true,
            canReceive = true,
            enabled = true,
            order = 0,
            keys = listOf(
                UserAddressKey(
                    addressId = AddressId("addressId"),
                    version = 0,
                    flags = 3,
                    active = true,
                    keyId = KeyId("key1"),
                    privateKey = PrivateKey(
                        key = TestKeys.Key1.privateKey,
                        isPrimary = true,
                        passphrase = EncryptedByteArray(TestKeys.Key1.passphrase)
                    )
                ),
                UserAddressKey(
                    addressId = AddressId("addressId"),
                    version = 0,
                    flags = 1,
                    active = true,
                    keyId = KeyId("key2"),
                    privateKey = PrivateKey(
                        key = TestKeys.Key2.privateKey,
                        isPrimary = false,
                        passphrase = EncryptedByteArray(TestKeys.Key2.passphrase)
                    )
                ),
                UserAddressKey(
                    addressId = AddressId("addressId"),
                    version = 0,
                    flags = 1,
                    active = false,             // <---- inactive
                    keyId = KeyId("key3"),
                    privateKey = PrivateKey(
                        key = TestKeys.Key2.privateKey,
                        isPrimary = false,
                        passphrase = EncryptedByteArray(TestKeys.Key2.passphrase)
                    )
                )
            ),
            signedKeyList = null
        )
        val expectedKeyListData = "[{\"Fingerprint\": \"20cf363b58ec99e722e53ec411c31e8e5e07f4d0\"," +
            "\"SHA256Fingerprints\": [\"7770549b156e1d2a973c6c48eba0554e0bb3edb4c30c6e226f245ec888c75d70\"," +
            "\"f984ebd4d4cd502ffc3f4df9e5dd6413266e41119137074c4ead54ca0bd60507\"],\"Flags\": 3,\"Primary\": 1}," +
            "{\"Fingerprint\": \"6b268d358b07f79f4833861155b8c95a6275d981\"," +
            "\"SHA256Fingerprints\": [\"21af090acd7498d29d9f53c451499d1ce09d9147fd1c777abbee911838bd1ca6\"," +
            "\"8cff8e4f65cb6cc857170afbcfa6f0a5f473c7a8edb5cbeb331ff3f5c6a544a1\"],\"Flags\": 1,\"Primary\": 0}]"
        // WHEN
        val signedKeyList = generateSignedKeyList(userAddress)
        // THEN
        assertEquals(expectedKeyListData, signedKeyList.data) { "Signed key list data didn't match" }
        val verified = userAddress.useKeys(cryptoContext) {
            verifyText(
                text = requireNotNull(signedKeyList.data),
                signature = requireNotNull(signedKeyList.signature),
                time = VerificationTime.Ignore,
                verificationContext = VerificationContext(
                    value = Constants.signedKeyListContextValue,
                    required = VerificationContext.ContextRequirement.Required.Always
                )
            )
        }
        assertTrue(verified) { "Couldn't verify the key list signature" }
    }
}
