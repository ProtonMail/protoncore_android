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

package me.proton.core.crypto.android.pgp

import android.util.Base64
import com.proton.gopenpgp.crypto.Crypto
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.VerificationTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class GOpenPGPCryptoTest {

    private val crypto = GOpenPGPCrypto()

    @Test
    fun getPublicKeyFromPrivateKey() {
        // GIVEN
        val privateKey = TestKey.privateKey
        val publicKey = TestKey.privateKeyPublicKey

        // WHEN
        val publicKeyFromPrivateKey = crypto.getPublicKey(privateKey)

        val expectedPublicKey = Crypto.newKeyFromArmored(publicKey)
        val actualPublicKey = Crypto.newKeyFromArmored(publicKeyFromPrivateKey)

        // THEN
        assertEquals(
            expected = expectedPublicKey.fingerprint,
            actual = actualPublicKey.fingerprint
        )
        assertFalse(actualPublicKey.isPrivate)
        assertFails { actualPublicKey.isLocked }
    }

    @Test
    fun getFingerprintFromPrivateKey() {
        // GIVEN
        val privateKey = TestKey.privateKey

        // WHEN
        val fingerprintFromPrivateKey = crypto.getFingerprint(privateKey)

        // THEN
        assertEquals(
            expected = TestKey.privateKeyFingerprint,
            actual = fingerprintFromPrivateKey
        )
    }

    @Test
    fun getPassphraseFromPasswordAndSalt() {
        // GIVEN
        val password = "password".toByteArray()
        val encodedSalt = Base64.encodeToString(ByteArray(16), Base64.DEFAULT)

        val expectedPassphrase = "7zqaLmaKtn.i7IjPfuPGY2Ah/mNM6Sy".toByteArray()

        // WHEN
        crypto.getPassphrase(password, encodedSalt).use { generatedPassphrase ->
            // THEN
            assertTrue(generatedPassphrase.array.contentEquals(expectedPassphrase))
        }
    }

    @Test
    fun getPassphraseMoreThan71CharsUsingLatin1() {
        // GIVEN
        // 80 Latin-1 chars.
        val password = "ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾".toByteArray()
        val encodedSalt = Base64.encodeToString(ByteArray(16), Base64.DEFAULT)

        val expectedPassphrase = "x1sDsGevQtmBwSzBVqCEoXSD9M8fquO".toByteArray()

        // WHEN
        crypto.getPassphrase(password, encodedSalt).use { generatedPassphrase ->
            // THEN
            assertTrue(generatedPassphrase.array.contentEquals(expectedPassphrase))
        }
    }

    @Test
    fun unlockPrivateKeyWithPassphrase() {
        // GIVEN
        val privateKey = TestKey.privateKey
        val passphrase = TestKey.privateKeyPassphrase

        // WHEN
        crypto.unlock(privateKey, passphrase).use { unlockedKey ->
            // THEN
            assertTrue(Crypto.newKey(unlockedKey.value).isUnlocked)
        }
    }

    @Test(expected = CryptoException::class)
    fun unlockPrivateKeyWithWrongPassphrase() {
        // GIVEN
        val privateKey = TestKey.privateKey
        val passphrase = TestKey.privateKey2Passphrase

        // WHEN
        crypto.unlock(privateKey, passphrase).use { unlockedKey ->
            // THEN
            assertTrue(Crypto.newKey(unlockedKey.value).isUnlocked)
        }
    }

    @Test
    fun lockUnlockedWithPassphrase() {
        // GIVEN
        val passphrase = TestKey.privateKeyPassphrase

        crypto.unlock(TestKey.privateKey, passphrase).use { unlockedKey ->
            // WHEN
            val locked = crypto.lock(unlockedKey.value, passphrase)

            // THEN
            val lockedKey = Crypto.newKeyFromArmored(locked)
            assertTrue(lockedKey.isLocked)

            // Try to use it (unlock, encrypt, sign, decrypt, verify).
            crypto.unlock(locked, passphrase).use { lockedUnlocked ->
                val publicKey = crypto.getPublicKey(TestKey.privateKey)

                val message = "message\r\nnewline"
                val encryptedOriginal = crypto.encryptAndSignText(message, publicKey, unlockedKey.value)
                val decryptedText =
                    crypto.decryptAndVerifyText(encryptedOriginal, listOf(publicKey), listOf(lockedUnlocked.value))

                assertEquals(
                    expected = VerificationStatus.Success,
                    actual = decryptedText.status
                )
                assertEquals(
                    expected = message.trimIndent(),
                    actual = decryptedText.text
                )
            }
        }
    }

    @Test
    fun encryptSignDecryptVerifyAString() {
        // GIVEN
        val message = "message\nnewline"

        val publicKey = crypto.getPublicKey(TestKey.privateKey)

        crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlocked ->
            // WHEN
            val encrypted = crypto.encryptText(message, publicKey)
            val signature = crypto.signText(message, unlocked.value)

            // THEN
            val decryptedText = crypto.decryptText(encrypted, unlocked.value)
            val isVerified = crypto.verifyText(decryptedText, signature, publicKey)
            assertTrue(isVerified)

            assertEquals(
                expected = message,
                actual = decryptedText
            )
        }
    }

    @Test
    fun encryptSignDecryptVerifyAByteArray() {
        // GIVEN
        val message = "message\r\nnewline"
        val data = message.toByteArray()

        val publicKey = crypto.getPublicKey(TestKey.privateKey)

        crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlocked ->
            // WHEN
            val encrypted = crypto.encryptData(data, publicKey)
            val signature = crypto.signData(data, unlocked.value)

            // THEN
            val decryptData = crypto.decryptData(encrypted, unlocked.value)
            val isVerified = crypto.verifyData(decryptData, signature, publicKey)
            assertTrue(isVerified)

            decryptData.use {
                assertTrue(data.contentEquals(it.array))
            }
        }
    }

    @Test
    fun encryptDecryptAByteArrayWithSessionKey() {
        // GIVEN
        val message = "message\r\nnewline"
        val data = message.toByteArray()

        val sessionKey = crypto.generateNewSessionKey()

        // WHEN
        val encrypted = crypto.encryptData(data, sessionKey)

        // THEN
        val decryptData = crypto.decryptData(encrypted, sessionKey)

        decryptData.use {
            assertTrue(data.contentEquals(it.array))
        }
    }

    @Test
    fun decryptEncryptedMessageWithPrivateKey() {
        // GIVEN
        val encrypted =
            """
            -----BEGIN PGP MESSAGE-----
            Version: ProtonMail

            wcBMA5kajsUECZmgAQgAgJuGP/0+pUPu24mWeviRQ79s6fKKsKh6y1aBXwJM
            eQ8mSaLvHNSaCa8s9yozs9gWo2/Uf8Lpmqb70SMh2npwI5hyOFqXsrMEoEHn
            KTf86kSHnGZEtwrScXnekJjO1rfYynnAYuppTfpUc2E/uGZg6RChlwPbBZMw
            tOk8n6iL6u0+Ren9fxAmmMTw66vc5PDejmfAgzbdxeD7qV8wzqmipgiErk/w
            dPEzI5QGtGXUwsDfJeSGEdCslN1kHtZRj2B3tg6Ms7Ea/VIb3Kq6uyn2hQhS
            MlWwjzauF5mryV4Kbi1RP6yTykbPnRz6ia22HwbWzOVJ2Nu534RqNYA/99Bd
            G9JcAXjM6al21XdX0ZQww2R0Of3VzFVwQX+RSG1SWGq11u2nu5iXBVUJDa5x
            MS2SksqmW3Bh7Tbz2zlrCNZxH8USiAxXt/3xjwNlXgCg4b8sKNHNN4+Wa6S8
            HNwbYAc=
            =9RxF
            -----END PGP MESSAGE-----
            """.trimIndent()

        val expected = "Test PGP/MIME Message\n\n\n"

        crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlockedKey ->
            // WHEN
            val decryptedText = crypto.decryptText(encrypted, unlockedKey.value)

            // THEN
            assertEquals(
                expected = expected,
                actual = decryptedText
            )
        }
    }

    @Test(expected = CryptoException::class)
    fun decryptEncryptedMessageWithWrongKey() {
        // GIVEN
        val encrypted =
            """
            -----BEGIN PGP MESSAGE-----
            Version: ProtonMail

            wcBMA5kajsUECZmgAQgAgJuGP/0+pUPu24mWeviRQ79s6fKKsKh6y1aBXwJM
            eQ8mSaLvHNSaCa8s9yozs9gWo2/Uf8Lpmqb70SMh2npwI5hyOFqXsrMEoEHn
            KTf86kSHnGZEtwrScXnekJjO1rfYynnAYuppTfpUc2E/uGZg6RChlwPbBZMw
            tOk8n6iL6u0+Ren9fxAmmMTw66vc5PDejmfAgzbdxeD7qV8wzqmipgiErk/w
            dPEzI5QGtGXUwsDfJeSGEdCslN1kHtZRj2B3tg6Ms7Ea/VIb3Kq6uyn2hQhS
            MlWwjzauF5mryV4Kbi1RP6yTykbPnRz6ia22HwbWzOVJ2Nu534RqNYA/99Bd
            G9JcAXjM6al21XdX0ZQww2R0Of3VzFVwQX+RSG1SWGq11u2nu5iXBVUJDa5x
            MS2SksqmW3Bh7Tbz2zlrCNZxH8USiAxXt/3xjwNlXgCg4b8sKNHNN4+Wa6S8
            HNwbYAc=
            =9RxF
            -----END PGP MESSAGE-----
            """.trimIndent()

        val expected = "Test PGP/MIME Message\n\n\n"

        // TestKey.privateKey2 is the wrong key (TestKey.privateKey is the right one).
        crypto.unlock(TestKey.privateKey2, TestKey.privateKey2Passphrase).use { unlockedKey ->
            // WHEN
            val decryptedText = crypto.decryptText(encrypted, unlockedKey.value)

            // THEN
            assertEquals(
                expected = expected,
                actual = decryptedText
            )
        }
    }

    @Test
    fun decryptEncryptedMessageWithPrivateKey2() {
        // GIVEN
        val encrypted =
            """
            -----BEGIN PGP MESSAGE-----
            Version: ProtonMail

            wcBMA7M4YhTWmh7GAQgAxAdgbJWi7MKSMiMg5rOUu6Y6nFJK9pgU5MsrKYqO
            /hXkkpocWTs4BDL+AXmy86e0C52mwsKJj/cFZ88erFLGMrkG+sVkDFi3fZ7Q
            dqrqKrzbGg6NubQpCtwGv+KvtFcMfCUWD4jeH/saD4wW9ZAH3Ozu0s/VamIX
            62VDi+l6TrZIUwsC6Pnyy5O8O1BnOultCjUP4bYApSfQIBDENBVyMVT9pp1/
            ylfgUSZQCj2vWkbMtMH+SAgBgk+MMYVBTx+Pk1O9lhZdqXhjzEmi58AZMdq7
            /+CGjJwnySBFCLaHddYfzvVVQEAJngRRl7WA+CVkskMc94w1nwlVeuARuAiy
            /9LAaQHHE3Wb1en/rqK4IPK0qWaInpVualn6KeORmtnS3Kl2Xynt92Lcckoj
            37WEdjXDCIhl4JyrldelRmaxBisnW3te4vsukGh87E4jL8oDvIMwHN0bm7KH
            +kBnlxqrR6N5vZmcjFoU+n9XBYDkoPZ0MZCwCgMi2BbWrQv7zy/o3+35kgms
            6c3Mwb7nIP15ksysLz884tF6k5cVoLFISL7OMqem1uKM66BgOYJovvRR1Y+v
            70aQ/G2w7B44mzPBOlzOAzhDQDHtxNft1XT+LH2cjrExd0EzYE+8fpYpOHC0
            KfHrt6wx/sj/O+e1M9F19UGDIJMFRmlgRIUJCEmpiaZnWjmdajfUpOPd47ac
            GYmSnoyEOzjf1Lyy0M6w7BHPQgwaF7Ss94EAcsFcfw==
            =iJT4
            -----END PGP MESSAGE-----
            """.trimIndent()

        val expected = "Dear valued customer,\n\n" +
            "Thank you for subscribing to ProtonMail! Your support will enable us to scale up ProtonMail and continue our mission to bring easy-to-use, encrypted email to people everywhere.\n\n" +
            "Thank you,\n\n" +
            "The ProtonMail Team\n"

        crypto.unlock(TestKey.privateKey2, TestKey.privateKey2Passphrase).use { unlockedKey ->
            // WHEN
            val decryptedText = crypto.decryptText(encrypted, unlockedKey.value)

            // THEN
            assertEquals(
                expected = expected,
                actual = decryptedText
            )
        }
    }

    @Test
    fun encryptAndSignDecryptAndVerifyAString() {
        // GIVEN
        val message = "message\nnewline"

        val publicKey = crypto.getPublicKey(TestKey.privateKey)

        crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlockedKey ->
            // WHEN
            val encryptedAndSigned = crypto.encryptAndSignText(message, publicKey, unlockedKey.value)

            // THEN
            val decryptedText = crypto.decryptAndVerifyText(
                message = encryptedAndSigned,
                publicKeys = listOf(publicKey),
                unlockedKeys = listOf(unlockedKey.value)
            )

            assertEquals(
                expected = VerificationStatus.Success,
                actual = decryptedText.status
            )
            assertEquals(
                expected = message,
                actual = decryptedText.text
            )
        }
    }

    @Test
    fun encryptAndSignWithCompressionDecryptAndVerifyAString() {
        // GIVEN
        val message = "message\nnewline"

        val publicKey = crypto.getPublicKey(TestKey.privateKey)

        crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlockedKey ->
            // WHEN
            val encryptedAndSigned = crypto.encryptAndSignTextWithCompression(message, publicKey, unlockedKey.value)

            // THEN
            val decryptedText = crypto.decryptAndVerifyText(
                message = encryptedAndSigned,
                publicKeys = listOf(publicKey),
                unlockedKeys = listOf(unlockedKey.value)
            )

            assertEquals(
                expected = VerificationStatus.Success,
                actual = decryptedText.status
            )
            assertEquals(
                expected = message,
                actual = decryptedText.text
            )
        }
    }

    @Test
    fun encryptAndSignWithCompressionDecryptAndVerifyBytes() {
        // GIVEN
        val message = "message\nnewline".toByteArray()

        val publicKey = crypto.getPublicKey(TestKey.privateKey)

        crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlockedKey ->
            // WHEN
            val encryptedAndSigned = crypto.encryptAndSignDataWithCompression(message, publicKey, unlockedKey.value)

            // THEN
            val decryptedData = crypto.decryptAndVerifyData(
                message = encryptedAndSigned,
                publicKeys = listOf(publicKey),
                unlockedKeys = listOf(unlockedKey.value)
            )

            assertEquals(
                expected = VerificationStatus.Success,
                actual = decryptedData.status
            )
            assertTrue(message.contentEquals(decryptedData.data))
        }
    }

    @Test
    fun encryptAndSignDecryptAndVerifyProvidedTime() {
        // GIVEN
        val message = "message\nnewline"

        val publicKey = crypto.getPublicKey(TestKey.privateKey)

        crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlockedKey ->
            // WHEN
            crypto.updateTime(1632312383) // 2021.
            val encryptedAndSigned = crypto.encryptAndSignText(message, publicKey, unlockedKey.value)

            // THEN
            val decryptedText = crypto.decryptAndVerifyText(
                message = encryptedAndSigned,
                publicKeys = listOf(publicKey),
                unlockedKeys = listOf(unlockedKey.value),
                time = VerificationTime.Utc(392039755) // 1982, keys have been generated later.
            )

            assertEquals(
                expected = VerificationStatus.Failure,
                actual = decryptedText.status
            )
            assertEquals(
                expected = message,
                actual = decryptedText.text
            )
        }
    }

    @Test
    fun encryptAndSignDecryptAndVerifyAByteArray() {
        // GIVEN
        val message = "message\r\nnewline"
        val data = message.toByteArray()

        val publicKey = crypto.getPublicKey(TestKey.privateKey)

        crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlockedKey ->
            // WHEN
            val encryptedAndSigned = crypto.encryptAndSignData(data, publicKey, unlockedKey.value)

            // THEN
            val decryptedData = crypto.decryptAndVerifyData(
                message = encryptedAndSigned,
                publicKeys = listOf(publicKey),
                unlockedKeys = listOf(unlockedKey.value)
            )

            assertEquals(
                expected = VerificationStatus.Success,
                actual = decryptedData.status
            )
            decryptedData.data.use {
                assertTrue(data.contentEquals(it.array))
            }
        }
    }

    @Test
    fun signTextEncrypted() {
        // GIVEN
        val text = "hello"
        val encryptionKeys = listOf(crypto.getPublicKey(TestKey.privateKey2))

        // WHEN
        val encryptedSignature = crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlockedKey ->
            crypto.signTextEncrypted(text, unlockedKey.value, encryptionKeys)
        }

        // THEN
        val verificationKeys = listOf(crypto.getPublicKey(TestKey.privateKey))
        crypto.unlock(TestKey.privateKey2, TestKey.privateKey2Passphrase).use { unlockedKey ->
            val verified = crypto.verifyTextEncrypted(
                text,
                encryptedSignature,
                unlockedKey.value,
                verificationKeys,
                VerificationTime.Ignore
            )
            assertTrue(verified)
        }
    }

    @Test
    fun signDataEncrypted() {
        // GIVEN
        val data = "hello".toByteArray()
        val encryptionKeys = listOf(crypto.getPublicKey(TestKey.privateKey2))

        // WHEN
        val encryptedSignature = crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlockedKey ->
            crypto.signDataEncrypted(data, unlockedKey.value, encryptionKeys)
        }

        // THEN
        val verificationKeys = listOf(crypto.getPublicKey(TestKey.privateKey))
        crypto.unlock(TestKey.privateKey2, TestKey.privateKey2Passphrase).use { unlockedKey ->
            val verified = crypto.verifyDataEncrypted(
                data,
                encryptedSignature,
                unlockedKey.value,
                verificationKeys,
                VerificationTime.Ignore
            )
            assertTrue(verified)
        }
    }

    @Test
    fun verifyTextEncrypted() {
        // GIVEN
        val text = "hello"
        val verificationKeys = listOf(crypto.getPublicKey(TestKey.privateKey))
        val encryptedSignature = """
            -----BEGIN PGP MESSAGE-----
            Version: GopenPGP 2.2.1
            Comment: https://gopenpgp.org
            
            wcBMA7M4YhTWmh7GAQgAtqETMR6x+vJ3rz+JKz53pp8gylau0tY+tbpjhoZZIIVC
            GL1HwjVBH0ERR7BmTvlyI7QhNqH88HQCBeFmEul6Jj+YBMz1XV8mU4Ve06YB6qF8
            WKf6dUZBgZ44DA4aFKVx+buTKK6xwBcUtMg7RGZItGkHMsGHxLVlFh4DpKJ0R9K3
            VmSxXGgPbejoRVye3VuYJgOcwdHzkykqJyMj16DJ7bqNINev/T4N21qnOSdHzP0b
            RMDCEb2HAc+aKh9GrfP+UteYhQL9y42OcWuOgKNCtzEVcca0JhZEnCTgWpmCBsmM
            SliWJNJfL6+fJOoD3eUJ7ATJgj7MGN2Jha1IQxf/oNLAqAFJ7jnXr+8MU6Heqf1d
            t2gzaZ5aBB3atULpfG6tqzcBuVOe7MHOXet9NWQvueLyrC6+gtWyNsDPOSun0o3v
            RXLa6G7KdW+kk74dEBv781nAor/QqyrUbxssROI49aRLzjPVAt+bwclKoy7JeIew
            zYbx+cNBl9e63mAZ8rOsZPrhvl9gjTOcDRtiuPcQr4x437fomFuPOGpMzlxUCqre
            o+Wy4JNlzygIxoWhVuwXOAEqQeUGriSOhW2gkcBoxtUhbfn9155HCJL3KBt8GeUs
            rKK3/tiV1x4qPN5qayj5azntdBOyXdVL889nrioApgReG5dsHSvxYDOsWHA7GiEi
            1RPocqZCk6V6rTvfxramoixL2d4onwpnH0QzznR8EsgKDHVQF3dlnBXwpgq6HQra
            O4X4K2or8GdQHzXlva9F3hdEx5bqyHBg5Yibk2lKxoh2zxPNh/IRlleIkTfr8RmX
            aocbgrspvQVQ+g==
            =3FTq
            -----END PGP MESSAGE-----
        """.trimIndent()
        // WHEN
        val verified = crypto.unlock(TestKey.privateKey2, TestKey.privateKey2Passphrase).use { unlockedKey ->
            crypto.verifyTextEncrypted(
                text,
                encryptedSignature,
                unlockedKey.value,
                verificationKeys,
                VerificationTime.Ignore
            )
        }
        // THEN
        assertTrue(verified)
    }

    @Test
    fun verifyEncryptedSignatureWithWrongVerificationKey() {
        // GIVEN
        val text = "hello"
        val wrongVerificationKeys = listOf(crypto.getPublicKey(TestKey.privateKey2))
        val encryptedSignature = """
            -----BEGIN PGP MESSAGE-----
            Version: GopenPGP 2.2.1
            Comment: https://gopenpgp.org
            
            wcBMA7M4YhTWmh7GAQgAtqETMR6x+vJ3rz+JKz53pp8gylau0tY+tbpjhoZZIIVC
            GL1HwjVBH0ERR7BmTvlyI7QhNqH88HQCBeFmEul6Jj+YBMz1XV8mU4Ve06YB6qF8
            WKf6dUZBgZ44DA4aFKVx+buTKK6xwBcUtMg7RGZItGkHMsGHxLVlFh4DpKJ0R9K3
            VmSxXGgPbejoRVye3VuYJgOcwdHzkykqJyMj16DJ7bqNINev/T4N21qnOSdHzP0b
            RMDCEb2HAc+aKh9GrfP+UteYhQL9y42OcWuOgKNCtzEVcca0JhZEnCTgWpmCBsmM
            SliWJNJfL6+fJOoD3eUJ7ATJgj7MGN2Jha1IQxf/oNLAqAFJ7jnXr+8MU6Heqf1d
            t2gzaZ5aBB3atULpfG6tqzcBuVOe7MHOXet9NWQvueLyrC6+gtWyNsDPOSun0o3v
            RXLa6G7KdW+kk74dEBv781nAor/QqyrUbxssROI49aRLzjPVAt+bwclKoy7JeIew
            zYbx+cNBl9e63mAZ8rOsZPrhvl9gjTOcDRtiuPcQr4x437fomFuPOGpMzlxUCqre
            o+Wy4JNlzygIxoWhVuwXOAEqQeUGriSOhW2gkcBoxtUhbfn9155HCJL3KBt8GeUs
            rKK3/tiV1x4qPN5qayj5azntdBOyXdVL889nrioApgReG5dsHSvxYDOsWHA7GiEi
            1RPocqZCk6V6rTvfxramoixL2d4onwpnH0QzznR8EsgKDHVQF3dlnBXwpgq6HQra
            O4X4K2or8GdQHzXlva9F3hdEx5bqyHBg5Yibk2lKxoh2zxPNh/IRlleIkTfr8RmX
            aocbgrspvQVQ+g==
            =3FTq
            -----END PGP MESSAGE-----
        """.trimIndent()
        // WHEN
        val verified = crypto.unlock(TestKey.privateKey2, TestKey.privateKey2Passphrase).use { unlockedKey ->
            crypto.verifyTextEncrypted(
                text,
                encryptedSignature,
                unlockedKey.value,
                wrongVerificationKeys,
                VerificationTime.Ignore
            )
        }
        // THEN
        assertFalse(verified)
    }

    @Test
    fun verifyEncryptedSignatureWithWrongDecryptionKey() {
        // GIVEN
        val text = "hello"
        val wrongVerificationKeys = listOf(crypto.getPublicKey(TestKey.privateKey))
        val encryptedSignature = """
            -----BEGIN PGP MESSAGE-----
            Version: GopenPGP 2.2.1
            Comment: https://gopenpgp.org
            
            wcBMA7M4YhTWmh7GAQgAtqETMR6x+vJ3rz+JKz53pp8gylau0tY+tbpjhoZZIIVC
            GL1HwjVBH0ERR7BmTvlyI7QhNqH88HQCBeFmEul6Jj+YBMz1XV8mU4Ve06YB6qF8
            WKf6dUZBgZ44DA4aFKVx+buTKK6xwBcUtMg7RGZItGkHMsGHxLVlFh4DpKJ0R9K3
            VmSxXGgPbejoRVye3VuYJgOcwdHzkykqJyMj16DJ7bqNINev/T4N21qnOSdHzP0b
            RMDCEb2HAc+aKh9GrfP+UteYhQL9y42OcWuOgKNCtzEVcca0JhZEnCTgWpmCBsmM
            SliWJNJfL6+fJOoD3eUJ7ATJgj7MGN2Jha1IQxf/oNLAqAFJ7jnXr+8MU6Heqf1d
            t2gzaZ5aBB3atULpfG6tqzcBuVOe7MHOXet9NWQvueLyrC6+gtWyNsDPOSun0o3v
            RXLa6G7KdW+kk74dEBv781nAor/QqyrUbxssROI49aRLzjPVAt+bwclKoy7JeIew
            zYbx+cNBl9e63mAZ8rOsZPrhvl9gjTOcDRtiuPcQr4x437fomFuPOGpMzlxUCqre
            o+Wy4JNlzygIxoWhVuwXOAEqQeUGriSOhW2gkcBoxtUhbfn9155HCJL3KBt8GeUs
            rKK3/tiV1x4qPN5qayj5azntdBOyXdVL889nrioApgReG5dsHSvxYDOsWHA7GiEi
            1RPocqZCk6V6rTvfxramoixL2d4onwpnH0QzznR8EsgKDHVQF3dlnBXwpgq6HQra
            O4X4K2or8GdQHzXlva9F3hdEx5bqyHBg5Yibk2lKxoh2zxPNh/IRlleIkTfr8RmX
            aocbgrspvQVQ+g==
            =3FTq
            -----END PGP MESSAGE-----
        """.trimIndent()
        // WHEN
        val verified = crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlockedKey ->
            crypto.verifyTextEncrypted(
                text,
                encryptedSignature,
                unlockedKey.value,
                wrongVerificationKeys,
                VerificationTime.Ignore
            )
        }
        // THEN
        assertFalse(verified)
    }

    @Test
    fun verifyEncryptedSignatureWithWrongData() {
        // GIVEN
        val text = "wrong data"
        val wrongVerificationKeys = listOf(crypto.getPublicKey(TestKey.privateKey))
        val encryptedSignature = """
            -----BEGIN PGP MESSAGE-----
            Version: GopenPGP 2.2.1
            Comment: https://gopenpgp.org
            
            wcBMA7M4YhTWmh7GAQgAtqETMR6x+vJ3rz+JKz53pp8gylau0tY+tbpjhoZZIIVC
            GL1HwjVBH0ERR7BmTvlyI7QhNqH88HQCBeFmEul6Jj+YBMz1XV8mU4Ve06YB6qF8
            WKf6dUZBgZ44DA4aFKVx+buTKK6xwBcUtMg7RGZItGkHMsGHxLVlFh4DpKJ0R9K3
            VmSxXGgPbejoRVye3VuYJgOcwdHzkykqJyMj16DJ7bqNINev/T4N21qnOSdHzP0b
            RMDCEb2HAc+aKh9GrfP+UteYhQL9y42OcWuOgKNCtzEVcca0JhZEnCTgWpmCBsmM
            SliWJNJfL6+fJOoD3eUJ7ATJgj7MGN2Jha1IQxf/oNLAqAFJ7jnXr+8MU6Heqf1d
            t2gzaZ5aBB3atULpfG6tqzcBuVOe7MHOXet9NWQvueLyrC6+gtWyNsDPOSun0o3v
            RXLa6G7KdW+kk74dEBv781nAor/QqyrUbxssROI49aRLzjPVAt+bwclKoy7JeIew
            zYbx+cNBl9e63mAZ8rOsZPrhvl9gjTOcDRtiuPcQr4x437fomFuPOGpMzlxUCqre
            o+Wy4JNlzygIxoWhVuwXOAEqQeUGriSOhW2gkcBoxtUhbfn9155HCJL3KBt8GeUs
            rKK3/tiV1x4qPN5qayj5azntdBOyXdVL889nrioApgReG5dsHSvxYDOsWHA7GiEi
            1RPocqZCk6V6rTvfxramoixL2d4onwpnH0QzznR8EsgKDHVQF3dlnBXwpgq6HQra
            O4X4K2or8GdQHzXlva9F3hdEx5bqyHBg5Yibk2lKxoh2zxPNh/IRlleIkTfr8RmX
            aocbgrspvQVQ+g==
            =3FTq
            -----END PGP MESSAGE-----
        """.trimIndent()
        // WHEN
        val verified = crypto.unlock(TestKey.privateKey2, TestKey.privateKey2Passphrase).use { unlockedKey ->
            crypto.verifyTextEncrypted(
                text,
                encryptedSignature,
                unlockedKey.value,
                wrongVerificationKeys,
                VerificationTime.Ignore
            )
        }
        // THEN
        assertFalse(verified)
    }

    @Test
    fun getVerifiedTimestampText() {
        // GIVEN
        val text = "hello"
        val verificationKey = crypto.getPublicKey(TestKey.privateKey)
        val signature = """
            -----BEGIN PGP SIGNATURE-----
            Version: GopenPGP 2.4.5
            Comment: https://gopenpgp.org
            
            wsBzBAABCgAnBQJiNHUJCZARwx6OXgf00BYhBCDPNjtY7JnnIuU+xBHDHo5eB/TQ
            AAC8Xwf9GO8HkMAcjOvluCerN/dJyZBmMlArPLW3kMiV3b44GBNxdH0Glt/tx2E5
            V9nQxZrDMCmfv2ujmaMVPmkIQZqR8chKRIADw3BggS60+OAklxAXQCwJ5QKueoeN
            C8d+IScr58oe7VValWc+D2j70yZryLbJQh+u8CT0M7Sl87IZvXMihadTEI1ZPIFU
            K7XIo2mNfVUatz3DK62iU+iYNzFpa637w1Xgn5oW4Pt/t9iyPjbA7SwztW212rw9
            jsHpVVfFZT1IN8ElBr/N/VKrA1L/A2/ybRujzGLpoxFFl6/viDbCOOS20rLEzIH+
            PMdP42XvnZGN8bVpl1u9vNzKt0dt2g==
            =yuJ3
            -----END PGP SIGNATURE-----    
        """.trimIndent()
        val expectedTimestamp = 1647605001L
        // WHEN
        val verifiedTimestamp = crypto.getVerifiedTimestampOfText(
            text,
            signature,
            verificationKey,
            VerificationTime.Ignore
        )
        // THEN
        assertEquals(expectedTimestamp, verifiedTimestamp)
    }

    @Test
    fun getVerifiedTimestampMaliciousText() {
        // GIVEN
        val text = "hello world"
        val verificationKey = crypto.getPublicKey(TestKey.privateKey)
        val signature = """
            -----BEGIN PGP SIGNATURE-----
            Version: GopenPGP 2.4.5
            Comment: https://gopenpgp.org
            
            wsBzBAABCgAnBQJiNHUJCZARwx6OXgf00BYhBCDPNjtY7JnnIuU+xBHDHo5eB/TQ
            AAC8Xwf9GO8HkMAcjOvluCerN/dJyZBmMlArPLW3kMiV3b44GBNxdH0Glt/tx2E5
            V9nQxZrDMCmfv2ujmaMVPmkIQZqR8chKRIADw3BggS60+OAklxAXQCwJ5QKueoeN
            C8d+IScr58oe7VValWc+D2j70yZryLbJQh+u8CT0M7Sl87IZvXMihadTEI1ZPIFU
            K7XIo2mNfVUatz3DK62iU+iYNzFpa637w1Xgn5oW4Pt/t9iyPjbA7SwztW212rw9
            jsHpVVfFZT1IN8ElBr/N/VKrA1L/A2/ybRujzGLpoxFFl6/viDbCOOS20rLEzIH+
            PMdP42XvnZGN8bVpl1u9vNzKt0dt2g==
            =yuJ3
            -----END PGP SIGNATURE-----    
        """.trimIndent()
        // WHEN
        val verifiedTimestamp = crypto.getVerifiedTimestampOfText(
            text,
            signature,
            verificationKey,
            VerificationTime.Ignore
        )
        // THEN
        assertNull(verifiedTimestamp)
    }

    @Test
    fun getVerifiedTimestampData() {
        // GIVEN
        val data = "hello".toByteArray()
        val verificationKey = crypto.getPublicKey(TestKey.privateKey)
        val signature = """
            -----BEGIN PGP SIGNATURE-----
            Version: GopenPGP 2.4.5
            Comment: https://gopenpgp.org
            
            wsBzBAABCgAnBQJiNHUJCZARwx6OXgf00BYhBCDPNjtY7JnnIuU+xBHDHo5eB/TQ
            AAC8Xwf9GO8HkMAcjOvluCerN/dJyZBmMlArPLW3kMiV3b44GBNxdH0Glt/tx2E5
            V9nQxZrDMCmfv2ujmaMVPmkIQZqR8chKRIADw3BggS60+OAklxAXQCwJ5QKueoeN
            C8d+IScr58oe7VValWc+D2j70yZryLbJQh+u8CT0M7Sl87IZvXMihadTEI1ZPIFU
            K7XIo2mNfVUatz3DK62iU+iYNzFpa637w1Xgn5oW4Pt/t9iyPjbA7SwztW212rw9
            jsHpVVfFZT1IN8ElBr/N/VKrA1L/A2/ybRujzGLpoxFFl6/viDbCOOS20rLEzIH+
            PMdP42XvnZGN8bVpl1u9vNzKt0dt2g==
            =yuJ3
            -----END PGP SIGNATURE-----    
        """.trimIndent()
        val expectedTimestamp = 1647605001L
        // WHEN
        val verifiedTimestamp = crypto.getVerifiedTimestampOfData(
            data,
            signature,
            verificationKey,
            VerificationTime.Ignore
        )
        // THEN
        assertEquals(expectedTimestamp, verifiedTimestamp)
    }

    @Test
    fun isValidKeyWithPrivateKey() {
        // GIVEN
        val privateKey = TestKey.privateKey
        // WHEN
        val isValid = crypto.isValidKey(privateKey)
        // THEN
        assertTrue(isValid)
    }

    @Test
    fun isValidKeyWithPublicKey() {
        // GIVEN
        val publicKey = TestKey.privateKeyPublicKey
        // WHEN
        val isValid = crypto.isValidKey(publicKey)
        // THEN
        assertTrue(isValid)
    }

    @Test
    fun isValidKeyWithExpiredPrivateKey() {
        // GIVEN
        // Passphrase: ABCDE
        val expiredPrivateKey = """
            -----BEGIN PGP PRIVATE KEY BLOCK-----
    
            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
            =slK0
            -----END PGP PRIVATE KEY BLOCK-----
        """.trimIndent()
        // WHEN
        val isValid = crypto.isValidKey(expiredPrivateKey)
        // THEN
        assertTrue(isValid)
    }

    @Test
    fun isValidKeyWithExpiredPublicKey() {
        // GIVEN
        // Public key corresponding to the previous tests' private key
        val expiredPublicKey = """
            -----BEGIN PGP PUBLIC KEY BLOCK-----
            mQENBGK7DUcBCADjMhrWs5hf6dy5ZljQ8vKqam5NfbmozwW02Pd/X2izwRylES8R
            HARocFzVhnPxJ3tWz5anBh3kNyPT/Gr62OAFLreDT+3PmqyIICG3iuFooCCaniBo
            8F8S5B9Tq+NQffRkNPnQu32yo5ViaGIxTsDXWq+jWoVdpbn0pHfvZWtwZXYcRfhf
            0cPeBFW3DyW2NuvOdyWKwokXdYgPhknjaFxEWOWZkbCTUWCyZBP45Dp3ip8VcwYV
            bU1QrU0lCmxj+rleaALPYp8ZvcCnTj1nMNTX+veeU+uFuM/5y3qiNDSMHBYEpcr0
            GKV5BqRMzl3h7eOehxPBU95W0tPoMAKEmrxtABEBAAG0IlRlc3RpbmcgdGVzdCAo
            RXhwaXJlZCkgPHRlc3RAdGVzdD6JAVQEEwEKAD4WIQT8AUnAQKzg7JOZs7s12JCU
            VERd1gUCYrsNRwIbAwUJAAAcRAULCQgHAgYVCgkICwIEFgIDAQIeAQIXgAAKCRA1
            2JCUVERd1iMdB/9slthUz5m5Px2SwdpRbJdJoXh8pHxwS3RcvzO5oL/Gdfi083ZW
            00V0C7foVVjWrBsB3PV1mYa5CfNEg0SSl2c0oFgCXiafHvOery5w3hyyO0vm1LmR
            3BHgxmF+z/x0m31Ou/PEuFBCcsUhquQHQyy4JLRZPC1l8FncDIKPXmYvOUcsL20t
            vTk9+b6j72IlsaFzl++8zZly45wRlxei8Y2Fqp7mZS/9mrjEkEdt0Kt/SIfL/G8N
            rNRftumDymD4d1zOitmyqTXm0sC1oG3nqV+dOjmPrqqszuLQxacihchzcYhoLnCo
            R+gGQvrIoOeswAAJRPdRC0zQ478ZxMATFCaOuQENBGK7DUcBCAC/kBGgq5Y4LGcD
            YdaAekK/5sXZj1Fs4w2yuH/YTQpTjfOzIMSfp4YgGjGUHUNjM6Ha6rcinvXFponX
            M1Slb5wsRlhT/U4UjnA9BPSAwohI3gctce/O7WkFxE5LVeOi4dp81ByrUowDGQ07
            Ra3W7WdRpG3dMbI8o7PzfwnC6v80VXAQXg1cuK/Up8t91MpQtJvmvnBs2wA9wPLo
            WZCcxsxdMvhp/k81saTorx8DpE4PVnfOhk6PJq671zm/yZlJu+Ai3CuTOka3vBck
            q1c3DcEBIk4q1gtTlop9ovV0afx9aJ6YDDrpU3O1oC2XZfMm6XNuYNbvBM0PrWk3
            lJbsGcBVABEBAAGJATwEGAEKACYWIQT8AUnAQKzg7JOZs7s12JCUVERd1gUCYrsN
            RwIbDAUJAAAcRAAKCRA12JCUVERd1uLbB/49HDpXyf+iIpALSu7nFsnqygLUeEab
            qCKCwU9XcyLnPOmJ7beFZPdvLMOej8MM1dumucqBlBc1u7O2H37X97f1aae9p5oH
            /XwO1RECguwYKOhCNmPI9TNLJyNUdUTzWlhDv3s7VkhsAULhoBPWwKpU22lezYW5
            xLyMnvUYi0GXq+tCq9wWnRyirTglJDROU4e+UtglxGLdfsCGID6nCLEwMdqx05Yq
            XLogagGRQNpAh7LxrjiAvp7pzaBSiGLBKxhaBt/loUqIaOAdhFyJM+D1VIsxjG8x
            3XRF35qNirIccf7Q3sNPbUW49m34wDHaZUGrMNbGWiMrkMI0pHnQal9y
            =ClXN
            -----END PGP PUBLIC KEY BLOCK-----

        """.trimIndent()
        // WHEN
        val isValid = crypto.isValidKey(expiredPublicKey)
        // THEN
        assertFalse(isValid)
    }

    @Test
    fun isValidKeyWithMalformedData() {
        // GIVEN
        val data = "ABCDEFG"
        // WHEN
        val isValid = crypto.isValidKey(data)
        // THEN
        assertFalse(isValid)
    }

    @Test
    fun isValidKeyWithSignature() {
        // GIVEN
        val signature = """
            -----BEGIN PGP SIGNATURE-----
            Version: GopenPGP 2.4.5
            Comment: https://gopenpgp.org
            
            wsBzBAABCgAnBQJiNHUJCZARwx6OXgf00BYhBCDPNjtY7JnnIuU+xBHDHo5eB/TQ
            AAC8Xwf9GO8HkMAcjOvluCerN/dJyZBmMlArPLW3kMiV3b44GBNxdH0Glt/tx2E5
            V9nQxZrDMCmfv2ujmaMVPmkIQZqR8chKRIADw3BggS60+OAklxAXQCwJ5QKueoeN
            C8d+IScr58oe7VValWc+D2j70yZryLbJQh+u8CT0M7Sl87IZvXMihadTEI1ZPIFU
            K7XIo2mNfVUatz3DK62iU+iYNzFpa637w1Xgn5oW4Pt/t9iyPjbA7SwztW212rw9
            jsHpVVfFZT1IN8ElBr/N/VKrA1L/A2/ybRujzGLpoxFFl6/viDbCOOS20rLEzIH+
            PMdP42XvnZGN8bVpl1u9vNzKt0dt2g==
            =yuJ3
            -----END PGP SIGNATURE-----    
        """.trimIndent()
        // WHEN
        val isValid = crypto.isValidKey(signature)
        // THEN
        assertFalse(isValid)
    }

    @Test
    fun getArmoredSignature() {
        // GIVEN
        val data = "ABC"
        val rawSignatureBase64 = "wsBzBAABCgAnBQJiuxb9CZARwx6OXgf00BYhBCDPNjtY7JnnIuU+xBHDHo5eB/TQAAAWFQf9Gs4Ws/YlaNys5fBPkZ5CGhYIZsIz4lBZ2Ib84/Va9D/74jDk3b3egfqHoxcyUq9OWq5kcX26tl9Z6BbvHN0DQp6aiyIOJ/Hpz2x8DvS1udLgXSlmpMhH9tj8qO1lvq3vdm/PAtwhPdqPN09gL2o+N4kIoQgrE4q/j7Oj8M+NDjfejWiu5hx8umJAePkpG2ObnLl17SAc7LfZNt6veUf/nQoUMPm42C1aXrTIvbQe3QaqYfGODggrxqrAW7vVq5j7yfQhzb2al78/f/OV6SJ3Dj9fsR3lG2Xsa4F7ePgOetT3vRBRvsCJNR2k5EaQDPz7+BDRcLfF8LYmMIvi1T9whw=="

        // WHEN
        val signature = crypto.getArmored(crypto.getBase64Decoded(rawSignatureBase64), PGPHeader.Signature)
        val verified = crypto.verifyData(data.encodeToByteArray(), signature, TestKey.privateKeyPublicKey, time = VerificationTime.Utc(1656515077))
        // THEN
        assertTrue(signature.contains("PGP SIGNATURE"))
        assertTrue(verified)
    }

    @Test
    fun isPrivateKeyWithPrivateKey() {
        assertTrue(crypto.isPrivateKey(TestKey.privateKey))
    }

    @Test
    fun isPrivateKeyWithPublicKey() {
        assertFalse(crypto.isPrivateKey(TestKey.privateKeyPublicKey))
    }

    @Test
    fun isPrivateKeyWithRandomData() {
        assertFalse(crypto.isPrivateKey("RANDOM DATA"))
    }

    @Test
    fun isPublicKeyWithPrivateKey() {
        assertFalse(crypto.isPublicKey(TestKey.privateKey))
    }

    @Test
    fun isPublicKeyWithPublicKey() {
        assertTrue(crypto.isPublicKey(TestKey.privateKeyPublicKey))
    }

    @Test
    fun isPublicKeyWithRandomData() {
        assertFalse(crypto.isPublicKey("RANDOM DATA"))
    }

    @Test
    fun signDetachedTrimTrailingSpaces() {
        // given
        val plainText = "this is a test\nWith spaces:   \nAnd trailing tabs:\t"
        val publicKey = TestKey.privateKeyPublicKey
        // when
        val signature = crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlockedKey ->
            crypto.signText(
                plainText,
                unlockedKey.value,
                trimTrailingSpaces = true
            )
        }
        // then
        assertTrue {
            crypto.verifyText(
                plainText,
                signature,
                publicKey,
                trimTrailingSpaces = true
            )
        }
        assertFalse {
            crypto.verifyText(
                plainText,
                signature,
                publicKey,
                trimTrailingSpaces = false
            )
        }
    }

    @Test
    fun verifyDetachedGopenpgpv2_4_10() {
        // given
        crypto.updateTime(1671550000)
        val plainText = "This is a test\nWith trailing spaces:    \n  With leading spaces\nWith trailing tabs:\t\t\n\tWith leading tabs\nWith trailing carriage returns:\r\n\rWith leading carriage returns\n\t \r With a mix \t\r\n"
        val signature = """
            -----BEGIN PGP SIGNATURE-----
            Version: GopenPGP 2.4.10
            Comment: https://gopenpgp.org

            wsBzBAABCgAnBQJjocOZCZARwx6OXgf00BYhBCDPNjtY7JnnIuU+xBHDHo5eB/TQ
            AACGgwf7Bx6J7JLZ2G6RFvr/wtl0DENZxUVS4H3wZPEIuVTh3/Lzd5BHfWN/mD+q
            Sz0BcjRNxAI+nDY2/J8HPIibNg1NDlUgrgxK0NPLS1DMWmtoW3JTF5sfFMyiVGxo
            RH4oluOe/UQcfxYTbMr8/EX8Gc9kdx4U7MqQNEc9CM5VIuxrfMpSZ2hvn5zlwexQ
            WdnWjVWePpbwpltX98wTlAtU93XARUgeIMrzkhEBc1sNSg6/ynECLENm8EMxWQmj
            9lpaROb2Fw50G7S1YjSUlc7WK+e4+IIP3Fqw/b21Kd1BasHS92OuHZNalbxyJA0F
            V6Zkmvzj3h9CucLSJw1Bo6ZJTDbkBQ==
            =fVs7
            -----END PGP SIGNATURE-----
        """.trimIndent()
        val publicKey = TestKey.privateKeyPublicKey
        // when
        val verifiedWithTrimming = crypto.verifyText(
            plainText,
            signature,
            publicKey,
            trimTrailingSpaces = true
        )
        val verifiedWithoutTrimming = crypto.verifyText(
            plainText,
            signature,
            publicKey,
            trimTrailingSpaces = false
        )
        // then
        assertTrue(verifiedWithTrimming)
        assertFalse(verifiedWithoutTrimming)
    }

    @Test
    fun verifyDetachedGopenpgpv2_5_0() {
        // given
        crypto.updateTime(1671550000)
        val plainText = "This is a test\n" +
            "With trailing spaces:    \n" +
            "  With leading spaces\n" +
            "With trailing tabs:\t\t\n" +
            "\tWith leading tabs\n" +
            "With trailing carriage returns:\r\n" +
            "\rWith leading carriage returns\n" +
            "\t \r With a mix \t\r\n"
        val signature = """
            -----BEGIN PGP SIGNATURE-----
            Version: GopenPGP 2.5.0
            Comment: https://gopenpgp.org
            
            wsBzBAEBCgAnBQJjocO4CZARwx6OXgf00BYhBCDPNjtY7JnnIuU+xBHDHo5eB/TQ
            AACLDQgAiGesYiKYkZCiFvytCmsFa/yTaOh96YaOlGwdXErbwsmEu6ZJfjoLp+Bp
            bBfpWDIrr93J3J8r9GVLAPrr3Eln3H4gyTNGXsfoCBjAE/25Ly7UtxrXjOonwW49
            QrbtlZ+t8QzdVdLAppi1LNPgt3PEUQozhHF1PvJUgb97fHTnDydOUD1CKl5zskTl
            fgRmTojIVqmPkG9VMWdc1sYyPixqTvaXp/Si0YVuHrH/NAjX1VHBLbRanVnd+Gnv
            2FlchBhWOipboS9Z6wf/4i83ZdOW61xqquUXwNI/K1ZadmS8X/+ojRO93V3FNWmR
            27KgLumCX2j+vKvb6E3YMWTmTfrxsg==
            =kmgZ
            -----END PGP SIGNATURE-----
        """.trimIndent()
        val publicKey = TestKey.privateKeyPublicKey
        // when
        val verifiedWithTrimming = crypto.verifyText(
            plainText,
            signature,
            publicKey,
            trimTrailingSpaces = true
        )
        val verifiedWithoutTrimming = crypto.verifyText(
            plainText,
            signature,
            publicKey,
            trimTrailingSpaces = false
        )
        val timestamp = crypto.getVerifiedTimestampOfData(
            plainText.toByteArray(),
            signature,
            publicKey
        )
        // then
        assertFalse(verifiedWithTrimming)
        assertTrue(verifiedWithoutTrimming)
    }

    @Test
    fun signDetachedNoTrimTrailingSpaces() {
        // given
        val plainText = "this is a test\nWith spaces:   \nAnd trailing tabs:\t"
        val publicKey = TestKey.privateKeyPublicKey
        // when
        val signature = crypto.unlock(TestKey.privateKey, TestKey.privateKeyPassphrase).use { unlockedKey ->
            crypto.signText(
                plainText,
                unlockedKey.value,
                trimTrailingSpaces = false
            )
        }
        // then
        assertFalse {
            crypto.verifyText(
                plainText,
                signature,
                publicKey,
                trimTrailingSpaces = true
            )
        }
        assertTrue {
            crypto.verifyText(
                plainText,
                signature,
                publicKey,
                trimTrailingSpaces = false
            )
        }
    }
}
