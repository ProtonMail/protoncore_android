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
import me.proton.core.crypto.common.pgp.PGPCrypto
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
    private lateinit var pgpCrypto: PGPCrypto

    private lateinit var tested: GenerateEdmCode

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { cryptoContext.pgpCrypto } returns pgpCrypto
        tested = GenerateEdmCode(
            authRepository = authRepository,
            cryptoContext = cryptoContext,
            product = Product.Mail
        )
    }

    @Test
    fun `generate EDM code`() = runTest {
        // GIVEN
        coEvery { authRepository.getSessionForks(any()) } returns Pair(
            SessionForkSelector("selector"),
            SessionForkUserCode("user-code")
        )
        every { pgpCrypto.generateRandomBytes(size = 32) } returns
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdef".toByteArray(Charsets.US_ASCII)

        // WHEN
        val (code, selector) = tested(sessionId = null)

        // THEN
        assertEquals("user-code:QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVphYmNkZWY=:AndroidMail", code)
        assertEquals("selector", selector.value)
    }
}