package me.proton.core.userrecovery.domain.usecase

import io.mockk.every
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GetExistingVerifiedRecoverySecretTest : BaseUserKeysTest() {
    private lateinit var tested: GetExistingVerifiedRecoverySecret

    @BeforeTest
    fun setUp() {
        tested = GetExistingVerifiedRecoverySecret(testCryptoContext, testUserManager, testUserRemoteDataSource)
    }

    @Test
    fun `verified secret is returned`() = runTest {
        // GIVEN
        every { testPgpCrypto.verifyText(any(), any(), any()) } returns true

        // THEN
        val verifiedSecret = tested(testUser.userId)

        // THEN
        assertEquals(testSecretValid, verifiedSecret)
    }

    @Test
    fun `returns null if secret is not verified`() = runTest {
        // GIVEN
        every { testPgpCrypto.verifyText(any(), any(), any()) } returns false

        // THEN
        val verifiedSecret = tested(testUser.userId)

        // THEN
        assertNull(verifiedSecret)
    }

    @Test
    fun `throws if no primary key`() = runTest {
        // GIVEN
        every { testPrivateKeyPrimary.isPrimary } returns false

        // WHEN
        assertFailsWith<IllegalArgumentException> {
            tested.invoke(testUser.userId)
        }
    }
}