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

package me.proton.core.key.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.VerificationTime
import me.proton.core.crypto.common.pgp.decryptAndVerifyDataOrNull
import me.proton.core.crypto.common.pgp.decryptAndVerifyFileOrNull
import me.proton.core.crypto.common.pgp.decryptDataOrNull
import org.junit.Test
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SessionKeyCryptoKtTest {

    private val context = TestCryptoContext()

    @Test
    fun `encryptData`() {
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { testContext.pgpCrypto } returns pgpCrypto
        val sessionKey = SessionKey("sessionKey".toByteArray())
        val result = sessionKey.encryptData(testContext, "message".toByteArray())
        assertNotNull(result)
        verify { pgpCrypto.encryptData("message".toByteArray(), sessionKey) }
    }

    @Test
    fun `encryptAndSignData`() {
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { testContext.pgpCrypto } returns pgpCrypto
        val sessionKey = SessionKey("sessionKey".toByteArray())
        val result = sessionKey.encryptAndSignData(testContext, "message".toByteArray(), "key".toByteArray())
        assertNotNull(result)
        verify { pgpCrypto.encryptAndSignData("message".toByteArray(), sessionKey, "key".toByteArray(), null) }
    }

    @Test
    fun `encryptAndSignFile`() {
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { testContext.pgpCrypto } returns pgpCrypto
        val sessionKey = SessionKey("sessionKey".toByteArray())
        val testFile = mockk<File>(relaxed = true)
        val destinationFile = mockk<File>(relaxed = true)
        val result = sessionKey.encryptAndSignFile(testContext, testFile, destinationFile, "key".toByteArray())
        assertNotNull(result)
        verify { pgpCrypto.encryptAndSignFile(testFile, destinationFile, sessionKey, "key".toByteArray(), null) }
    }

    @Test
    fun `decryptData`() {
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { testContext.pgpCrypto } returns pgpCrypto
        val sessionKey = SessionKey("sessionKey".toByteArray())
        val result = sessionKey.decryptData(testContext, "message".toByteArray())
        assertNotNull(result)
        verify { pgpCrypto.decryptData("message".toByteArray(), sessionKey) }
    }

    @Test
    fun `decryptAndVerifyData time_now`() {
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { testContext.pgpCrypto } returns pgpCrypto
        val sessionKey = SessionKey("sessionKey".toByteArray())
        val publicKeys = listOf("test-pub-key")
        val result = sessionKey.decryptAndVerifyData(testContext, "message".toByteArray(), publicKeys)
        assertNotNull(result)
        verify {
            pgpCrypto.decryptAndVerifyData(
                "message".toByteArray(),
                sessionKey,
                publicKeys,
                VerificationTime.Now,
                null
            )
        }
    }

    @Test
    fun `decryptAndVerifyData provided time`() {
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { testContext.pgpCrypto } returns pgpCrypto
        val sessionKey = SessionKey("sessionKey".toByteArray())
        val publicKeys = listOf("test-pub-key")
        val result =
            sessionKey.decryptAndVerifyData(testContext, "message".toByteArray(), publicKeys, VerificationTime.Utc(1))
        assertNotNull(result)
        verify {
            pgpCrypto.decryptAndVerifyData(
                "message".toByteArray(),
                sessionKey,
                publicKeys,
                VerificationTime.Utc(1),
                null
            )
        }
    }

    @Test
    fun `decryptAndVerifyFile verification time now`() {
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { testContext.pgpCrypto } returns pgpCrypto
        val sessionKey = SessionKey("sessionKey".toByteArray())
        val testFile = mockk<File>(relaxed = true)
        val destinationFile = mockk<File>(relaxed = true)
        val publicKeys = listOf("test-pub-key")
        val result = sessionKey.decryptAndVerifyFile(testContext, testFile, destinationFile, publicKeys)
        assertNotNull(result)
        verify {
            pgpCrypto.decryptAndVerifyFile(
                testFile,
                destinationFile,
                sessionKey,
                publicKeys,
                VerificationTime.Now,
                null
            )
        }
    }

    @Test
    fun `decryptAndVerifyFile provided verification time`() {
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { testContext.pgpCrypto } returns pgpCrypto
        val sessionKey = SessionKey("sessionKey".toByteArray())
        val testFile = mockk<File>(relaxed = true)
        val destinationFile = mockk<File>(relaxed = true)
        val publicKeys = listOf("test-pub-key")
        val result =
            sessionKey.decryptAndVerifyFile(testContext, testFile, destinationFile, publicKeys, VerificationTime.Utc(1))
        assertNotNull(result)
        verify {
            pgpCrypto.decryptAndVerifyFile(
                testFile,
                destinationFile,
                sessionKey,
                publicKeys,
                VerificationTime.Utc(1),
                null
            )
        }
    }

    @Test
    fun `decryptDataOrNull`() {
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { testContext.pgpCrypto } returns pgpCrypto
        val sessionKey = SessionKey("sessionKey".toByteArray())
        val result = sessionKey.decryptDataOrNull(testContext, "message".toByteArray())
        assertNotNull(result)
        verify { pgpCrypto.decryptDataOrNull("message".toByteArray(), sessionKey) }
    }

    @Test
    fun `decryptDataOrNull returns null`() {
        mockkStatic("me.proton.core.crypto.common.pgp.PGPCryptoOrNullKt")
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { testContext.pgpCrypto } returns pgpCrypto
        every { pgpCrypto.decryptDataOrNull(data = any(), sessionKey = any()) } returns null
        val sessionKey = SessionKey("sessionKey".toByteArray())
        val result = sessionKey.decryptDataOrNull(testContext, "message".toByteArray())
        assertNull(result)
        verify { pgpCrypto.decryptDataOrNull("message".toByteArray(), sessionKey) }
        unmockkStatic("me.proton.core.crypto.common.pgp.PGPCryptoOrNullKt")
    }

    @Test
    fun `decryptAndVerifyDataOrNull time_now`() {
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { testContext.pgpCrypto } returns pgpCrypto
        every {
            pgpCrypto.decryptAndVerifyDataOrNull(
                data = any(),
                sessionKey = any(),
                publicKeys = any(),
                time = any(),
                verificationContext = any()
            )
        } returns null
        val sessionKey = SessionKey("sessionKey".toByteArray())
        val publicKeys = listOf("test-pub-key")
        val result = sessionKey.decryptAndVerifyDataOrNull(testContext, "message".toByteArray(), publicKeys)
        assertNull(result)
        verify {
            pgpCrypto.decryptAndVerifyDataOrNull(
                "message".toByteArray(),
                sessionKey,
                publicKeys,
                VerificationTime.Now,
                null
            )
        }
    }

    @Test
    fun `decryptAndVerifyFileOrNull verification time default`() {
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { testContext.pgpCrypto } returns pgpCrypto
        every {
            pgpCrypto.decryptAndVerifyFileOrNull(
                source = any(),
                destination = any(),
                sessionKey = any(),
                publicKeys = any(),
                time = any(),
                verificationContext = any()
            )
        } returns null
        val sessionKey = SessionKey("sessionKey".toByteArray())
        val testFile = mockk<File>(relaxed = true)
        val destinationFile = mockk<File>(relaxed = true)
        val publicKeys = listOf("test-pub-key")
        val result = sessionKey.decryptAndVerifyFileOrNull(testContext, testFile, destinationFile, publicKeys)
        assertNull(result)
        verify {
            pgpCrypto.decryptAndVerifyFileOrNull(
                testFile,
                destinationFile,
                sessionKey,
                publicKeys,
                VerificationTime.Now,
                null
            )
        }
    }
}