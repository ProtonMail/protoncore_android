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

package me.proton.core.contact.domain

import ezvcard.Ezvcard
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.getFingerprintOrNull
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.Recipient
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CryptoUtilsTests {

    private val cryptoContextMock = mockk<CryptoContext>()
    private val pgpCryptoMock: PGPCrypto = mockk()

    val sut = CryptoUtilsImpl()

    @Before
    fun `before each`() {
        clearAllMocks()

        every { cryptoContextMock.pgpCrypto } returns pgpCryptoMock

        every { pgpCryptoMock.getFingerprintOrNull(any()) } returns "key fingerprint"
    }

    val vCardEmail = "calendar@proton.black"

    val vCardWith2PinnedKeys = """
            BEGIN:VCARD
            VERSION:4.0
            FN;PREF=1:calendar@proton.black
            ITEM1.EMAIL;PREF=1:calendar@proton.black
            UID:proton-web-4e57f941-d1b4-7909-c879-73a2df5513f1
            ITEM1.KEY;PREF=2:data:application/pgp-keys;base64,xjMEZKMDghYJKwYBBAHaRw8BA
             QdAGems3V25edmSzHRFKePBU7692nBprwN3uZmapAjgH+bNLWNhbGVuZGFyQHByb3Rvbi5ibGFj
             ayA8Y2FsZW5kYXJAcHJvdG9uLmJsYWNrPsKPBBMWCABBBQJkowOCCZDXZ5WySpsJSRYhBIhRe96
             tJFIFkIiG7ddnlbJKmwlJAhsDAh4BAhkBAwsJBwIVCAMWAAIFJwkCBwIAAKfmAQDwrg8xuiTdYi
             xQFoK1fr/UREL2FpHE1pE9QLwO0slSsAD7BKMYfsSykTUxonqiJ+CK67EETbYIt+fIndA2GVaP5
             gDOOARkowOCEgorBgEEAZdVAQUBAQdAVllU+SypP4bqiTvgGIgg2O3GEpMUTE8MBkRdZEZIGRkD
             AQoJwngEGBYIACoFAmSjA4IJkNdnlbJKmwlJFiEEiFF73q0kUgWQiIbt12eVskqbCUkCGwwAAFd
             vAQCCv7i8tCLAp4Mwzh7fsOKQxqdq6tlq+FB/TPuaKGQiIgEAoSVwDasnAMoeH9GKjP29ExWORv
             PmxsmDMe0uzZy4xA0=
            ITEM1.KEY;PREF=1:data:application/pgp-keys;base64,xjMEYIE/zBYJKwYBBAHaRw8BA
             QdAU0kzBdPct+/iReob+92uE1hEJPzoXnrrTqx5p8EoOa7NLWNhbGVuZGFyQHByb3Rvbi5ibGFj
             ayA8Y2FsZW5kYXJAcHJvdG9uLmJsYWNrPsKPBBAWCgAgBQJggT/MBgsJBwgDAgQVCAoCBBYCAQA
             CGQECGwMCHgEAIQkQ9LTBFWUbz9MWIQQL9ztQ8o2jSXASlPX0tMEVZRvP09xeAQD3ioSt4E6SyV
             xOeS8xBQvhuEXkqBKKZCkMO10fd0P2LgD/WvtGpRv8JAll0feMgG2y1lufZtJImTeLr0ciYb7AE
             gnOOARggT/MEgorBgEEAZdVAQUBAQdAJYTJ0NuH3zSCNxk+gsFNTVHuPDLQQLRsyNermAbrEXID
             AQgHwngEGBYIAAkFAmCBP8wCGwwAIQkQ9LTBFWUbz9MWIQQL9ztQ8o2jSXASlPX0tMEVZRvP0/e
             ZAQC9vSk4lPi9v1dMHsbKCChrYPR2WCMSUXykpNcDuP2TBgEA0jjgSKW351PQTmHU15UcSFY71O
             pD+j04Cs4EcONklw0=
            ITEM1.X-PM-ENCRYPT:true
            ITEM1.X-PM-SIGN:true
            END:VCARD
        """.trimIndent()

    val vCardWithNoPinnedKeys = """
            BEGIN:VCARD
            VERSION:4.0
            FN;PREF=1:calendar@proton.black
            ITEM1.EMAIL;PREF=1:calendar@proton.black
            UID:proton-web-4e57f941-d1b4-7909-c879-73a2df5513f1
            ITEM1.X-PM-ENCRYPT:true
            ITEM1.X-PM-SIGN:true
            END:VCARD
        """.trimIndent()

    val vCardWith2PinnedKeysForDifferentEmail = """
            BEGIN:VCARD
            VERSION:4.0
            FN;PREF=1:calendar@proton.black
            ITEM1.EMAIL;PREF=1:calendar@proton.black
            ITEM2.EMAIL;PREF=1:different-email@proton.black
            UID:proton-web-4e57f941-d1b4-7909-c879-73a2df5513f1
            ITEM2.KEY;PREF=2:data:application/pgp-keys;base64,xjMEZKMDghYJKwYBBAHaRw8BA
             QdAGems3V25edmSzHRFKePBU7692nBprwN3uZmapAjgH+bNLWNhbGVuZGFyQHByb3Rvbi5ibGFj
             ayA8Y2FsZW5kYXJAcHJvdG9uLmJsYWNrPsKPBBMWCABBBQJkowOCCZDXZ5WySpsJSRYhBIhRe96
             tJFIFkIiG7ddnlbJKmwlJAhsDAh4BAhkBAwsJBwIVCAMWAAIFJwkCBwIAAKfmAQDwrg8xuiTdYi
             xQFoK1fr/UREL2FpHE1pE9QLwO0slSsAD7BKMYfsSykTUxonqiJ+CK67EETbYIt+fIndA2GVaP5
             gDOOARkowOCEgorBgEEAZdVAQUBAQdAVllU+SypP4bqiTvgGIgg2O3GEpMUTE8MBkRdZEZIGRkD
             AQoJwngEGBYIACoFAmSjA4IJkNdnlbJKmwlJFiEEiFF73q0kUgWQiIbt12eVskqbCUkCGwwAAFd
             vAQCCv7i8tCLAp4Mwzh7fsOKQxqdq6tlq+FB/TPuaKGQiIgEAoSVwDasnAMoeH9GKjP29ExWORv
             PmxsmDMe0uzZy4xA0=
            ITEM2.KEY;PREF=1:data:application/pgp-keys;base64,xjMEYIE/zBYJKwYBBAHaRw8BA
             QdAU0kzBdPct+/iReob+92uE1hEJPzoXnrrTqx5p8EoOa7NLWNhbGVuZGFyQHByb3Rvbi5ibGFj
             ayA8Y2FsZW5kYXJAcHJvdG9uLmJsYWNrPsKPBBAWCgAgBQJggT/MBgsJBwgDAgQVCAoCBBYCAQA
             CGQECGwMCHgEAIQkQ9LTBFWUbz9MWIQQL9ztQ8o2jSXASlPX0tMEVZRvP09xeAQD3ioSt4E6SyV
             xOeS8xBQvhuEXkqBKKZCkMO10fd0P2LgD/WvtGpRv8JAll0feMgG2y1lufZtJImTeLr0ciYb7AE
             gnOOARggT/MEgorBgEEAZdVAQUBAQdAJYTJ0NuH3zSCNxk+gsFNTVHuPDLQQLRsyNermAbrEXID
             AQgHwngEGBYIAAkFAmCBP8wCGwwAIQkQ9LTBFWUbz9MWIQQL9ztQ8o2jSXASlPX0tMEVZRvP0/e
             ZAQC9vSk4lPi9v1dMHsbKCChrYPR2WCMSUXykpNcDuP2TBgEA0jjgSKW351PQTmHU15UcSFY71O
             pD+j04Cs4EcONklw0=
            ITEM1.X-PM-ENCRYPT:true
            ITEM1.X-PM-SIGN:true
            END:VCARD
        """.trimIndent()

    val publicAddress = PublicAddress(
        "calendar@proton.black",
        recipientType = Recipient.External.value,
        "text/html",
        listOf(
            PublicAddressKey(
                "calendar@proton.black", 3, PublicKey(
                    "armored key from public repository",
                    isPrimary = true,
                    true, true, true
                )
            ),
        ),
        null,
        ignoreKT=0
    )

    @Test
    fun `correctly extract first pinned key for encrypting, keys in reverse order`() {

        val vCard = Ezvcard.parse(vCardWith2PinnedKeys).first()

        every { pgpCryptoMock.isKeyExpired(any()) } returns false
        every { pgpCryptoMock.isKeyRevoked(any()) } returns false

        every { pgpCryptoMock.getArmored(vCard.keys[0].data, any()) } returns "armored public key pref 2"
        every { pgpCryptoMock.getArmored(vCard.keys[1].data, any()) } returns "armored public key pref 1"

        val result = sut.extractPinnedPublicKeys(
            CryptoUtils.PinnedKeysPurpose.Encrypting,
            vCardEmail,
            vCard,
            publicAddress,
            cryptoContextMock
        )

        with(result as CryptoUtils.PinnedKeysOrError.Success) {
            assertTrue(result.pinnedPublicKeys.size == 1)
            assertTrue(result.pinnedPublicKeys[0].key.contentEquals("armored public key pref 1"))
        }

    }

    @Test
    fun `correctly extract all pinned keys for signature verification`() {

        val vCard = Ezvcard.parse(vCardWith2PinnedKeys).first()

        every { pgpCryptoMock.isKeyExpired(any()) } returns false
        every { pgpCryptoMock.isKeyRevoked(any()) } returns false
        every { pgpCryptoMock.getArmored(any(), any()) } returns "armored public key"

        val result = sut.extractPinnedPublicKeys(
            CryptoUtils.PinnedKeysPurpose.VerifyingSignature,
            vCardEmail,
            vCard,
            publicAddress,
            cryptoContextMock
        )

        println(result)

        with(result as CryptoUtils.PinnedKeysOrError.Success) {
            assertTrue(result.pinnedPublicKeys.size == 2)
        }

    }

    @Test
    fun `error extracting keys when there are pinned keys but for different email address`() {

        val vCard = Ezvcard.parse(vCardWith2PinnedKeysForDifferentEmail).first()

        val result = sut.extractPinnedPublicKeys(
            CryptoUtils.PinnedKeysPurpose.Encrypting,
            vCardEmail,
            vCard,
            publicAddress,
            cryptoContextMock
        )

        assertEquals(result, CryptoUtils.PinnedKeysOrError.Error.NoKeysAvailable)
    }

    @Test
    fun `error extracting keys when there are no pinned keys`() {

        val vCard = Ezvcard.parse(vCardWithNoPinnedKeys).first()

        val result = sut.extractPinnedPublicKeys(
            CryptoUtils.PinnedKeysPurpose.Encrypting,
            vCardEmail,
            vCard,
            publicAddress,
            cryptoContextMock
        )

        assertEquals(result, CryptoUtils.PinnedKeysOrError.Error.NoKeysAvailable)
    }

    @Test
    fun `error extracting keys when email is not found in VCard`() {

        val vCard = Ezvcard.parse(vCardWith2PinnedKeys).first()

        val result = sut.extractPinnedPublicKeys(
            CryptoUtils.PinnedKeysPurpose.Encrypting,
            "non-existing@email.com",
            vCard,
            publicAddress,
            cryptoContextMock
        )

        assertEquals(result, CryptoUtils.PinnedKeysOrError.Error.NoEmailInVCard)
    }

}

