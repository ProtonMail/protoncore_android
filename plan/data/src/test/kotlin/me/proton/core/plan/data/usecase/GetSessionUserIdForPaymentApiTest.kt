package me.proton.core.plan.data.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Type
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetSessionUserIdForPaymentApiTest {
    @MockK
    private lateinit var userManager: UserManager

    private lateinit var tested: GetSessionUserIdForPaymentApi

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = GetSessionUserIdForPaymentApi(userManager)
    }

    @Test
    fun `userId is null`() = runTest {
        assertNull(tested(null))
    }

    @Test
    fun `userId is credential-less`() = runTest {
        // GIVEN
        val testId = SessionUserId("test-id")
        coEvery { userManager.getUser(testId) } returns mockk {
            coEvery { type } returns Type.CredentialLess
        }

        // WHEN
        val result = tested(testId)

        // THEN
        assertNull(result)
    }

    @Test
    fun `userId is proton user`() = runTest {
        // GIVEN
        val testId = SessionUserId("test-id")
        coEvery { userManager.getUser(testId) } returns mockk {
            coEvery { type } returns Type.Proton
        }

        // WHEN
        val result = tested(testId)

        // THEN
        assertEquals(testId, result)
    }
}
