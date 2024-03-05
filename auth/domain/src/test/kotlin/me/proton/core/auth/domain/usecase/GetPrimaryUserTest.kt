package me.proton.core.auth.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class GetPrimaryUserTest {
    @MockK
    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var userRepository: UserRepository

    private lateinit var tested: GetPrimaryUser

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = GetPrimaryUser(accountRepository, userRepository)
    }

    @Test
    fun `no primary user`() = runTest {
        // GIVEN
        coEvery { accountRepository.getPrimaryUserId() } returns flowOf(null)

        // WHEN
        val user = tested()

        // THEN
        assertNull(user)
    }

    @Test
    fun `primary user`() = runTest {
        // GIVEN
        val testUserId = UserId("test-user-id")
        val testUser = mockk<User>()
        coEvery { accountRepository.getPrimaryUserId() } returns flowOf(testUserId)
        coEvery { userRepository.getUser(testUserId) } returns testUser

        // WHEN
        val user = tested()

        // THEN
        assertSame(testUser, user)
    }
}
