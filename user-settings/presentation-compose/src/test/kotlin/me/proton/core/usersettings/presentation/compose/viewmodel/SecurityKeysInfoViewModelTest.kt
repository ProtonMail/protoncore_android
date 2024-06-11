package me.proton.core.usersettings.presentation.compose.viewmodel

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.fido.domain.entity.Fido2RegisteredKey
import me.proton.core.domain.entity.UserId
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.usersettings.domain.usecase.ObserveRegisteredSecurityKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SecurityKeysInfoViewModelTest : CoroutinesTest by CoroutinesTest() {

    @MockK
    private lateinit var observeRegisteredSecurityKeys: ObserveRegisteredSecurityKeys

    @MockK
    private lateinit var accountManager: AccountManager

    private val testUserId = UserId("test_user_id")

    private lateinit var tested: SecurityKeysInfoViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { accountManager.getPrimaryUserId() } returns flowOf(testUserId)
        tested = SecurityKeysInfoViewModel(accountManager, observeRegisteredSecurityKeys)
    }

    @Test
    fun `security keys exist`() = coroutinesTest {
        // GIVEN
        coEvery { observeRegisteredSecurityKeys.invoke(testUserId, any()) } returns flowOf(
            listOf(
                Fido2RegisteredKey("format", UByteArray(10), "Test key 1"),
                Fido2RegisteredKey("format", UByteArray(10), "Test key 2"),
                Fido2RegisteredKey("format", UByteArray(10), "Test key 3"),
            )
        )
        // WHEN
        tested.state.test {
            // THEN
            assertEquals(SecurityKeysState.Loading, awaitItem())
            val nextItem = awaitItem()
            assertIs<SecurityKeysState.Success>(nextItem)
            assertEquals(3, (nextItem as SecurityKeysState.Success).keys.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `security keys empty`() = coroutinesTest {
        // GIVEN
        coEvery { observeRegisteredSecurityKeys.invoke(testUserId, any()) } returns flowOf(emptyList())
        // WHEN
        tested.state.test {
            // THEN
            assertEquals(SecurityKeysState.Loading, awaitItem())
            val nextItem = awaitItem()
            assertIs<SecurityKeysState.Success>(nextItem)
            assertEquals(0, (nextItem as SecurityKeysState.Success).keys.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `security keys error`() = coroutinesTest {
        // GIVEN
        coEvery { observeRegisteredSecurityKeys.invoke(testUserId, any()) } throws Error("Test error")
        // WHEN
        tested.state.test {
            // THEN
            assertEquals(SecurityKeysState.Loading, awaitItem())
            val nextItem = awaitItem()
            assertIs<SecurityKeysState.Error>(nextItem)
            assertEquals("Test error", (nextItem as SecurityKeysState.Error).throwable?.message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}