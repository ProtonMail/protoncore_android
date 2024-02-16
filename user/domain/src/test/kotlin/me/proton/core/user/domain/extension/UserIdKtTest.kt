package me.proton.core.user.domain.extension

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Type
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserIdKtTest {
    @Test
    fun `userId is null`() = runTest {
        val userId: UserId? = null
        assertTrue(userId.isNullOrCredentialLess(mockk()))
    }

    @Test
    fun `userId is credential-less`() = runTest {
        val userManager = mockk<UserManager> {
            coEvery { getUser(any()) } returns mockk {
                every { type } returns Type.CredentialLess
            }
        }
        assertTrue(UserId("test").isNullOrCredentialLess(userManager))
    }

    @Test
    fun `userId type is proton user`() = runTest {
        val userManager = mockk<UserManager> {
            coEvery { getUser(any()) } returns mockk {
                every { type } returns Type.Proton
            }
        }
        assertFalse(UserId("test").isNullOrCredentialLess(userManager))
    }

    @Test
    fun `userId type is undefined`() = runTest {
        val userManager = mockk<UserManager> {
            coEvery { getUser(any()) } returns mockk {
                every { type } returns null
            }
        }
        assertFalse(UserId("test").isNullOrCredentialLess(userManager))
    }
}
