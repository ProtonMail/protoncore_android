package me.proton.core.auth.domain.usecase.fork

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.aead.AeadCrypto
import me.proton.core.crypto.common.aead.AeadCryptoFactory
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.util.kotlin.DispatcherProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetEncryptedPassphrasePayloadTest {
    @MockK
    private lateinit var cryptoContext: CryptoContext

    @MockK
    private lateinit var passphraseRepository: PassphraseRepository

    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var tested: GetEncryptedPassphrasePayload

    private val testUserId = UserId("uid")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        dispatcherProvider = TestDispatcherProvider()
        tested = GetEncryptedPassphrasePayload(
            cryptoContext = cryptoContext,
            dispatcherProvider = dispatcherProvider,
            passphraseRepository = passphraseRepository
        )
    }

    @Test
    fun `generate payload`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val aeadFactory = mockk<AeadCryptoFactory> {
            every { create(any(), any(), any(), any()) } returns mockk<AeadCrypto> {
                every { encrypt(any<String>(), any(), any()) } answers { "encrypted-${firstArg<String>()}" }
            }
        }
        val keyStoreCrypto = mockk<KeyStoreCrypto> {
            every { decrypt(any<EncryptedByteArray>()) } answers {
                PlainByteArray(firstArg<EncryptedByteArray>().array)
            }
        }
        every { cryptoContext.aeadCryptoFactory } returns aeadFactory
        every { cryptoContext.keyStoreCrypto } returns keyStoreCrypto
        coEvery { passphraseRepository.getPassphrase(testUserId) } returns
                EncryptedByteArray(byteArrayOf(112, 119, 100))

        // WHEN
        val payload = tested(
            testUserId,
            encryptionKey = EncryptedByteArray(byteArrayOf(1, 2, 3)),
            aesCipherGCMTagBits = 8,
            aesCipherIvBytes = 1
        )

        // THEN
        assertEquals(
            """encrypted-{"keyPassword":"pwd","type":"default"}""",
            payload
        )
    }
}
