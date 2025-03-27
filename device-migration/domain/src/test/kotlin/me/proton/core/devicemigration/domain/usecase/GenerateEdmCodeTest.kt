package me.proton.core.devicemigration.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.entity.SessionForkSelector
import me.proton.core.auth.domain.entity.SessionForkUserCode
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.devicemigration.domain.entity.ChildClientId
import me.proton.core.devicemigration.domain.entity.EdmParams
import me.proton.core.devicemigration.domain.entity.EncryptionKey
import me.proton.core.domain.entity.Product
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateEdmCodeTest {
    @MockK
    private lateinit var authRepository: AuthRepository

    @MockK
    private lateinit var cryptoContext: CryptoContext

    @MockK
    private lateinit var keyStoreCrypto: KeyStoreCrypto

    @MockK
    private lateinit var pgpCrypto: PGPCrypto

    private lateinit var tested: GenerateEdmCode

    private val testProduct = Product.Mail

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { cryptoContext.keyStoreCrypto } returns keyStoreCrypto
        every { cryptoContext.pgpCrypto } returns pgpCrypto
        tested = GenerateEdmCode(
            authRepository = authRepository,
            cryptoContext = cryptoContext,
            product = testProduct
        )
    }

    @Test
    fun `generate EDM code`() = runTest {
        // GIVEN
        coEvery { authRepository.getSessionForks(any()) } returns Pair(
            SessionForkSelector("selector"),
            SessionForkUserCode("user-code")
        )
        val randomBytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdef".toByteArray(Charsets.US_ASCII)
        val encryptedRandomBytes = EncryptedByteArray(byteArrayOf(1, 2, 3))
        every { keyStoreCrypto.encrypt(PlainByteArray(randomBytes)) } returns encryptedRandomBytes
        every { pgpCrypto.generateRandomBytes(size = 32) } returns randomBytes

        // WHEN
        val result = tested(sessionId = null)

        // THEN
        assertEquals("user-code:QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVphYmNkZWY=:AndroidMail", result.qrCodeContent)
        assertEquals("selector", result.selector.value)
        assertEquals(
            EdmParams(
                childClientId = ChildClientId("AndroidMail"),
                encryptionKey = EncryptionKey(encryptedRandomBytes),
                userCode = SessionForkUserCode("user-code")
            ),
            result.edmParams
        )
    }
}
