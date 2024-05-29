package me.proton.core.usersettings.domain.usecase

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.fido.domain.entity.Fido2RegisteredKey
import me.proton.core.auth.domain.feature.IsFido2Enabled
import me.proton.core.domain.entity.UserId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class ObserveRegisteredSecurityKeysTest {
    @MockK
    private lateinit var isFido2Enabled: IsFido2Enabled

    @MockK
    private lateinit var observeUserSettings: ObserveUserSettings

    private lateinit var tested: ObserveRegisteredSecurityKeys
    private val testUserId = UserId("user-id")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ObserveRegisteredSecurityKeys(isFido2Enabled, observeUserSettings)
    }

    @Test
    fun `fido2 is disabled`() = runTest {
        every { isFido2Enabled(testUserId) } returns false
        every { observeUserSettings(testUserId, any()) } returns flowOf(mockk(relaxed = true))

        tested(testUserId).test {
            expectNoEvents()
        }
    }

    @Test
    fun `no registered keys`() = runTest {
        every { isFido2Enabled(testUserId) } returns true
        every { observeUserSettings(testUserId, any()) } returns flowOf(mockk(relaxed = true))

        tested(testUserId).test {
            assertTrue(awaitItem().isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `registered keys`() = runTest {
        val testRegisteredKeys = listOf<Fido2RegisteredKey>(mockk(), mockk())
        every { isFido2Enabled(testUserId) } returns true
        every { observeUserSettings(testUserId, any()) } returns flowOf(mockk {
            every { twoFA } returns mockk {
                every { registeredKeys } returns testRegisteredKeys
            }
        })

        tested(testUserId).test {
            assertContentEquals(testRegisteredKeys, awaitItem())
            awaitComplete()
        }
    }
}
