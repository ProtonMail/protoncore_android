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

package me.proton.core.crypto.android.pgp

import com.proton.gopenpgp.crypto.Crypto
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.util.kotlin.HashUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class DecryptMimeMessageTest {

    private lateinit var decryptMimeMessage: DecryptMimeMessage

    private val decryptionKey = """
        -----BEGIN PGP PRIVATE KEY BLOCK-----
        Version: GopenPGP 2.4.5
        Comment: https://gopenpgp.org

        xYYEXNidoBYJKwYBBAHaRw8BAQdA5tcrjOeA3T4EdEYFdatvvc74Om57MWQMkREh
        sjC5ePv+CQMIya1zJGkQJepgylS8P4mdvG5FckeujdPkVRQGutDYaki4zSyJAk5G
        fIcNcBH+JUB/rqC+fhW50+FX3maH5lEw/LpQkQOmYOHyESoaoRTQf80pdGVzdEBw
        cm90b25tYWlsLmNvbSA8dGVzdEBwcm90b25tYWlsLmNvbT7CjAQTFggAPgUCXNid
        oAmQ0SXZ8I0QC6wWIQSbyyEpkow7QroiEwTRJdnwjRALrAIbAwIeAQIZAQMLCQcC
        FQgDFgACAiIBAACmIAEAn7pZZEc4DQno5XhnTD3AKPZeRXwJrh8yuX1Y/FDFAAEA
        /1gmogW3RSePurl9O4ZRKgy28O9nUs1Rj5xfjvD711gEx4sEXNidoBIKKwYBBAGX
        VQEFAQEHQHdfieoKF+aXdibZJ/jzXBjMh5iqHAhiL9UZhkksO7wNAwEKCf4JAwgJ
        CvZDu/l2FmC1i4vDtjvjV4LhK5WQ76wupPxICbkm3lNl4GBsVzKQ+iBuwQmK0rrt
        +EA9u728I1BQ8KiupXg+INqlpiO4lPkv2Z9VU06JwngEGBYIACoFAlzYnaAJkNEl
        2fCNEAusFiEEm8shKZKMO0K6IhME0SXZ8I0QC6wCGwwAAPoJAP418zZTD1o/TLfi
        06/mVYM0UWMXnrI+92E7nXUqkNWIUgD+J3vbUuYKTm1Ik86zXqOkHpgnyR7yt6kQ
        RsN2tP8lJQ8=
        =okmI
        -----END PGP PRIVATE KEY BLOCK-----
    """.trimIndent()

    private val verificationKey = """
        -----BEGIN PGP PUBLIC KEY BLOCK-----
        Version: GopenPGP 2.4.5
        Comment: https://gopenpgp.org

        xjMEXNidoBYJKwYBBAHaRw8BAQdAQMB1pb2SfHhzpycpIy6iDyU7oMXWXgZsKxAz
        X3lTEq7NKXRlc3RAcHJvdG9ubWFpbC5jb20gPHRlc3RAcHJvdG9ubWFpbC5jb20+
        wowEExYIAD4FAlzYnaAJkNLQ/NBUB1IfFiEEauk3gpYXSZa4G3Fw0tD80FQHUh8C
        GwMCHgECGQEDCwkHAhUIAxYAAgIiAQAA0T8BAOwwjUxGfoaasnsSF2dtkP385bqW
        ZCYaIl9c2WZuF0TwAQD482U7ymKWAzwwruMh8hl3oDZFG4KPKUdJOEY9UqsKAs44
        BFzYnaASCisGAQQBl1UBBQEBB0ArwsHX08RHU5mUlixhavveY4mrZe/4PtHE//Cg
        oYZwLQMBCgnCeAQYFggAKgUCXNidoAmQ0tD80FQHUh8WIQRq6TeClhdJlrgbcXDS
        0PzQVAdSHwIbDAAAf+QA/3c701MpUo8p/P4EKPzhskIJ1vxhojvFBNcHMK2xC8q7
        AP9Ghc67vUniu7oMsIiKTJlLIDYLQ0UDmHaifXwk9KD5Aw==
        =5mEE
        -----END PGP PUBLIC KEY BLOCK-----
    """.trimIndent()

    private val decryptionKeyRing =
        Crypto.newKeyFromArmored(decryptionKey).unlock("test_passphrase".toByteArray()).let {
            Crypto.newKeyRing(it)
        }

    private val verificationKeyRing =
        Crypto.newKeyFromArmored(verificationKey).let {
            Crypto.newKeyRing(it)
        }

    private val expectedContent = "This is a signed pgp message\r\n\r\n\r\n"
    private val expectedMimeType = "text/plain"

    @Before
    fun setUp() {
        decryptMimeMessage = DecryptMimeMessage()
    }

    @Test
    fun signedOK() {
        // given
        val message = """
            -----BEGIN PGP MESSAGE-----
            Version: GopenPGP 2.4.5
            Comment: https://gopenpgp.org

            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
            jEoeYMkeq9KD/MHX
            =6xDM
            -----END PGP MESSAGE-----
        """.trimIndent()
        val expectedStatus = VerificationStatus.Success
        // when
        val decrypted = decryptMimeMessage(
            Crypto.newPGPMessageFromArmored(message),
            decryptionKeyRing,
            verificationKeyRing,
            0
        )
        // then
        assertEquals(expectedContent, decrypted.body.content)
        assertEquals(expectedMimeType, decrypted.body.mimeType)
        assertTrue(decrypted.attachments.isEmpty())
        assertEquals(expectedStatus, decrypted.verificationStatus)
    }

    @Test
    fun decryptWithoutVerifying() {
        // given
        val message = """
            -----BEGIN PGP MESSAGE-----
            Version: GopenPGP 2.4.5
            Comment: https://gopenpgp.org

            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
            jEoeYMkeq9KD/MHX
            =6xDM
            -----END PGP MESSAGE-----
        """.trimIndent()
        val expectedStatus = VerificationStatus.Unknown
        // when
        val decrypted = decryptMimeMessage(
            Crypto.newPGPMessageFromArmored(message),
            decryptionKeyRing,
            null,
            0
        )
        // then
        assertEquals(expectedContent, decrypted.body.content)
        assertEquals(expectedMimeType, decrypted.body.mimeType)
        assertTrue(decrypted.attachments.isEmpty())
        assertEquals(expectedStatus, decrypted.verificationStatus)
    }

    @Test
    fun notSigned() {
        // given
        val message = """
            -----BEGIN PGP MESSAGE-----
            Version: GopenPGP 2.4.5
            Comment: https://gopenpgp.org
            
            wV4DuoHa5jmSStQSAQdAqOkR+qOMTnX9ti37YPYkqryH+Qz81KzPjMyygVlLDk8w
            rbHrUqgitGV8b9Q5EFQGDAyEB0cz0mjrCbw9Z03XdwNSX3lR/Jpq06aa4Dvyo509
            0ukBbE+Jxqx/uws+klnjTfWkuwWNuZ7jD1RrLfchqgKCMKY+HcZ6tgMq8s8n6wZ5
            elBfUH3kC5dSpj2o+ygAXHqaL37OQ9UvK8PQ06qJTcfnddfVuGFqS2kERDgTUDXA
            hBF/OJV9zk50UP0sPrh+PuQArHmlIPGA5c0gX9ELvSSXTv8ej6f4el19FnRM1Tun
            TeCbnYcD2sK6TA3838sDb8jP3nz30RvPZql8KEY1J9drRSgZZNUwIaTckb5M1dy+
            zE+jxPPMXjTGTDGiwSGOTdp4VSbxknjq4B0YYHjg1c6ZrgTkKzUVFReOR1gzURrg
            itjmK+TDb47W60AchhUA5jV6AmE8FqEyxudQEXCmfcC+FQxznvXwLN1u3Iq4oFF3
            bqQS7BR1vlGv4upTGX8LWdRnA2LuZdBLEoseBhYwKBwFbmSszXgSdPohJ9hDRj0+
            Uw2fFYGlYXFPNOkCtT/XxQDuT4MSYJvVyvNw2jPskVCsb9sM+JeNxRduZ6u1RIUp
            NzedsWRko2ZgarvYuee+fByKHpFXhrIBHSESlAtJMARFzIxpjlekzYXUQHtI6W2x
            qu/OJ9v2ILNpfbs2isteE2sh3EMspP3Gtzra5NO3CkuUNOCZ5n6NLlBgLEwXkLqM
            VbigYXrc6zl5XYpByB7LjB8It4HX+CW5tZj3doFJ9TR+0hzIE4udrLHJzpILm4ir
            dTC9PjjMAO1jXMtaYt6x
            =4hL/
            -----END PGP MESSAGE-----
        """.trimIndent()
        val expectedStatus = VerificationStatus.NotSigned
        val expectedContent = "This message is not signed\r\n\r\n\r\n"
        // when
        val decrypted = decryptMimeMessage(
            Crypto.newPGPMessageFromArmored(message),
            decryptionKeyRing,
            verificationKeyRing,
            0
        )
        // then
        assertEquals(expectedContent, decrypted.body.content)
        assertEquals(expectedMimeType, decrypted.body.mimeType)
        assertTrue(decrypted.attachments.isEmpty())
        assertEquals(expectedStatus, decrypted.verificationStatus)
    }

    @Test
    fun noSigner() {
        // given
        val message = """
            -----BEGIN PGP MESSAGE-----
            Version: GopenPGP 2.4.5
            Comment: https://gopenpgp.org
            
            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
            HOwmXIQbo62hfZVd
            =d+Do
            -----END PGP MESSAGE-----
        """.trimIndent()
        val expectedStatus = VerificationStatus.NotMatchKey
        // when
        val decrypted = decryptMimeMessage(
            Crypto.newPGPMessageFromArmored(message),
            decryptionKeyRing,
            verificationKeyRing,
            0
        )
        // then
        assertEquals(expectedContent, decrypted.body.content)
        assertEquals(expectedMimeType, decrypted.body.mimeType)
        assertTrue(decrypted.attachments.isEmpty())
        assertEquals(expectedStatus, decrypted.verificationStatus)
    }

    @Test
    fun wrongSignature() {
        // given
        val message = """
            -----BEGIN PGP MESSAGE-----
            Version: GopenPGP 2.4.5
            Comment: https://gopenpgp.org
            
            wV4DuoHa5jmSStQSAQdAE+PTO7W4TYPFg94xbE5Dq0E/XxBgLbiIeZRKfJ7PQWUw
            BP9zce4xIVOtt42SjKSlXZ0rAA7lEQSGkYbKKqzfNbNdpbKTgXVzLcDaKaey9Lpe
            0uoBOTgSTIGE1HlRpiLJ4ZH/ex33ZiFy1sCoO0pLQjShDAjOWiEXaQywAFoW8zrS
            zMKq4CK/49Lr8x3T1kvataahykj65EjSM8U/XimvK0WsgPATxcMoA1vDjIlk5Mlu
            G9D4W+sRn4uwYxwB92nznx+e/HtOC6qnZCqZJw+ska5fG0gcmzQknpey6REflFMs
            aY+ULN9Q0KgSzVzRXIuVt2ycKZbMDjQq4SlSi6LEeCfuHvBEG85Gp/IfleYQwPd/
            S9mVNyQl2hfK/a91h1uk6qabwccpV+mhqQqVavGHeBlO0xpM9B3aWGYS560WPq2L
            OeU0X1oRmotaFwn5EOkBLE6Wd1gSrd9nrXGPSLwkAWXm1GWltPQ9WLdqWvTPAkGm
            zunogsQJGVFGelt8T3uFFTC3gGq1dQhGarqpaEr9XR4A9F61Fg5YGpxWtnAWNWUF
            OxbFN52CzhP5oUFqtu0hSUI3RfFwNzx3c1AKqxkkJvUOpOjosaSfDpd5V4Yr9M0z
            t+1tqc5dJAkkHZXIdKk9DfYJGu6JjKGjgEMGiDdfRLYopBVH5ZkMU4KtgkbuPsa8
            EcxOktrAc5PQWYk3Tm4JNBIzEtDyncWo9ZpiNsPOsOWOpVfmOtWIXymjYXaf1yh2
            f9dVc+YprsCHkcCwIJNDgpMgVijvr45eEmu/DDb3+t0cr+t06uGsmz69Aw1HieCe
            mnvAKc48uSu9Vt+jQ3eEaGnx6QGm6DTY9IdjIFeF84ANkQuYWPzvrFDjvj7qA4Ir
            X+NYBLqd+USt82spCrZHcIn+O7nulxGcTgiAL/RwSEeAdjkxCHNQJa174P6HjQXx
            mBN0wp2MpfAj4yVe80yAfLSUGQUnTP/hq1QaALO0HJbnsI2MuSUEzY4VDmxZCaHH
            PiFx02VY9RfsWJ7M4CJjGoDZ8mWEWubR50G4t6966yw/pNqPUEtq0Hu6wGuKOtzb
            J6+TufVgvSFOiTJb11d2dO80HXGNsGISTavek5kI1IlDdQ1rHMllaVUuLlSmAGT1
            6qmV+DjUo1b2Nkj6ZoBpS162YwZnksVxwubmxySmebW4SpTByO7q5rUXvUdl4Lpy
            RXsIqH4uAI10DCEx2YNoigjxJe5WksOAHNqXHIHf96rMUQ8U4a4nfkNYrXJ9741A
            mqdVp0Jo7igSS12vyGKJ9gDbuWh0SMJv8DmfCjMVNtd1/eduOxWQUn0gGZ5+he8Y
            yFNNYzFmP4nDldtt8CbJPaAOnQqAwiUnXrQs0LThj/+0Obevj3Ts23EMtiOmANER
            oJVbWLqvMy+LQYgUiFACj0ExsD7MAyeDvBgzn0gHHF6r6DlFefc2FPvOW8g3q96h
            AAPemuXQkLOv/ZAf8dJfsCpAwOgsrrgCV0UQGpA2KSQKvrML1lfunrDRLWh01ehe
            4HxZIV901vQA8UzPvvLteHgn55upToT+zrQnmk/+JtpRrknWej+ugk0D27p7rhF+
            IO2f3jl3jg8dFsdeItT9zGZYb/E4tKsbt9qFvqqYY2uSQIMNUurowHPhvdvkMy9E
            3xMUWox+za0bOdPYQZSvELqlePyqwukYqMkuEqCDLuvpWf+57mrXDxcrTH+T2C6q
            T/P0bss8tgpvZ0fO3Qj4P0X0jv/EorSjyFImRkjl4WodcYs/IfG13KGZVke4ve6F
            gBT+XjM6Am3CoS9comE0qcqN22KJwXSL3CZt/qEIdyEBh9Ge3SVrwC+Bsyl9hMsF
            3myZBB/LYbD8vh+2T8Xl0JvQEhiznGunmO9/uxmqQygY2g3RrVmJze7lOSD+tHNc
            h6Lw7jfMpsSddT1wmNNmU/XdAcmS5tRS4kHmqeaxqOeWnYl8e+vy89jsXO5M3N6X
            bi3C+Orst+FBbaP3Q4LJKoDG4O4OJ5+uMtnNC4OKB7KN60ZaIJ0terjTf7K/9gjk
            fuoVwj3delL/Ndzc
            =sXg5
            -----END PGP MESSAGE-----
        """.trimIndent()
        val expectedStatus = VerificationStatus.Failure
        // when
        val decrypted = decryptMimeMessage(
            Crypto.newPGPMessageFromArmored(message),
            decryptionKeyRing,
            verificationKeyRing,
            0
        )
        // then
        assertEquals(expectedContent, decrypted.body.content)
        assertEquals(expectedMimeType, decrypted.body.mimeType)
        assertTrue(decrypted.attachments.isEmpty())
        assertEquals(expectedStatus, decrypted.verificationStatus)
    }

    @Test
    fun mimeMessageWithAttachments() {
        // given
        val message = """
            -----BEGIN PGP MESSAGE-----

            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
            =ZFQP
            -----END PGP MESSAGE-----
        """.trimIndent()
        val expectedStatus = VerificationStatus.Unknown
        val expectedBodyHash = "ff4e0b6a62f150d42c4cd0c2d5359f2a5abc0497692676a03b5e7b6de0af73c8"
        val expectedMimeType = "text/html"
        // when
        val decrypted = decryptMimeMessage(
            Crypto.newPGPMessageFromArmored(message),
            decryptionKeyRing,
            null,
            0
        )
        // then
        val bodyHash = HashUtils.sha256(decrypted.body.content)
        assertEquals(expectedBodyHash, bodyHash)
        assertEquals(expectedMimeType, decrypted.body.mimeType)
        assertEquals(expectedStatus, decrypted.verificationStatus)
        assertEquals(2, decrypted.attachments.size)
    }

    @Test
    fun decryptWithWrongKeyThrows() {
        // given
        val message = """
            -----BEGIN PGP MESSAGE-----
            Version: GopenPGP 2.4.5
            Comment: https://gopenpgp.org

            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
            jEoeYMkeq9KD/MHX
            =6xDM
            -----END PGP MESSAGE-----
        """.trimIndent()
        val wrongKey = """
            -----BEGIN PGP PRIVATE KEY BLOCK-----
            Comment: 2295 C1DE A775 830F D5BA  99DE 61BB 6D61 E68C D460
            Comment: test@example.proton.me
            
            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
            =7bFC
            -----END PGP PRIVATE KEY BLOCK-----
        """.trimIndent()
        val wrongDecryptionKeyRing = Crypto.newKeyFromArmored(wrongKey).let { Crypto.newKeyRing(it) }
        // when
        assertFailsWith<CryptoException> {
            decryptMimeMessage(
                Crypto.newPGPMessageFromArmored(message),
                wrongDecryptionKeyRing,
                null,
                0
            )
        }
    }

}
