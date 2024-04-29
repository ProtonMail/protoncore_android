package me.proton.core.usersettings.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsSessionAccountRecoveryEnabledTest {
    @MockK
    private lateinit var getUserSettings: GetUserSettings

    private lateinit var tested: IsSessionAccountRecoveryEnabled

    private val testUserId = UserId("user-id")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = IsSessionAccountRecoveryEnabled(getUserSettings)
    }

    @Test
    fun `sessionAccountRecovery is enabled`() = runTest {
        // GIVEN
        coEvery { getUserSettings(testUserId, false) } returns mockk {
            every { sessionAccountRecovery } returns true
        }

        // WHEN
        val result = tested(testUserId)

        // THEN
        assertTrue(result)
        coVerify(exactly = 1) { getUserSettings(testUserId, false) }
    }

    @Test
    fun `sessionAccountRecovery is initially null`() = runTest {
        // GIVEN
        coEvery { getUserSettings(testUserId, false) } returns mockk {
            every { sessionAccountRecovery } returns null
        }
        coEvery { getUserSettings(testUserId, true) } returns mockk {
            every { sessionAccountRecovery } returns true
        }

        // WHEN
        val result = tested(testUserId)

        // THEN
        assertTrue(result)
        coVerify(exactly = 1) { getUserSettings(testUserId, false) }
        coVerify(exactly = 1) { getUserSettings(testUserId, true) }
    }

    @Test
    fun `force refresh`() = runTest {
        // GIVEN
        coEvery { getUserSettings(testUserId, true) } returns mockk {
            every { sessionAccountRecovery } returns null
        }

        // WHEN
        val result = tested(testUserId, refresh = true)

        // THEN
        assertFalse(result)
        coVerify(exactly = 1) { getUserSettings(testUserId, true) }
    }

    @Test
    fun `sessionAccountRecovery is always null`() = runTest {
        // GIVEN
        coEvery { getUserSettings(testUserId, any()) } returns mockk {
            every { sessionAccountRecovery } returns null
        }

        // WHEN
        val result = tested(testUserId)

        // THEN
        assertFalse(result)
        coVerify(exactly = 2) { getUserSettings(testUserId, any()) }
    }
}
