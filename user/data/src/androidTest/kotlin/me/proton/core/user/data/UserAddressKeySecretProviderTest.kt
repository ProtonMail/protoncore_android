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

package me.proton.core.user.data

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.key.domain.useKeysAs
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.entity.UserKey
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNull

class UserAddressKeySecretProviderTest {

    private lateinit var provider: UserAddressKeySecretProvider
    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = object : KeyStoreCrypto {
            override fun isUsingKeyStore(): Boolean = false
            override fun encrypt(value: String): EncryptedString = value
            override fun decrypt(value: EncryptedString): String = value
            override fun encrypt(value: PlainByteArray): EncryptedByteArray = EncryptedByteArray(value.array.copyOf())
            override fun decrypt(value: EncryptedByteArray): PlainByteArray = PlainByteArray(value.array.copyOf())
        }
    )

    private val mailboxPassword = "mailboxPassword"
    private val tokenVal = """
        -----BEGIN PGP MESSAGE-----
        Version: GopenPGP 2.3.1
        Comment: https://gopenpgp.org
    
        wV4DNBPNx6Mfm4QSAQdA+gjeD8AGMVyluYObzKlo9B9kU4SNeiqAWjqwFGXTHFMw
        DZzOa6EIuNeyTQoGrFSKma3Z0oyKNCRvsF70Vt1K8wSEx/dNsLn0AGjMm0D1Cm9z
        0lAB3vhbnxfSE2/cVCBRjs87k9TvFcoZHp5eztyL1/BkXfLyugh6nFp5LV8H0ZO/
        HWS28TNwxGtna0QZtLe2Xe+tEm/wnW4NFZ3SOyMNBvWWmw==
        =LjHO
        -----END PGP MESSAGE-----
    """.trimIndent()
    private val tokenSignature = """
        -----BEGIN PGP SIGNATURE-----
        Version: GopenPGP 2.3.1
        Comment: https://gopenpgp.org

        wnUEABYKACcFAmHAhc4JkD8UQEqW+HQOFqEEWT32tVwJth9elkubPxRASpb4dA4A
        AElYAP0Uya2xCtEKuv+7qsIqlb/LvTLNY+/1csBo1sse7WKN2AD+PkU1i6RJHs+u
        BKwLEalXrDxzHVBKw2QK29wOn8tZXgY=
        =fdg7
        -----END PGP SIGNATURE-----
    """.trimIndent()
    private val privateUserKey = """
        -----BEGIN PGP PRIVATE KEY BLOCK-----
        Version: GopenPGP 2.3.1
        Comment: https://gopenpgp.org
    
        xYYEYcCFzhYJKwYBBAHaRw8BAQdAU/1bnScil300DPZgNarSabg+D7DgWmnTA+wR
        6Mp/97r+CQMIzPA6oob4f6xgD0rhEUmxO/MHZTIywg1fbXa5pzYdpMaEKaUUp015
        eziVhKXcFPA/6upQT1hlFC8lt49YTMTbxs24k9NN+wOaJbAJOLrPL80LdGVzdCA8
        dGVzdD7CjAQTFggAPgUCYcCFzgmQPxRASpb4dA4WoQRZPfa1XAm2H16WS5s/FEBK
        lvh0DgIbAwIeAQIZAQMLCQcCFQgDFgACAiIBAACvDgEAzMpdqT/wPWFo1S7+daUg
        o2nQpGZ3M5wIjzQ2C9V/CHEA/i7AqNNCJYMbWysMNdohYWUeGgTUxKC0WdKEvuql
        5HICx4sEYcCFzhIKKwYBBAGXVQEFAQEHQFEbuqzVb2s3I/kQu6VPXy4abibrDnIP
        0RHGmi9fZRxIAwEKCf4JAwjzE/GkMIwewmBEmURSZ40FYGLsIGDY8P1N5Jty0bAK
        cb9w3+nHND3IQldzGoiEk/y5kFI1UFR2A9TCpxSUKW1qMSFKzmi/VPxJaksQZIfq
        wngEGBYIACoFAmHAhc4JkD8UQEqW+HQOFqEEWT32tVwJth9elkubPxRASpb4dA4C
        GwwAAGsVAP9wOFHhuRzbp8kmmZJKs0sty8Kqo8w8A+HvGpHh/pYHlgEAlxjYenkr
        u9wJxv2+Wc4w4aIT8gDCPxanKA6L9y0yiws=
        =Jt+6
        -----END PGP PRIVATE KEY BLOCK-----
    """.trimIndent()


    @BeforeTest
    fun setUp() {
        provider = UserAddressKeySecretProvider(mockk(), cryptoContext)
    }

    @Test
    fun getPassphraseWontReturnTokenWithWrongFormat() = runTest {
        // GIVEN
        val userKey = mockk<UserKey>(relaxed = true) {
            every { privateKey } returns mockk(relaxed = true) {
                every { key } returns privateUserKey
                every { passphrase } returns EncryptedByteArray(mailboxPassword.toByteArray())
            }
        }
        val user = mockk<User>(relaxed = true) {
            every { keys } returns listOf(userKey)
        }
        val addressKey = mockk<UserAddressKey>(relaxed = true) {
            every { active } returns true
            every { token } returns tokenVal
            every { signature } returns tokenSignature
        }
        // WHEN
        val passphrase = user.useKeysAs(cryptoContext) { userContext ->
            provider.getPassphrase(
                userId = mockk(),
                userContext = userContext,
                key = addressKey,
            )
        }
        // THEN
        assertNull(passphrase)
    }

}
